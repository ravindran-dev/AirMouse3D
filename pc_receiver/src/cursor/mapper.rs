use crate::model::motion_data::MotionData;
use std::sync::Mutex;
use std::time::Instant;

const SENSITIVITY: f64 = 100.0;
const DAMP: f64 = 0.85; 
const CLEAR: f64 = 0.7; 
static STATE: Mutex<(f64, f64, f64, f64, Option<Instant>)> =
    Mutex::new((0.0, 0.0, 0.0, 0.0, None));
pub fn map_motion(data: &MotionData)->(i32, i32){
    let now = Instant::now();
    let mut state = STATE.lock().unwrap();
    let dt = if let Some(prev) = state.4 {
        now.duration_since(prev).as_secs_f64()
    } else{
        0.0
    };
    state.4 = Some(now);
    state.2 = state.2 * CLEAR + data.motion.dx * (1.0 - CLEAR);
    state.3 = state.3 * CLEAR + data.motion.dy * (1.0 - CLEAR);
    state.2 *= DAMP;
    state.3 *= DAMP;
    state.0 += state.2 * dt * SENSITIVITY;
    state.1 += state.3 * dt * SENSITIVITY;
    let dx = state.0.round() as i32;
    let dy = state.1.round() as i32;
    state.0 -= dx as f64;
    state.1 -= dy as f64;
    (dx, dy)
}
