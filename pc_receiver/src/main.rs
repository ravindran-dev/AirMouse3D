mod config;
mod firebase;
mod model;
mod cursor;
mod utils;
use crate::model::motion_data::{MotionData, MotionPayload};
use enigo::{Enigo, Settings};
use tokio::time::{sleep, Duration};
use std::sync::{Arc, Mutex};
use firebase::client::fetch_motion_data;
use cursor::mapper::map_motion;
use cursor::controller::apply_cursor;
#[derive(Clone, Copy)]
struct SharedMotion {
    dx: f64,
    dy: f64,
    timestamp: u64,
}
#[tokio::main]
async fn main() {
    println!("Rust PC Receiver started");

    let url = format!(
        "{}/sessions/{}.json",
        config::FIREBASE_BASE_URL,
        config::SESSION_ID
    );

    let mut enigo = Enigo::new(&Settings::default())
        .expect("Failed to initialize Enigo");

    let shared_motion = Arc::new(Mutex::new(SharedMotion {
        dx: 0.0,
        dy: 0.0,
        timestamp: 0,
    }));

    {
        let shared_motion = Arc::clone(&shared_motion);
        tokio::spawn(async move {
            loop {
                if let Some(data) = fetch_motion_data(&url).await {
                    let mut sm = shared_motion.lock().unwrap();
                    sm.dx = data.motion.dx;
                    sm.dy = data.motion.dy;
                    sm.timestamp = data.motion.timestamp;
                }
                sleep(Duration::from_millis(config::POLL_INTERVAL_MS)).await;
            }
        });
    }
    loop {
        let (dx, dy, ts) = {
            let sm = shared_motion.lock().unwrap();
            (sm.dx, sm.dy, sm.timestamp)
        };
        let motion_data = MotionData {
            motion: MotionPayload {
                dx,
                dy,
                click: false,
                timestamp: ts,
            },
        };
        let (mx, my) = map_motion(&motion_data);
        apply_cursor(mx, my, &motion_data, &mut enigo);
        sleep(Duration::from_millis(8)).await;
    }
}
