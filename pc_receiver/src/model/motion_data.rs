use serde::Deserialize;

#[derive(Debug, Deserialize)]
pub struct MotionData {

    #[serde(rename = "gyroX")]
    pub gyro_x: Option<f64>,

    #[serde(rename = "gyroY")]
    pub gyro_y: Option<f64>,

   
    pub gesture: Option<String>,
}
