package com.vincent.transfercloud.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.*


@Serializable
sealed class Request(
	val type: String
) {
	@Serializable
	data class Login(
		val username: String,
		val password: String
	) : Request("login")

	@Serializable
	data class Register(
		val username: String,
		val password: String,
		val email: String
	) : Request("register")

	@Serializable
	data class SendEmail(
		val from: String,
		val to: String,
		val title: String,
		val content: String
	) : Request("sendEmail")

	@Serializable
	data class GetInbox(
		val username: String
	) : Request("getInbox")

	@Serializable
	data class GetEmailDetail(
		val emailId: String
	) : Request("getEmailDetail")

	@Serializable
	data class GetSent(
		val username: String
	) : Request("getSent")

	@Serializable
	data class DeleteEmail(
		val emailId: String
	)
}

@Serializable
sealed class Response {
	@Serializable
	data class LoginResponse(
		val success: Boolean,
		val message: String,
		val email: String
	) : Response()

	@Serializable
	data class RegisterResponse(
		val success: Boolean,
		val message: String
	) : Response()

	@Serializable
	data class ErrorResponse(
		val message: String
	) : Response()

	@Serializable
	data class SuccessResponse(
		val message: String,
		val data: JsonElement? = null
	) : Response()

	@Serializable
	data class GetInboxResponse(
		val emails: List<Email>
	) : Response()

	@Serializable
	data class GetEmailDetailResponse(
		val success: Boolean,
		val message: String,
		val email: Email? = null
	) : Response()

	@Serializable
	data class SendEmailResponse(
		val success: Boolean,
		val message: String
	) : Response()
}

@Serializable
data class Email(
	val id: String = UUID.randomUUID().toString(),
	val from: String,
	val to: String,
	val title: String,
	val content: String,
	val createdAt: Long = System.currentTimeMillis(),
)