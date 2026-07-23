use std::net::UdpSocket;

/// Finds this machine's LAN IPv4 address without adding a dependency: briefly "connect" a UDP
/// socket to a public address (no packet is actually sent for UDP connect -- it only asks the
/// OS routing table which local interface/IP would be used) and read back the local endpoint.
pub fn local_ipv4() -> Option<String> {
    let socket = UdpSocket::bind("0.0.0.0:0").ok()?;
    socket.connect("8.8.8.8:80").ok()?;
    let addr = socket.local_addr().ok()?;
    Some(addr.ip().to_string())
}
