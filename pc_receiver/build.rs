//! Windows build step: generate a multi-size `.ico` from the procedural icon (src/icon.rs) and
//! embed it plus product metadata into the exe. Everything here is best-effort — if the Windows
//! resource compiler isn't available on the build machine, we emit a warning and continue, so
//! the app still builds (it just won't have the file-explorer icon; the live window icon set at
//! runtime in main.rs doesn't depend on any of this).

// The icon-drawing routine, shared verbatim with the runtime via `mod icon` in main.rs.
#[allow(dead_code)]
mod icon {
    include!("src/icon.rs");
}

fn main() {
    println!("cargo:rerun-if-changed=src/icon.rs");
    println!("cargo:rerun-if-changed=build.rs");

    #[cfg(windows)]
    embed_windows_resources();
}

#[cfg(windows)]
fn embed_windows_resources() {
    let out_dir = std::env::var("OUT_DIR").unwrap();
    let ico_path = std::path::PathBuf::from(&out_dir).join("app_icon.ico");

    if let Err(e) = write_ico(&ico_path) {
        println!("cargo:warning=Could not generate app icon: {e}");
        return;
    }

    let mut res = winresource::WindowsResource::new();
    res.set_icon(ico_path.to_str().unwrap());
    res.set("ProductName", "AirMouse3D Receiver");
    res.set("FileDescription", "AirMouse3D — use your phone as a wireless mouse");
    res.set("CompanyName", "AirMouse3D");
    res.set("LegalCopyright", "AirMouse3D");

    if let Err(e) = res.compile() {
        println!("cargo:warning=Could not embed exe icon/metadata (resource compiler unavailable?): {e}");
    }
}

#[cfg(windows)]
fn write_ico(path: &std::path::Path) -> std::io::Result<()> {
    let mut dir = ico::IconDir::new(ico::ResourceType::Icon);
    for size in [16u32, 24, 32, 48, 64, 128, 256] {
        let rgba = icon::rgba(size);
        let image = ico::IconImage::from_rgba_data(size, size, rgba);
        let entry = ico::IconDirEntry::encode(&image)
            .map_err(|e| std::io::Error::new(std::io::ErrorKind::Other, e))?;
        dir.add_entry(entry);
    }
    let file = std::fs::File::create(path)?;
    dir.write(file)
}
