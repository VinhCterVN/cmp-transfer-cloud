package com.vincent.transfercloud.utils

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

fun getNetworkInterfaces(): List<InetAddress> {
	val result = mutableListOf<InetAddress>()
	val nis = NetworkInterface.getNetworkInterfaces()
	for (ni in nis) {
		if (!ni.isUp || ni.isLoopback) continue
		if (ni.inetAddresses.hasMoreElements().not()) continue
		if (ni.displayName.contains("VirtualBox", ignoreCase = true)) continue
		if (ni.displayName.contains("VMware", ignoreCase = true)) continue
		if (ni.displayName.contains("Hyper-V", ignoreCase = true)) continue
		val addresses = ni.inetAddresses
		for (address in addresses) {
			if (address is Inet4Address)
				result.add(address)
		}
	}
	return result.toList()
}
