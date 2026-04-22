package io.github.sgpublic.dormnet.targets

import java.net.Inet4Address
import java.net.NetworkInterface

internal fun findCquptNetworkInfo(): CquptNetworkInfo {
    val interfaces = NetworkInterface.getNetworkInterfaces().toList()
    for (networkInterface in interfaces) {
        if (!networkInterface.isUp || networkInterface.isLoopback) {
            continue
        }
        val address = networkInterface.inetAddresses.toList()
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress }
            ?: continue
        val mac = networkInterface.hardwareAddress?.joinToString(":") {
            "%02x".format(it)
        }
        return CquptNetworkInfo(
            ip = address.hostAddress,
            mac = mac,
        )
    }
    return CquptNetworkInfo(ip = null, mac = null)
}
