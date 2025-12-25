mod config;
mod firebase;
mod model;
mod cursor;
mod utils;

use enigo::{Enigo, Settings};
use tokio::time::{sleep, Duration};

use firebase::client::fetch_motion_data;
use cursor::mapper::map_motion;
use cursor::controller::apply_cursor;

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

    loop {
        if let Some(data) = fetch_motion_data(&url).await {
            let (dx, dy) = map_motion(&data);
            apply_cursor(dx, dy, &data, &mut enigo);
        }

        sleep(Duration::from_millis(config::POLL_INTERVAL_MS)).await;
    }
}
