package com.airmouse3d.model

/** The PC receiver's address on the local network, learned once from its pairing QR code. */
data class PcAddress(val host: String, val port: Int) {
    fun display(): String = "$host:$port"

    companion object {
        /**
         * Parses the plain-text payload the PC's QR code encodes: `host:port`, or just `host`
         * (in which case [defaultPort] is assumed). Accepts raw IPv4 as well as bracketed IPv6
         * (`[::1]:7890`) since `pc_receiver` may end up bound to either.
         */
        fun parse(raw: String, defaultPort: Int): PcAddress? {
            val text = raw.trim()
            if (text.isEmpty()) return null

            if (text.startsWith("[")) {
                val closeBracket = text.indexOf(']')
                if (closeBracket == -1) return null
                val host = text.substring(1, closeBracket)
                val remainder = text.substring(closeBracket + 1)
                val port = remainder.removePrefix(":").toIntOrNull() ?: defaultPort
                return PcAddress(host, port)
            }

            val lastColon = text.lastIndexOf(':')
            return if (lastColon == -1) {
                PcAddress(text, defaultPort)
            } else {
                val host = text.substring(0, lastColon)
                val port = text.substring(lastColon + 1).toIntOrNull() ?: return null
                PcAddress(host, port)
            }
        }
    }
}
