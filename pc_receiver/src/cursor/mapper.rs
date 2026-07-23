use crate::model::motion_data::MotionPayload;

/// The phone already applies shake rejection, a low-pass filter, a dead zone, and sensitivity
/// scaling before ever sending a packet (see `MotionProcessor` on the Android side) -- dx/dy
/// here are the final, ready-to-apply pixel deltas. Running a second, independent smoothing +
/// dead-zone + rescale pass on this side (as the old Firebase-polling version did) only stacks
/// a second filter's lag on top of the first, which is exactly what made movement feel laggy
/// and imprecise rather than "exact, like a real mouse". This just maps axes for screen space
/// and applies one generous safety clamp against a corrupt or malicious packet -- no shaping.
const MAX_PIXELS_PER_PACKET: f64 = 300.0;

pub fn map_motion(data: &MotionPayload) -> (i32, i32) {
    let dx = (-data.dy).clamp(-MAX_PIXELS_PER_PACKET, MAX_PIXELS_PER_PACKET);
    let dy = data.dx.clamp(-MAX_PIXELS_PER_PACKET, MAX_PIXELS_PER_PACKET);

    (dx.round() as i32, dy.round() as i32)
}
