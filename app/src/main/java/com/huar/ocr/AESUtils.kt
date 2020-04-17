package com.ybculture.pangold.util
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtils {
    const val key = "128bitslength*@#" //key 16bit
    private const val IV_STRING = "A-16-Byte-String" //偏移量 16bit
    private const val charset = "UTF-8"
    fun encrypt(content: String): String? {
        try {
            val contentBytes = content.toByteArray(charset(charset))
            val keyBytes = key.toByteArray(charset(charset))
            val encryptedBytes = aesEncryptBytes(contentBytes, keyBytes)
            return AESBase64.encode(encryptedBytes!!)
//            return byteToHex(encryptedBytes!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decrypt(content: String?): String? {
        try {
            val encryptedBytes = AESBase64.decode(content!!)
//            val encryptedBytes = deHex(content!!)
            val keyBytes = key.toByteArray(charset(charset))
            val decryptedBytes =
                aesDecryptBytes(encryptedBytes, keyBytes)
            return String(decryptedBytes!!, charset(charset))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun aesEncryptBytes(
        contentBytes: ByteArray,
        keyBytes: ByteArray
    ): ByteArray? {
        try {
            return cipherOperation(
                contentBytes,
                keyBytes,
                Cipher.ENCRYPT_MODE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun aesDecryptBytes(
        contentBytes: ByteArray,
        keyBytes: ByteArray
    ): ByteArray? {
        try {
            return cipherOperation(
                contentBytes,
                keyBytes,
                Cipher.DECRYPT_MODE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(Exception::class)
    private fun cipherOperation(
        contentBytes: ByteArray,
        keyBytes: ByteArray,
        mode: Int
    ): ByteArray {
        val secretKey =
            SecretKeySpec(keyBytes, "AES")
        val initParam = IV_STRING.toByteArray(charset(charset))
        val ivParameterSpec =
            IvParameterSpec(initParam)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(mode, secretKey, ivParameterSpec)
        return cipher.doFinal(contentBytes)
    }
}