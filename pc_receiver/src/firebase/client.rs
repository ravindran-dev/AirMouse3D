use crate::model::motion_data::MotionData;
use crate::model::session::SessionsMap;

pub async fn fetch_active_session_id(base_url: &str) -> Option<String> {
    let url = format!("{}/sessions.json", base_url);

    let res = reqwest::get(&url).await.ok()?;
    let sessions: SessionsMap = res.json().await.ok()?;

    for (id, entry) in sessions {
        if entry.active.unwrap_or(false) {
            return Some(id);
        }
    }
    None
}

pub async fn fetch_motion_data(url: &str) -> Option<MotionData> {
    let res = reqwest::get(url).await.ok()?;
    res.json::<MotionData>().await.ok()
}
