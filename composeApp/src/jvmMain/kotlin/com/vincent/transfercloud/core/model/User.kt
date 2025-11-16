package com.vincent.transfercloud.core.model

data class User(
	val name: String,
	val photoUrl: String = "https://i.pravatar.cc/150?u=$name"
)