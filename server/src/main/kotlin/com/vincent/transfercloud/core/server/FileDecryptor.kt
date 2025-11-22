package com.vincent.transfercloud.core.server

import kotlinx.io.files.FileNotFoundException
import java.io.File
import java.io.FileInputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object FileDecryptor {
	private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
	private const val IV_SIZE = 16

	/**
	 * Đọc file từ ổ đĩa, giải mã và trả về ByteArray gốc
	 */
	fun loadAndDecryptFile(storagePath: String, secretKey: SecretKey): ByteArray {
		val storageDir = File(System.getProperty("user.dir"), "storage").apply { if (!exists()) mkdirs() }
		val encryptedFile = File(storageDir, storagePath)
		if (!encryptedFile.exists()) {
			throw FileNotFoundException("Không tìm thấy file: ${encryptedFile.absolutePath}")
		}
		val fileBytes = FileInputStream(encryptedFile).use { it.readBytes() }
		if (fileBytes.size < IV_SIZE) {
			throw IllegalStateException("File bị hỏng hoặc không đúng định dạng")
		}
		val iv = fileBytes.copyOfRange(0, IV_SIZE)
		val encryptedContent = fileBytes.copyOfRange(IV_SIZE, fileBytes.size)
		val ivSpec = IvParameterSpec(iv)
		val cipher = Cipher.getInstance(TRANSFORMATION)
		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
		return cipher.doFinal(encryptedContent)
	}
}