mod config;
mod firebase;
mod model;
mod cursor;
mod utils;

use crate::model::motion_data::{MotionData, MotionPayload};
use enigo::{Enigo, Settings};
use tokio::time::{sleep, Duration, Instant};
use std::sync::{Arc, Mutex};

use firebase::client::{fetch_motion_data, fetch_active_session_id};
use cursor::mapper::map_motion;
use cursor::controller::apply_cursor;

#[derive(Clone, Copy)]
struct SharedMotion {
    dx: f64,
    dy: f64,
    timestamp: u64,
}

struct ClickDebounce {
    last_click: Instant,
    cooldown: Duration,
    last_click_state: bool,
}

#[tokio::main]
async fn main() {
    println!("🖥 Rust PC Receiver started");
    println!("🔍 Waiting for active session...");

    // ===== AUTO DETECT ACTIVE SESSION ID =====
    let session_id = loop {
        if let Some(id) = fetch_active_session_id(config::FIREBASE_BASE_URL).await {
            println!("✅ Connected to session: {}", id);
            break id;
        }
        sleep(Duration::from_secs(1)).await;
    };

    // ✅ IMPORTANT: still reading FULL SESSION JSON (not /motion.json)
    let url = format!(
        "{}/sessions/{}.json",
        config::FIREBASE_BASE_URL,
        session_id
    );

    let mut enigo = Enigo::new(&Settings::default())
        .expect("Failed to initialize Enigo");

    let shared_motion = Arc::new(Mutex::new(SharedMotion {
        dx: 0.0,
        dy: 0.0,
        timestamp: 0,
    }));

    let shared_click = Arc::new(Mutex::new(false));

    let mut click_debounce = ClickDebounce {
        last_click: Instant::now() - Duration::from_secs(1),
        cooldown: Duration::from_millis(750),
        last_click_state: false,
    };

    // ===== FIREBASE POLLING TASK =====
    {
        let shared_motion = Arc::clone(&shared_motion);
        let shared_click = Arc::clone(&shared_click);
        let url = url.clone();

        tokio::spawn(async move {
            loop {
                if let Some(data) = fetch_motion_data(&url).await {
                    let mut sm = shared_motion.lock().unwrap();
                    sm.dx = data.motion.dx;
                    sm.dy = data.motion.dy;
                    sm.timestamp = data.motion.timestamp;
                    drop(sm);

                    let mut click = shared_click.lock().unwrap();
                    *click = data.motion.click;
                }
                sleep(Duration::from_millis(config::POLL_INTERVAL_MS)).await;
            }
        });
    }

    println!("🎯 Receiving motion data...");

    // ===== CURSOR LOOP =====
    loop {
        let (dx, dy, ts, click) = {
            let sm = shared_motion.lock().unwrap();
            let click = shared_click.lock().unwrap();
            (sm.dx, sm.dy, sm.timestamp, *click)
        };

        let should_click = click
            && !click_debounce.last_click_state
            && click_debounce.last_click.elapsed() >= click_debounce.cooldown;

        let motion_data = MotionData {
            motion: MotionPayload {
                dx,
                dy,
                click: should_click,
                timestamp: ts,
            },
        };

        if should_click {
            click_debounce.last_click = Instant::now();
        }
        click_debounce.last_click_state = click;

        let (mx, my) = map_motion(&motion_data);
        apply_cursor(mx, my, &motion_data, &mut enigo);

        sleep(Duration::from_millis(8)).await;
    }
}
