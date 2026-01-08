use serde::Deserialize;
use std::collections::HashMap;



#[derive(Debug, Deserialize)]
pub struct MotionResponse {
    pub motion: MotionPayload,
}

#[derive(Debug, Deserialize)]
pub struct MotionPayload {
    pub dx: f64,
    pub dy: f64,
    pub click: bool,
    pub timestamp: u64,
}

pub async fn fetch_motion_data(url: &str) -> Option<MotionResponse> {
    let res = reqwest::get(url).await.ok()?;
    let data = res.json::<MotionResponse>().await.ok()?;
    Some(data)
}



#[derive(Debug, Deserialize)]
struct SessionEntry {
    active: Option<bool>,
}

pub async fn fetch_active_session_id(base_url: &str) -> Option<String> {
    let url = format!("{}/sessions.json", base_url);

    let res = reqwest::get(&url).await.ok()?;
    let map: HashMap<String, SessionEntry> = res.json().await.ok()?;

    for (id, session) in map {
        if session.active.unwrap_or(false) {
            return Some(id);
        }
    }
    None
}
