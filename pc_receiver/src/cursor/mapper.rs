use crate::config::SENSITIVITY;
use crate::model::motion_data::MotionData;
use crate::utils::filters::apply_dead_zone;


pub fn map_motion(data: &MotionData) -> (i32, i32) {
    let gx = apply_dead_zone(data.gyro_x.unwrap_or(0.0), 0.05);
    let gy = apply_dead_zone(data.gyro_y.unwrap_or(0.0), 0.05);

    let dx = (gy * SENSITIVITY) as i32;
    let dy = (gx * SENSITIVITY) as i32;

    (dx, dy)
}
