package com.vincent.transfercloud.core.server

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object FileEncryptor {
	private const val ALGORITHM = "AES"
	private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
	private const val IV_SIZE = 16

	/**
	 * Hàm nhận ByteArray, mã hóa và lưu xuống file
	 * * @param data: Dữ liệu file gốc dưới dạng ByteArray
	 * @param fileName Tên file muốn lưu (ví dụ: "document.pdf")
	 * @param secretKey Khóa bí mật dùng để mã hóa
	 * @return String: Đường dẫn tuyệt đối của file đã lưu
	 */
	fun saveEncryptedFile(data: ByteArray, fileName: String, ownerId: String, secretKey: SecretKey): String {
		val newName = fileNameWithoutExtensionFromPath(fileName)
		val storageDir = File(System.getProperty("user.dir"), "storage").apply { if (!exists()) mkdirs() }
		val ownerDir = File(storageDir, ownerId).apply { if (!exists()) mkdirs() }
		val outputFile = File(ownerDir, "${System.currentTimeMillis()}_$newName.enc")

		encryptStream(data.inputStream(), outputFile, secretKey)
		println("Encrypted File ensured at: ${outputFile.absolutePath}")
		return outputFile.name
	}

	fun encryptStream(inputStream: InputStream, outputFile: File, secretKey: SecretKey) {
		val iv = ByteArray(IV_SIZE)
		SecureRandom().nextBytes(iv)
		val ivSpec = IvParameterSpec(iv)
		val cipher = Cipher.getInstance(TRANSFORMATION)
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
		FileOutputStream(outputFile).use { fos ->
			fos.write(iv)
			CipherOutputStream(fos, cipher).use { cos ->
				inputStream.copyTo(cos)
			}
		}
	}
}