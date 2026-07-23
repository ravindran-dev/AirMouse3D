# 3D Air Mouse Using Smartphone Motion Sensors

## Project Overview

The **3D Air Mouse** project enables a smartphone to act as a wireless mouse for a PC using built-in motion sensors. Instead of traditional mouse hardware, the system captures hand movements via the phone’s gyroscope and accelerometer and translates them into real-time cursor movements and mouse actions on a computer.

The project uses a **QR-code pairing mechanism** and a **direct UDP socket over the local Wi-Fi network** for communication between the mobile device and the PC — no cloud service, no internet connection required. A Rust-based PC receiver app (a real desktop window, not a terminal program) listens to motion updates and controls the cursor using operating system–level APIs.

> Earlier revisions of this project routed motion data through Firebase Realtime Database.
> That added a cloud round-trip to every single motion update, which made the cursor feel
> laggy and imprecise. The direct-UDP design below replaced it for exactly that reason.



## Objectives

* Replace traditional mouse input with motion-based control
* Enable touch-free and portable PC interaction
* Implement real-time motion data synchronization
* Design a session-based pairing system without Bluetooth
* Demonstrate cross-platform system-level programming



## Problem Statement

Conventional mouse devices are not always practical in scenarios such as:

* Presentations and remote control environments
* Accessibility for users with physical limitation
* Situations where touch-based input is inconvenient

This project addresses these limitations by using a smartphone as an intuitive air mouse.

---

## System Architecture

### High-Level Flow

```
Smartphone Sensors
        ↓
Direct UDP Socket (same Wi-Fi LAN, QR-code paired)
        ↓
PC Receiver (Rust)
        ↓
OS-Level Mouse Control
```

### Key Design Decisions

* No Bluetooth — a direct UDP socket over the local Wi-Fi network instead
* No cloud dependency: phone and PC talk directly, no internet required
* QR code (with a manual-entry fallback) pairs one phone with one PC
* An ack packet from the PC on every motion packet drives a genuine "Connected" indicator,
  since UDP itself gives no delivery confirmation
* Mouse-like movement, shaped once on the phone (never re-filtered on the PC, which used to
  double the perceived lag). The pipeline is a **One-Euro adaptive filter** (steady and jitter-free
  when aiming slowly, low-lag when flicking fast) plus **pointer acceleration** (slow tilts =
  fine control, fast tilts = quick screen traversal) — the same techniques dedicated air-mice and
  OS pointer curves use.
* Motion is gyroscope (tilt-rate) based, not accelerometer-based translation tracking: phone
  sensors alone can't track a phone sliding across a flat surface reliably (accelerometer-only
  dead reckoning drifts within seconds), whereas rotation rate is drift-free and precise. Tilting
  the phone right/left moves the cursor right/left; tilting the far edge down moves it down (the
  up/down axis is intentionally inverted). The axis and sign mapping is derived from Android's
  documented gyroscope coordinate system and locked in place by direction unit tests.



## System Methodology

1. The PC receiver opens a window, detects its local IP, binds a UDP port, and displays both a
   QR code and the plain-text address in that window — just double-click the app, no terminal.
2. The mobile application scans that QR code (or accepts manual entry) to learn the PC's address.
3. The mobile application reads gyroscope data and filters it (low-pass filter, dead zone, shake
   rejection, sensitivity scaling) to remove noise and small fluctuations.
4. Filtered motion values are sent directly to the PC over a UDP socket, roughly every 10ms.
5. The PC receiver maps each packet to cursor movement and mouse actions, and echoes back a small
   ack packet the phone uses to confirm the link is alive.
6. OS-level APIs execute cursor movement, clicks, and scroll.



## Team Roles & Responsibilities

### 1. Mobile Motion Sensing Engineer

* Access phone sensors (gyroscope, accelerometer)
* Filter and normalize motion data
* Control sampling rate and stability

### 2. Mobile App & Pairing Engineer

* Design Android UI
* Implement QR-code scanning (and manual-entry fallback) for pairing
* Persist the paired PC address and manage the UDP connection lifecycle

### 3. Networking Engineer

* Design the UDP packet schema shared by both sides
* Implement local IP detection and QR code generation on the PC side
* Handle ack-based reachability so a dead/unreachable link is detectable, not silent

### 4. PC Receiver & Cursor Control Engineer

* Implement PC receiver in Rust
* Read motion data from the UDP socket
* Map motion values to cursor movement
* Control mouse using OS-level APIs

### 5. System Integration & Documentation Lead

* Integrate all modules
* Perform testing and validation
* Prepare documentation and presentation



## Technology Stack

### Mobile Application

* Platform: Android
* Language: Kotlin
* APIs: Android Sensor API, CameraX + ML Kit (QR pairing)

### Networking

* Direct UDP socket over the local Wi-Fi network (no cloud, no internet required)
* JSON-based packet schema, shared exactly by both sides

### PC Receiver

* Language: Rust
* Desktop UI: `eframe` / `egui` (windowed app with an app icon; no terminal)
* Networking: `tokio::net::UdpSocket`
* Pairing: `qrcode` (rendered as an image in the app window)
* Async Runtime: `tokio`
* OS Control: `enigo`

### Tools

* Android Studio
* VS Code
* Git & GitHub

---

## PC Receiver Project Structure

```
pc_receiver/
├── Cargo.toml
├── build.rs           # bakes the procedural app icon into the exe (Windows)
├── installer/         # Install.bat / install.ps1 / package.ps1 / installer.nsi
├── src/
│   ├── main.rs        # launches the GUI (no console window in release builds)
│   ├── gui.rs         # eframe/egui window: QR image, address + copy, status, live stats
│   ├── server.rs      # background UDP receive loop, independent of the GUI thread
│   ├── icon.rs        # app icon, drawn procedurally in code (window + exe)
│   ├── config.rs
│   ├── net/           # local IP detection
│   ├── model/
│   ├── cursor/
│   └── utils/
└── README.md
```

The receiver is a normal, **installable** desktop app — not a terminal program. It has an app
icon, a polished dark window (QR image, address + Copy button, a live "Connected"/"Waiting"
status and packet stats), and a one-click installer that adds Start Menu + Desktop shortcuts like
any Windows program. Build it with `cargo build --release`, then double-click
`pc_receiver/installer/Install.bat`. See [`pc_receiver/README.md`](pc_receiver/README.md) for the
full download/install/uninstall steps (no admin rights required).

## Android App Project Structure

```
android_app/
├── app/src/main/java/com/airmouse3d/
│   ├── di/ · model/ · sensor/ · net/ · repository/
│   ├── service/ · viewmodel/ · navigation/ · ui/ · utils/
└── README.md
```

Kotlin, Jetpack Compose, MVVM + repository pattern, Hilt, and Coroutines/Flow, sending the exact
UDP packet schema `pc_receiver` listens for. See [`android_app/README.md`](android_app/README.md)
for the module's architecture diagram, pairing sequence diagram, and setup instructions.



##  Testing & Validation

### Testing Strategy

* Independent testing by sending mock UDP packets directly to `pc_receiver`
* No dependency on the mobile app during initial testing

### Test Cases

* Cursor movement for positive and negative motion values
* Dead-zone testing to prevent cursor jitter
* Gesture-based left and right click testing
* Handling missing or invalid data safely
* Long runtime stability testing

### Result

* Smooth cursor movement achieved
* Stable performance without crashes
* Acceptable latency for user interaction


## Cross-Platform Compatibility

| Operating System | Status      | Notes                              |
| ---------------- | ----------- | ---------------------------------- |
| Windows          | Supported | Fully functional                   |
| macOS            | Supported | Requires accessibility permissions |
| Linux (X11)      | Supported | Recommended for demo               |
| Linux (Wayland)  | Limited  | Input injection restricted         |



## Limitations

* Phone and PC must be on the same local Wi-Fi network (no internet needed, but no cross-network use either)
* Latency depends on Wi-Fi conditions, though typically far lower than a cloud round-trip
* Wayland restricts mouse emulation on Linux

## Future Scope

* Support for Wayland input protocols
* Drag gestures, zoom
* iOS application support
* AI-based gesture recognition



## Conclusion

The 3D Air Mouse project demonstrates an innovative and practical alternative to traditional mouse input by leveraging smartphone sensors and a direct, low-latency local-network connection. The system highlights the integration of mobile sensing, real-time networking, and system-level programming, making it suitable for educational, assistive, and real-world interaction scenarios.



## License

This project is developed for academic and educational purposes.



