use crate::model::motion_data::MotionData;
use std::sync::Mutex;


static LAST: Mutex<(f64, f64)> = Mutex::new((0.0, 0.0));

// Mouse-like tuning
const SENSITIVITY: f64 = 6.0;     
const DEAD: f64 = 0.8;           
const MAX_INPUT: f64 = 15.0;      
const SMOOTH: f64 = 0.4;          

pub fn map_motion(data: &MotionData) -> (i32, i32) {
    let mut last = LAST.lock().unwrap();

    
    let mut x = data.motion.dx;
    let mut y = -data.motion.dy; // invert Y for screen coordinates

    
    if x.abs() < DEAD && y.abs() < DEAD {
        last.0 = 0.0;
        last.1 = 0.0;
        return (0, 0);
    }

    
    x = (x / MAX_INPUT).clamp(-1.0, 1.0);
    y = (y / MAX_INPUT).clamp(-1.0, 1.0);

   
    x = last.0 + (x - last.0) * SMOOTH;
    y = last.1 + (y - last.1) * SMOOTH;

    last.0 = x;
    last.1 = y;

    
    let dx = (x * SENSITIVITY).round() as i32;
    let dy = (y * SENSITIVITY).round() as i32;

    (dx, dy)
}

