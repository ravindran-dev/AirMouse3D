use crate::model::motion_data::MotionData;
const SENSITIVITY: f64 = 20.0;
pub fn map_motion(data: &MotionData) -> (i32, i32) {
    let dx = (data.motion.dx * SENSITIVITY).round() as i32;
    let dy = (data.motion.dy * SENSITIVITY).round() as i32;
    (dx, dy)
}