// Hide the console window in release builds so this behaves like a normal double-click-able
// Windows app; kept for debug builds so `cargo run` still shows println!/panic output.
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod config;
mod cursor;
mod gui;
mod icon;
mod model;
mod net;
mod server;
mod utils;

use server::ReceiverStatus;
use std::sync::{Arc, Mutex};

fn main() -> eframe::Result<()> {
    let local_ip = net::discovery::local_ipv4().unwrap_or_else(|| "0.0.0.0".to_string());
    let address = format!("{}:{}", local_ip, config::UDP_LISTEN_PORT);

    let status: server::SharedStatus = Arc::new(Mutex::new(ReceiverStatus::default()));
    server::spawn(status.clone());

    let qr_image = gui::qr_color_image(&address);

    // Live window / taskbar icon, drawn procedurally (see src/icon.rs) so it matches the exe's
    // embedded icon without shipping a binary asset.
    let icon = eframe::egui::IconData {
        rgba: icon::rgba(64),
        width: 64,
        height: 64,
    };

    let options = eframe::NativeOptions {
        viewport: eframe::egui::ViewportBuilder::default()
            .with_title("AirMouse3D Receiver")
            .with_inner_size([540.0, 470.0])
            .with_min_inner_size([510.0, 440.0])
            .with_icon(Arc::new(icon)),
        ..Default::default()
    };

    eframe::run_native(
        "AirMouse3D Receiver",
        options,
        Box::new(move |cc| Ok(Box::new(gui::ReceiverApp::new(cc, status, address, qr_image)))),
    )
}
