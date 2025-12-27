use crate::model::motion_data::MotionData;

/// Motion is already mapped → return directly
pub fn map_motion(data: &MotionData) -> (i32, i32) {
    (data.motion.dx as i32, data.motion.dy as i32)
}
