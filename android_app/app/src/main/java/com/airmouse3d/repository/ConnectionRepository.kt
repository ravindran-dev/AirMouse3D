package com.airmouse3d.repository

import com.airmouse3d.model.MotionSample
import com.airmouse3d.model.PcAddress
import kotlinx.coroutines.flow.Flow

/** Owns pairing with, and the live link to, the PC receiver over direct LAN UDP. */
interface ConnectionRepository {
    /** The currently paired PC, or null if the user hasn't scanned a pairing QR code yet. */
    val pcAddress: Flow<PcAddress?>

    /** Whether the PC has acked a packet recently enough to be considered live. */
    val isReachable: Flow<Boolean>

    /** Saves [address] as the paired PC and (re)opens the UDP socket to it. */
    suspend fun pairWith(address: PcAddress)

    /** Forgets the paired PC and closes the socket. */
    suspend fun forgetPc()

    suspend fun sendMotion(sample: MotionSample)

    /** Opens the UDP socket to whichever PC is already paired, if any. */
    suspend fun ensureConnected(): PcAddress?
}
