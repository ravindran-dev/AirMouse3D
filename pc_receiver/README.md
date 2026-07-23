# AirMouse3D — PC Receiver

A small **Windows desktop app** that receives motion from the AirMouse3D phone app over the local
Wi-Fi network (direct UDP — no cloud, no internet) and drives the PC's mouse cursor.

It's a real windowed app, not a terminal program: double-click it and it opens a window showing a
pairing **QR code**, the connection **address**, a **Copy** button, a live **Connected / Waiting**
status, and packet stats. Close it to stop.

## Download & install (like a normal app)

The app installs per-user — **no admin rights needed** — and adds Start Menu + Desktop shortcuts
and an entry in *Settings ▸ Apps* (so it uninstalls like any other program).

1. Build the exe (one-time, needs the Rust toolchain):
   ```
   cargo build --release
   ```
   This produces `target/release/AirMouse3D-Receiver.exe`.
2. Install it: double-click **`installer/Install.bat`** (or run
   `powershell -ExecutionPolicy Bypass -File installer/install.ps1 -Launch`).
3. Launch **AirMouse3D Receiver** from the Start Menu or Desktop.

To remove it: double-click **`installer/Uninstall.bat`**, or use *Settings ▸ Apps ▸ AirMouse3D
Receiver ▸ Uninstall*.

> Prefer a single downloadable `Setup.exe`? `installer/installer.nsi` is an optional
> [NSIS](https://nsis.sourceforge.io/) script — run `makensis installer.nsi` (with NSIS
> installed) to produce `AirMouse3D-Setup.exe`. The `.ps1`/`.bat` installer above needs no extra
> tooling, so it's the default path.

## Using it

1. Open **AirMouse3D Receiver** on the PC. Its window shows the QR code + address.
2. On the phone, open the AirMouse3D app → **Scan QR Code** (or type the address shown).
3. Tap **Start Air Mouse**, hold the phone flat like a mouse, and tilt to move the cursor.

The window shows **● Connected — `<phone ip>`** once packets arrive, plus live packet count, rate,
and uptime. If the phone drops off it shows **Waiting to reconnect…** and recovers on its own.

## How it works

```
Phone (UDP motion packets) ──► AirMouse3D-Receiver.exe ──► OS mouse APIs (enigo) ──► cursor
                            ◄── (ack packets) ──────────
```

- **`src/main.rs`** — launches the GUI (no console window in release builds), sets the window/app
  icon, spawns the background UDP server.
- **`src/gui.rs`** — the `egui`/`eframe` window: QR image, address + copy, status pill, live stats.
- **`src/server.rs`** — background thread running the async UDP receive loop, independent of the
  GUI; publishes live status the GUI reads each frame. Surfaces a clear error in-window (instead
  of crashing) if the port is already in use or cursor control can't initialize.
- **`src/cursor/`** — `mapper.rs` maps the phone's `dx/dy` to screen axes; `controller.rs` applies
  movement, clicks (left/right/middle), and scroll via `enigo`.
- **`src/net/discovery.rs`** — detects this machine's LAN IP for the QR code / address.
- **`src/icon.rs`** — the app icon, drawn procedurally in code (no binary asset); `build.rs` bakes
  it into a multi-size `.ico` embedded in the exe, and `main.rs` reuses it for the live window icon.

### Movement is filtered once, on the phone

All the motion shaping — adaptive **One-Euro filtering**, **pointer acceleration**, dead zone,
shake rejection, sensitivity — happens on the phone (see `../android_app`, `MotionProcessor`). The
receiver deliberately does **no** second smoothing pass: the `dx/dy` in each packet are final,
ready-to-apply pixel deltas. Re-filtering here would just stack a second filter's lag on top and
make the cursor feel mushy. `mapper.rs` only maps axes and applies one safety clamp.

## Notes

- Phone and PC must be on the **same Wi-Fi network**. No internet connection is required.
- The exe-embedded icon step in `build.rs` is best-effort: if the Windows resource compiler isn't
  available it's skipped with a warning and the build still succeeds (the live window icon, set at
  runtime, is unaffected).
