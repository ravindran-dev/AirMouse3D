use crate::model::motion_data::MotionData;

pub async fn fetch_motion_data(url: &str) -> Option<MotionData> {
    let response = reqwest::get(url).await.ok()?;
    let data = response.json::<MotionData>().await.ok()?;
    Some(data)
}
