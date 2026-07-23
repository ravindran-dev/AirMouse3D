// Procedurally drawn app icon: a rounded-square indigo gradient with a white pointer arrow.
//
// Pure `std` math (no image crate, no committed binary asset) so the exact same routine can be
// used two ways: `build.rs` bakes it into a multi-size `.ico` embedded in the exe (taskbar /
// file-explorer / Task Manager icon), and `main.rs` uses it for the live window icon. Keeping
// it as code means there's one source of truth and nothing binary to review or drift.
// (Plain `//` comments, not `//!` — this file is `include!`d into build.rs inside a `mod`.)

/// Returns straight (un-premultiplied) RGBA bytes for a `size`x`size` icon, 4x supersampled for
/// smooth edges. Corners outside the rounded square are fully transparent.
pub fn rgba(size: u32) -> Vec<u8> {
    const SS: u32 = 4; // supersampling factor
    let big = (size * SS) as f32;
    let radius = big * 0.22;

    // Classic pointer-arrow polygon in a normalized 0..1 box (tip at top-left).
    let arrow: [(f32, f32); 7] = [
        (0.00, 0.00),
        (0.00, 0.72),
        (0.19, 0.55),
        (0.30, 0.80),
        (0.40, 0.755),
        (0.285, 0.515),
        (0.54, 0.515),
    ];
    let box_size = big * 0.52;
    let box_x = big * 0.26;
    let box_y = big * 0.22;

    let mut buf = vec![0u8; (size * size * 4) as usize];

    for oy in 0..size {
        for ox in 0..size {
            let (mut r, mut g, mut b, mut a) = (0f32, 0f32, 0f32, 0f32);
            for sy in 0..SS {
                for sx in 0..SS {
                    let px = (ox * SS + sx) as f32 + 0.5;
                    let py = (oy * SS + sy) as f32 + 0.5;
                    let (sr, sg, sb, sa) =
                        sample(px, py, big, radius, &arrow, box_x, box_y, box_size);
                    r += sr;
                    g += sg;
                    b += sb;
                    a += sa;
                }
            }
            let n = (SS * SS) as f32;
            let idx = ((oy * size + ox) * 4) as usize;
            buf[idx] = (r / n) as u8;
            buf[idx + 1] = (g / n) as u8;
            buf[idx + 2] = (b / n) as u8;
            buf[idx + 3] = (a / n) as u8;
        }
    }
    buf
}

#[allow(clippy::too_many_arguments)]
fn sample(
    px: f32,
    py: f32,
    big: f32,
    radius: f32,
    arrow: &[(f32, f32); 7],
    box_x: f32,
    box_y: f32,
    box_size: f32,
) -> (f32, f32, f32, f32) {
    // Rounded-square signed distance: negative = inside.
    let half = big / 2.0;
    let qx = (px - half).abs() - (half - radius);
    let qy = (py - half).abs() - (half - radius);
    let outside = (qx.max(0.0).powi(2) + qy.max(0.0).powi(2)).sqrt();
    let d = outside + qx.max(qy).min(0.0) - radius;
    if d >= 0.0 {
        return (0.0, 0.0, 0.0, 0.0); // transparent corner
    }

    // Vertical indigo gradient background.
    let t = (py / big).clamp(0.0, 1.0);
    let bg = (
        lerp(108.0, 74.0, t),
        lerp(99.0, 67.0, t),
        lerp(255.0, 208.0, t),
    );

    // White arrow on top.
    let nx = (px - box_x) / box_size;
    let ny = (py - box_y) / box_size;
    if point_in_poly(nx, ny, arrow) {
        return (250.0, 250.0, 255.0, 255.0);
    }

    (bg.0, bg.1, bg.2, 255.0)
}

fn lerp(a: f32, b: f32, t: f32) -> f32 {
    a + (b - a) * t
}

fn point_in_poly(x: f32, y: f32, poly: &[(f32, f32)]) -> bool {
    let mut inside = false;
    let mut j = poly.len() - 1;
    for i in 0..poly.len() {
        let (xi, yi) = poly[i];
        let (xj, yj) = poly[j];
        if ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi) {
            inside = !inside;
        }
        j = i;
    }
    inside
}
