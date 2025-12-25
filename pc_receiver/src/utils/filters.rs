pub fn apply_dead_zone(value: f64, threshold: f64) -> f64 {
    if value.abs() < threshold {
        0.0
    } else {
        value
    }
}
