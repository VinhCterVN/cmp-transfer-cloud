package com.vincent.transfercloud.core.plugins

import com.vincent.transfercloud.data.schema.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.xml.crypto.Data

fun Application.configDatabase() {
	Database.connect(
		url = "jdbc:postgresql://ep-quiet-term-a1luua4r-pooler.ap-southeast-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_B4Ar0OgtMIJR&sslmode=require&channelBinding=require",
		driver = "org.postgresql.Driver",
	)

	transaction {
		SchemaUtils.create(Users, Folders, Files, Shares, Activities)
	}
}