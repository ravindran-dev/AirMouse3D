use serde::Deserialize;

/// One UDP datagram from the phone, decoded directly (no Firebase document wrapper needed
/// anymore -- the packet payload *is* the motion sample). Field names match
/// `com.airmouse3d.model.MotionSample` on the Android side exactly.
#[derive(Debug, Deserialize)]
pub struct MotionPayload {
    pub dx: f64,
    pub dy: f64,
    pub scroll: f64,
    #[allow(dead_code)]
    pub click: bool,
    #[serde(rename = "clickType")]
    pub click_type: String,
    #[allow(dead_code)]
    pub timestamp: u64,
}
