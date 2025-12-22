package com.vincent.transfercloud.utils

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

fun getLocalNetworkIp(): String? {
    return try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            if (intf.isLoopback || !intf.isUp || intf.displayName.contains("VMware") || intf.displayName.contains("VirtualBox")) continue

            val addresses = intf.inetAddresses
            for (address in addresses) {
                if (address is Inet4Address) {
                    println(address)

                }
            }
        }
        InetAddress.getLocalHost().hostAddress // Fallback
    } catch (e: Exception) {
        null
    }
}

fun main() {
	val ip = getLocalNetworkIp()
	println(ip)
}