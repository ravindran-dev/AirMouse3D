use serde::Deserialize;
use std::collections::HashMap;

#[derive(Debug, Deserialize)]
pub struct SessionEntry {
    pub active: Option<bool>,
}

pub type SessionsMap = HashMap<String, SessionEntry>;
