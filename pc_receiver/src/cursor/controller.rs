use enigo::{Enigo, Mouse, Button, Direction, Coordinate};
use crate::model::motion_data::MotionData;

pub fn apply_cursor(dx: i32, dy: i32, data: &MotionData, enigo: &mut Enigo) {
    if dx != 0 || dy != 0 {
        enigo.move_mouse(dx, dy, Coordinate::Rel).ok();
    }

    if data.motion.click {
        enigo.button(Button::Left, Direction::Press).ok();
        enigo.button(Button::Left, Direction::Release).ok();
    }
}
