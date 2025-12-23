package com.vincent.transfercloud.core.model

import com.vincent.transfercloud.utils.getNetworkInterfaces

data class NetworkConfig(
	var host: String = "localhost",
	var port: Int = 9090
)

data class TransferConfig(
	var localAddress: String = getNetworkInterfaces().first().toString().removePrefix("/"),
	var discoveryPort: Int = 16789
)