use crate::server::SharedStatus;
use eframe::egui;
use std::time::{Duration, Instant};

/// How long without a fresh packet before we stop showing "Connected" and fall back to
/// "waiting to reconnect" -- mirrors `Constants.CONNECTION_ACK_TIMEOUT_MS` on the Android side.
const LINK_TIMEOUT: Duration = Duration::from_secs(3);

// Palette (matches the app's indigo Material theme).
const ACCENT: egui::Color32 = egui::Color32::from_rgb(124, 116, 255);
const GOOD: egui::Color32 = egui::Color32::from_rgb(76, 195, 138);
const WARN: egui::Color32 = egui::Color32::from_rgb(230, 176, 63);
const BAD: egui::Color32 = egui::Color32::from_rgb(232, 96, 84);
const CARD: egui::Color32 = egui::Color32::from_rgb(32, 34, 46);
const MUTED: egui::Color32 = egui::Color32::from_rgb(150, 154, 170);

pub struct ReceiverApp {
    status: SharedStatus,
    address_text: String,
    qr_texture: egui::TextureHandle,
    copied_at: Option<Instant>,
    // Packets-per-second estimate, refreshed ~once a second from the running total.
    rate: f32,
    rate_sample: Option<(Instant, u64)>,
}

impl ReceiverApp {
    pub fn new(
        cc: &eframe::CreationContext<'_>,
        status: SharedStatus,
        address_text: String,
        qr_image: egui::ColorImage,
    ) -> Self {
        apply_style(&cc.egui_ctx);
        let qr_texture =
            cc.egui_ctx
                .load_texture("pairing-qr", qr_image, egui::TextureOptions::NEAREST);
        Self {
            status,
            address_text,
            qr_texture,
            copied_at: None,
            rate: 0.0,
            rate_sample: None,
        }
    }

    fn update_rate(&mut self, total: u64) {
        let now = Instant::now();
        match self.rate_sample {
            Some((t, prev)) if now.duration_since(t) >= Duration::from_millis(1000) => {
                let dt = now.duration_since(t).as_secs_f32();
                self.rate = (total.saturating_sub(prev)) as f32 / dt;
                self.rate_sample = Some((now, total));
            }
            None => self.rate_sample = Some((now, total)),
            _ => {}
        }
    }
}

impl eframe::App for ReceiverApp {
    fn ui(&mut self, ui: &mut egui::Ui, _frame: &mut eframe::Frame) {
        // The UDP thread updates status independently of egui's input-driven repaint, so tick.
        ui.ctx().request_repaint_after(Duration::from_millis(150));

        let status = self.status.lock().unwrap().clone();
        self.update_rate(status.packets_received);

        egui::CentralPanel::default().show(ui, |ui| {
            ui.add_space(6.0);
            ui.horizontal(|ui| {
                ui.add_space(2.0);
                ui.heading(egui::RichText::new("AirMouse3D").color(ACCENT).strong());
                ui.label(egui::RichText::new("Receiver").color(MUTED).size(18.0));
            });
            ui.label(
                egui::RichText::new("Keep this window open while you use your phone as a mouse.")
                    .color(MUTED)
                    .size(12.0),
            );
            ui.add_space(10.0);

            if let Some(err) = &status.fatal_error {
                error_card(ui, err);
                return;
            }

            let connected = status
                .last_packet_at
                .map(|t| t.elapsed() < LINK_TIMEOUT)
                .unwrap_or(false);

            egui::Frame::new()
                .fill(CARD)
                .corner_radius(16u8)
                .inner_margin(egui::Margin::same(16))
                .show(ui, |ui| {
                    ui.horizontal(|ui| {
                        // QR on white, so any camera scans it cleanly.
                        egui::Frame::new()
                            .fill(egui::Color32::WHITE)
                            .corner_radius(10u8)
                            .inner_margin(egui::Margin::same(6))
                            .show(ui, |ui| {
                                ui.add(
                                    egui::Image::new(&self.qr_texture)
                                        .fit_to_exact_size(egui::vec2(150.0, 150.0)),
                                );
                            });

                        ui.add_space(16.0);

                        ui.vertical(|ui| {
                            ui.set_max_width(230.0); // so the labels below wrap instead of clip
                            ui.label(
                                egui::RichText::new("Scan to connect")
                                    .color(egui::Color32::WHITE)
                                    .size(15.0)
                                    .strong(),
                            );
                            ui.label(
                                egui::RichText::new("Open AirMouse3D, tap Scan QR Code.")
                                    .color(MUTED)
                                    .size(12.0),
                            );
                            ui.add_space(10.0);

                            ui.label(egui::RichText::new("Address").color(MUTED).size(11.0));
                            ui.add(
                                egui::TextEdit::singleline(&mut self.address_text)
                                    .font(egui::TextStyle::Monospace)
                                    .desired_width(190.0),
                            );
                            ui.add_space(4.0);

                            let copied = self
                                .copied_at
                                .map(|t| t.elapsed() < Duration::from_millis(1400))
                                .unwrap_or(false);
                            let label = if copied { "Copied!" } else { "Copy address" };
                            if ui.button(label).clicked() {
                                ui.ctx().copy_text(self.address_text.clone());
                                self.copied_at = Some(Instant::now());
                            }
                        });
                    });
                });

            ui.add_space(12.0);
            status_pill(ui, &status, connected);

            if status.packets_received > 0 {
                ui.add_space(10.0);
                stats_row(ui, &status, self.rate);
            }

            ui.add_space(10.0);
            ui.separator();
            ui.label(
                egui::RichText::new(
                    "Tip: phone and PC must be on the same Wi-Fi. No internet needed.",
                )
                .color(MUTED)
                .size(11.0),
            );
        });
    }
}

fn status_pill(ui: &mut egui::Ui, status: &crate::server::ReceiverStatus, connected: bool) {
    let (color, text) = if connected {
        (
            GOOD,
            format!("Connected — {}", status.peer_addr.clone().unwrap_or_default()),
        )
    } else if status.packets_received > 0 {
        (WARN, "Phone disconnected — waiting to reconnect…".to_string())
    } else {
        (MUTED, "Waiting for phone to connect…".to_string())
    };

    // Pulse the dot when packets are actively flowing.
    let live = status
        .last_packet_at
        .map(|t| t.elapsed() < Duration::from_millis(250))
        .unwrap_or(false);

    egui::Frame::new()
        .fill(color.linear_multiply(0.16))
        .corner_radius(12u8)
        .inner_margin(egui::Margin::symmetric(14, 10))
        .show(ui, |ui| {
            ui.horizontal(|ui| {
                let (rect, _) = ui.allocate_exact_size(egui::vec2(12.0, 12.0), egui::Sense::hover());
                let dot = if live { color } else { color.linear_multiply(0.7) };
                ui.painter().circle_filled(rect.center(), 5.0, dot);
                ui.label(egui::RichText::new(text).color(color).strong());
            });
        });
}

fn stats_row(ui: &mut egui::Ui, status: &crate::server::ReceiverStatus, rate: f32) {
    let uptime = status
        .first_packet_at
        .map(|t| format_duration(t.elapsed()))
        .unwrap_or_else(|| "—".to_string());

    ui.horizontal(|ui| {
        stat(ui, "Packets", &format!("{}", status.packets_received));
        ui.add_space(18.0);
        stat(ui, "Rate", &format!("{:.0}/s", rate));
        ui.add_space(18.0);
        stat(ui, "Uptime", &uptime);
    });
}

fn stat(ui: &mut egui::Ui, label: &str, value: &str) {
    ui.vertical(|ui| {
        ui.label(egui::RichText::new(label).color(MUTED).size(11.0));
        ui.label(
            egui::RichText::new(value)
                .color(egui::Color32::WHITE)
                .size(16.0)
                .strong(),
        );
    });
}

fn error_card(ui: &mut egui::Ui, message: &str) {
    egui::Frame::new()
        .fill(BAD.linear_multiply(0.16))
        .corner_radius(12u8)
        .inner_margin(egui::Margin::same(14))
        .show(ui, |ui| {
            ui.label(egui::RichText::new("Can't start").color(BAD).strong());
            ui.add_space(4.0);
            ui.label(egui::RichText::new(message).color(egui::Color32::WHITE));
        });
}

fn format_duration(d: Duration) -> String {
    let secs = d.as_secs();
    let (h, m, s) = (secs / 3600, (secs % 3600) / 60, secs % 60);
    if h > 0 {
        format!("{h}h {m}m")
    } else if m > 0 {
        format!("{m}m {s}s")
    } else {
        format!("{s}s")
    }
}

fn apply_style(ctx: &egui::Context) {
    let mut visuals = egui::Visuals::dark();
    visuals.panel_fill = egui::Color32::from_rgb(20, 21, 30);
    visuals.window_fill = egui::Color32::from_rgb(20, 21, 30);
    visuals.override_text_color = Some(egui::Color32::from_rgb(224, 226, 236));
    visuals.widgets.inactive.corner_radius = egui::CornerRadius::same(8);
    visuals.widgets.hovered.corner_radius = egui::CornerRadius::same(8);
    visuals.widgets.active.corner_radius = egui::CornerRadius::same(8);
    ctx.set_visuals(visuals);

    ctx.all_styles_mut(|style| {
        style.spacing.item_spacing = egui::vec2(8.0, 6.0);
        style.spacing.button_padding = egui::vec2(12.0, 6.0);
    });
}

/// Renders `text` as a QR code image egui can display as a texture. Hand-rasterized straight
/// from the QR module grid (no `image` crate involved) to avoid pulling in a second, possibly
/// mismatched, version of that crate via `qrcode`'s own optional image integration.
pub fn qr_color_image(text: &str) -> egui::ColorImage {
    let code = qrcode::QrCode::new(text).expect("failed to build QR code");
    let modules_per_side = code.width();
    let dark_modules: Vec<bool> = code
        .to_colors()
        .into_iter()
        .map(|c| c == qrcode::Color::Dark)
        .collect(); // row-major, true = dark module

    const SCALE: usize = 8; // pixels per module, for a crisp, easily scannable image
    const QUIET_ZONE: usize = 4; // modules of white border a scanner needs to reliably lock on

    let side_modules = modules_per_side + QUIET_ZONE * 2;
    let side_px = side_modules * SCALE;

    let mut rgba = vec![255u8; side_px * side_px * 4];

    for row in 0..modules_per_side {
        for col in 0..modules_per_side {
            if !dark_modules[row * modules_per_side + col] {
                continue;
            }
            let base_row = (row + QUIET_ZONE) * SCALE;
            let base_col = (col + QUIET_ZONE) * SCALE;
            for dy in 0..SCALE {
                for dx in 0..SCALE {
                    let px = base_col + dx;
                    let py = base_row + dy;
                    let idx = (py * side_px + px) * 4;
                    rgba[idx] = 0;
                    rgba[idx + 1] = 0;
                    rgba[idx + 2] = 0;
                }
            }
        }
    }

    egui::ColorImage::from_rgba_unmultiplied([side_px, side_px], &rgba)
}
