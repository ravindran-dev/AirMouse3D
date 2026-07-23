use crate::config;
use crate::cursor::controller::apply_cursor;
use crate::cursor::mapper::map_motion;
use crate::model::motion_data::MotionPayload;
use enigo::{Enigo, Settings};
use std::sync::{Arc, Mutex};
use std::time::Instant;
use tokio::net::UdpSocket;

/// Live state the background UDP loop publishes for the GUI thread to read every frame.
#[derive(Clone, Default)]
pub struct ReceiverStatus {
    pub peer_addr: Option<String>,
    pub first_packet_at: Option<Instant>,
    pub last_packet_at: Option<Instant>,
    pub packets_received: u64,
    /// Set once and never cleared if the server thread couldn't start at all (port already in
    /// use, cursor control unavailable, ...) -- surfaced as a banner in the GUI instead of a
    /// background thread silently dying with nothing visible to the user.
    pub fatal_error: Option<String>,
}

pub type SharedStatus = Arc<Mutex<ReceiverStatus>>;

/// Runs the UDP receive loop on its own OS thread (with its own Tokio runtime), independent of
/// the GUI's event loop on the main thread.
pub fn spawn(status: SharedStatus) {
    std::thread::spawn(move || {
        let runtime = match tokio::runtime::Runtime::new() {
            Ok(runtime) => runtime,
            Err(err) => {
                status.lock().unwrap().fatal_error =
                    Some(format!("Couldn't start the network runtime: {err}"));
                return;
            }
        };
        runtime.block_on(run(status));
    });
}

async fn run(status: SharedStatus) {
    let socket = match UdpSocket::bind(("0.0.0.0", config::UDP_LISTEN_PORT)).await {
        Ok(socket) => socket,
        Err(err) => {
            status.lock().unwrap().fatal_error = Some(format!(
                "Couldn't bind UDP port {}: {err}. Is another copy of this app already running?",
                config::UDP_LISTEN_PORT,
            ));
            return;
        }
    };

    let mut enigo = match Enigo::new(&Settings::default()) {
        Ok(enigo) => enigo,
        Err(err) => {
            status.lock().unwrap().fatal_error =
                Some(format!("Couldn't initialize cursor control: {err}"));
            return;
        }
    };

    let mut buf = [0u8; 1024];
    loop {
        let (len, peer) = match socket.recv_from(&mut buf).await {
            Ok(result) => result,
            Err(_) => continue,
        };

        let payload: MotionPayload = match serde_json::from_slice(&buf[..len]) {
            Ok(payload) => payload,
            Err(_) => continue,
        };

        let (mx, my) = map_motion(&payload);
        apply_cursor(mx, my, &payload, &mut enigo);
        let _ = socket.send_to(b"{\"ack\":true}", peer).await;

        let now = Instant::now();
        let mut s = status.lock().unwrap();
        if s.first_packet_at.is_none() {
            s.first_packet_at = Some(now);
        }
        s.peer_addr = Some(peer.to_string());
        s.last_packet_at = Some(now);
        s.packets_received += 1;
    }
}
