package com.airmouse3d.net

import com.airmouse3d.model.MotionSample
import com.airmouse3d.model.PcAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the raw UDP socket to the PC receiver. One [DatagramSocket] does double duty as both
 * sender and receiver: since every outbound packet's source port is that socket's ephemeral
 * local port, the PC's ack reply (sent back to whichever address a packet arrived from) lands
 * on the exact same socket without any separate listening bind -- the standard UDP
 * request/reply pattern.
 *
 * Ack recency (see [millisSinceLastAck]) is the only meaningful signal that the PC receiver
 * is actually alive and hearing us -- UDP itself gives no delivery confirmation, so without
 * it a dead or unreachable PC would look identical to a working one.
 */
@Singleton
class UdpMotionClient @Inject constructor() {
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { encodeDefaults = true }

    @Volatile private var socket: DatagramSocket? = null
    @Volatile private var target: InetSocketAddress? = null
    @Volatile private var lastAckAtMillis: Long = 0L
    private var receiveJob: Job? = null

    @Synchronized
    fun connect(address: PcAddress) {
        disconnect()
        val newSocket = DatagramSocket()
        socket = newSocket
        target = InetSocketAddress(address.host, address.port)
        receiveJob = clientScope.launch { receiveLoop(newSocket) }
    }

    private fun receiveLoop(socket: DatagramSocket) {
        val buffer = ByteArray(512)
        while (!socket.isClosed) {
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                lastAckAtMillis = System.currentTimeMillis()
            } catch (e: IOException) {
                if (socket.isClosed) return
            }
        }
    }

    suspend fun send(sample: MotionSample) {
        val sock = socket ?: return
        val addr = target ?: return
        withContext(Dispatchers.IO) {
            try {
                val bytes = json.encodeToString(sample).encodeToByteArray()
                sock.send(DatagramPacket(bytes, bytes.size, addr))
            } catch (_: IOException) {
                // Best-effort: a dropped/failed send just means one frame of motion is lost.
                // Callers read ack recency to know whether the link is actually working.
            }
        }
    }

    fun millisSinceLastAck(): Long =
        if (lastAckAtMillis == 0L) Long.MAX_VALUE else System.currentTimeMillis() - lastAckAtMillis

    @Synchronized
    fun disconnect() {
        receiveJob?.cancel()
        receiveJob = null
        socket?.close()
        socket = null
        target = null
        lastAckAtMillis = 0L
    }
}
