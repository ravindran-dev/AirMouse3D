use enigo::{Axis, Button, Coordinate, Direction, Enigo, Mouse};
use crate::model::motion_data::MotionPayload;

/// Applies one decoded motion payload straight to the OS cursor.
///
/// Click is a one-shot signal by construction: the phone only ever sends a non-"NONE"
/// `clickType` in the single UDP packet that corresponds to the tap/double-tap/long-press
/// gesture, then reverts to "NONE" for every packet after. That push-based, one-packet-per-event
/// model (unlike the old Firebase polling loop, which could observe the same `click == true`
/// document across several poll cycles) means no debounce/edge-detection is needed here --
/// any packet with a non-"NONE" clickType fires exactly once.
pub fn apply_cursor(dx: i32, dy: i32, data: &MotionPayload, enigo: &mut Enigo) {
    if dx != 0 || dy != 0 {
        enigo.move_mouse(dx, dy, Coordinate::Rel).ok();
    }

    if let Some(button) = button_for(&data.click_type) {
        enigo.button(button, Direction::Press).ok();
        enigo.button(button, Direction::Release).ok();
    }

    if data.scroll.abs() >= 1.0 {
        // Phone reports scroll in screen pixels moved; enigo scrolls in "lines". Negate so
        // dragging fingers up (positive screen-space delta) scrolls the page up, matching a
        // physical trackpad/mouse wheel.
        enigo.scroll(-(data.scroll.round() as i32), Axis::Vertical).ok();
    }
}

fn button_for(click_type: &str) -> Option<Button> {
    match click_type {
        "LEFT" => Some(Button::Left),
        "RIGHT" => Some(Button::Right),
        "MIDDLE" => Some(Button::Middle),
        _ => None,
    }
}
