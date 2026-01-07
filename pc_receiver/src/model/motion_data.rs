use serde::Deserialize;

#[derive(Debug, Deserialize)]
pub struct MotionData {
    pub motion: MotionPayload,
}

#[derive(Debug, Deserialize)]
pub struct MotionPayload {
    pub dx: f64,
    pub dy: f64,
    pub click: bool,
    pub timestamp: Option<i64>,
}
