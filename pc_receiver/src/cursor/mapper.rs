use crate::model::motion_data::MotionData;
use std::sync::Mutex;


static LAST: Mutex<(f64, f64)> = Mutex::new((0.0, 0.0));


const DEAD_ZONE: f64 = 1.2;      
const MAX_INPUT: f64 = 12.0;     
const SENSITIVITY: f64 = 7.0;    
const SMOOTH: f64 = 0.6;         

pub fn map_motion(data: &MotionData) -> (i32, i32) {
    let mut last = LAST.lock().unwrap();

    
    let mut x = data.motion.dy;  
    let mut y = data.motion.dx;  

    
    x = -x;

    
    if x.abs() < DEAD_ZONE && y.abs() < DEAD_ZONE {
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
