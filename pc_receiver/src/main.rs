mod config;
mod firebase;
mod model;
mod cursor;
mod utils;

use enigo::{Enigo, Settings};
use tokio::time::{sleep, Duration, Instant};

use firebase::client::{fetch_motion_data, fetch_active_session_id};
use cursor::mapper::map_motion;
use cursor::controller::apply_cursor;

use crate::model::motion_data::{MotionData, MotionPayload};

struct ClickDebounce {
    last_click: Instant,
    cooldown: Duration,
    last_click_state: bool,
}

#[tokio::main]
async fn main() {
    println!(" Rust PC Receiver started");
    println!(" Waiting for active mobile session...");

    
    let session_id = loop {
        if let Some(id) = fetch_active_session_id(config::FIREBASE_BASE_URL).await {
            println!(" Connected to session: {}", id);
            break id;
        }
        sleep(Duration::from_secs(1)).await;
    };

    let motion_url = format!(
        "{}/sessions/{}/motion.json",
        config::FIREBASE_BASE_URL,
        session_id
    );

   

    let mut enigo = Enigo::new(&Settings::default())
        .expect("Failed to initialize Enigo");

    let mut click_debounce = ClickDebounce {
        last_click: Instant::now() - Duration::from_secs(1),
        cooldown: Duration::from_millis(500),
        last_click_state: false,
    };

    println!(" Receiving motion data...");

    

    loop {
        if let Some(data) = fetch_motion_data(&motion_url).await {

           
            let should_click = data.motion.click
                && !click_debounce.last_click_state
                && click_debounce.last_click.elapsed() >= click_debounce.cooldown;

            let motion_data = MotionData {
                motion: MotionPayload {
                    dx: data.motion.dx,
                    dy: data.motion.dy,
                    click: should_click,
                    timestamp: data.motion.timestamp,
                },
            };

            if should_click {
                click_debounce.last_click = Instant::now();
            }

            click_debounce.last_click_state = data.motion.click;

            
            let (mx, my) = map_motion(&motion_data);
            apply_cursor(mx, my, &motion_data, &mut enigo);
        }

        sleep(Duration::from_millis(config::POLL_INTERVAL_MS)).await;
    }
}
