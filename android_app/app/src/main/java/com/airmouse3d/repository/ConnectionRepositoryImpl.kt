package com.airmouse3d.repository

import com.airmouse3d.model.MotionSample
import com.airmouse3d.model.PcAddress
import com.airmouse3d.net.UdpMotionClient
import com.airmouse3d.utils.Constants
import com.airmouse3d.utils.PcAddressStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepositoryImpl @Inject constructor(
    private val pcAddressStore: PcAddressStore,
    private val udpClient: UdpMotionClient,
) : ConnectionRepository {

    override val pcAddress: Flow<PcAddress?> = pcAddressStore.pcAddress

    override val isReachable: Flow<Boolean> = flow {
        while (true) {
            emit(udpClient.millisSinceLastAck() <= Constants.CONNECTION_ACK_TIMEOUT_MS)
            delay(500)
        }
    }

    override suspend fun pairWith(address: PcAddress) {
        pcAddressStore.save(address)
        udpClient.connect(address)
    }

    override suspend fun forgetPc() {
        udpClient.disconnect()
        pcAddressStore.clear()
    }

    override suspend fun sendMotion(sample: MotionSample) {
        udpClient.send(sample)
    }

    override suspend fun ensureConnected(): PcAddress? {
        val address = pcAddressStore.pcAddress.first() ?: return null
        udpClient.connect(address)
        return address
    }
}
