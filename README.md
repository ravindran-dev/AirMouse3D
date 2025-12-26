# 3D Air Mouse Using Smartphone Motion Sensors

## Project Overview

The **3D Air Mouse** project enables a smartphone to act as a wireless mouse for a PC using built-in motion sensors. Instead of traditional mouse hardware, the system captures hand movements via the phone’s gyroscope and accelerometer and translates them into real-time cursor movements and mouse actions on a computer.

The project uses a **session-based pairing mechanism** and **Firebase Realtime Database** for communication between the mobile device and the PC. A Rust-based PC receiver listens to motion updates and controls the cursor using operating system–level APIs.



## Objectives

* Replace traditional mouse input with motion-based control
* Enable touch-free and portable PC interaction
* Implement real-time motion data synchronization
* Design a session-based pairing system without Bluetooth
* Demonstrate cross-platform system-level programming



## Problem Statement

Conventional mouse devices are not always practical in scenarios such as:

* Presentations and remote control environments
* Accessibility for users with physical limitations
* Situations where touch-based input is inconvenient

This project addresses these limitations by using a smartphone as an intuitive air mouse.

---

## System Architecture

### High-Level Flow

```
Smartphone Sensors
        ↓
Firebase Realtime Database (Session ID)
        ↓
PC Receiver (Rust)
        ↓
OS-Level Mouse Control
```

### Key Design Decisions

* No Bluetooth or direct PC–phone connection
* Firebase used as a real-time communication bridge
* Session ID used to pair one phone with one PC
* Relative cursor movement for smooth control



## System Methodology

1. The mobile application reads gyroscope and accelerometer data.
2. Raw sensor data is filtered to remove noise and small fluctuations.
3. Filtered motion values are uploaded to Firebase under a unique session ID.
4. The PC receiver listens to the same session ID in real time.
5. Motion values are mapped to cursor movement and mouse actions.
6. OS-level APIs execute cursor movement and clicks.



## Team Roles & Responsibilities

### 1. Mobile Motion Sensing Engineer

* Access phone sensors (gyroscope, accelerometer)
* Filter and normalize motion data
* Control sampling rate and stability

### 2. Mobile App & Session Pairing Engineer

* Design Android UI
* Implement session ID input and validation
* Connect mobile app to Firebase

### 3. Firebase & Realtime Communication Engineer

* Configure Firebase Realtime Database
* Design database schema
* Handle session-based data isolation

### 4. PC Receiver & Cursor Control Engineer

* Implement PC receiver in Rust
* Read motion data from Firebase
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
* APIs: Android Sensor API

### Cloud / Backend

* Firebase Realtime Database
* JSON-based data exchange

### PC Receiver

* Language: Rust
* Networking: `reqwest` (Firebase REST API)
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
├── src/
│   ├── main.rs
│   ├── config.rs
│   ├── firebase/
│   ├── model/
│   ├── cursor/
│   └── utils/
└── README.md
```



##  Testing & Validation

### Testing Strategy

* Independent testing using mock Firebase data
* No dependency on mobile app during initial testing

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

* Requires active internet connection (Firebase-based)
* Latency depends on network conditions
* Wayland restricts mouse emulation on Linux



## Future Scope

* Offline mode using local Wi-Fi sockets or Bluetooth
* Support for Wayland input protocols
* Additional gestures (scroll, drag, zoom)
* iOS application support
* AI-based gesture recognition



## Conclusion

The 3D Air Mouse project demonstrates an innovative and practical alternative to traditional mouse input by leveraging smartphone sensors and real-time cloud communication. The system highlights the integration of mobile sensing, cloud synchronization, and system-level programming, making it suitable for educational, assistive, and real-world interaction scenarios.



## License

This project is developed for academic and educational purposes.



