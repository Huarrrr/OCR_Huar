package com.ybculture.pangold.util

import com.alibaba.fastjson.util.IOUtils
import java.io.ByteArrayOutputStream
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

object RSAUtil {
    private const val CHARSET = "UTF-8"
    private const val RSA_ALGORITHM = "RSA"
    private const val COMMON_RSA = "RSA/ECB/PKCS1Padding"
    fun createKeys(keySize: Int): Map<String, String> {
        // 为RSA算法创建一个KeyPairGenerator对象
        val kpg: KeyPairGenerator = try {
            KeyPairGenerator.getInstance(RSA_ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalArgumentException("No such algorithm-->[$RSA_ALGORITHM]")
        }
        // 初始化KeyPairGenerator对象,密钥长度
        kpg.initialize(keySize)
        // 生成密匙对
        val keyPair = kpg.generateKeyPair()
        // 得到公钥
        val publicKey: Key = keyPair.public
        val publicKeyStr = AESBase64.encode(publicKey.encoded)
        // 得到私钥
        val privateKey: Key = keyPair.private
        val privateKeyStr = AESBase64.encode(privateKey.encoded)
        val keyPairMap: MutableMap<String, String> =
            HashMap()
        keyPairMap["publicKey"] = publicKeyStr
        keyPairMap["privateKey"] = privateKeyStr
        return keyPairMap
    }

    /**
     * 得到公钥
     *
     * @param publicKey 密钥字符串（经过base64编码）
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getPublicKey(publicKey: String?): RSAPublicKey {
        // 通过X509编码的Key指令获得公钥对象
        val keyFactory =
            KeyFactory.getInstance(RSA_ALGORITHM)
        val x509KeySpec =
            X509EncodedKeySpec(AESBase64.decode(publicKey))
        return keyFactory.generatePublic(x509KeySpec) as RSAPublicKey
    }

    /**
     * 得到私钥
     *
     * @param privateKey 密钥字符串（经过base64编码）
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getPrivateKey(privateKey: String?): RSAPrivateKey {
        // 通过PKCS#8编码的Key指令获得私钥对象
        val keyFactory =
            KeyFactory.getInstance(RSA_ALGORITHM)
        val pkcs8KeySpec =
            PKCS8EncodedKeySpec(AESBase64.decode(privateKey))
        return keyFactory.generatePrivate(pkcs8KeySpec) as RSAPrivateKey
    }

    /**
     * 公钥加密
     *
     * @param data
     * @param publicKey
     * @return
     */
    fun publicEncrypt(
        data: String,
        publicKey: RSAPublicKey
    ): String {
        return try {
            val cipher = Cipher.getInstance(COMMON_RSA)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            AESBase64.encode(
                rsaSplitCodec(
                    cipher,
                    Cipher.ENCRYPT_MODE,
                    data.toByteArray(charset(CHARSET)),
                    publicKey.modulus.bitLength()
                )
            )
        } catch (e: Exception) {
            throw RuntimeException("加密字符串[$data]时遇到异常", e)
        }
    }

    /**
     * 私钥解密
     *
     * @param data
     * @param privateKey
     * @return
     */
    fun privateDecrypt(
        data: String,
        privateKey: RSAPrivateKey
    ): String {
        return try {
            val cipher = Cipher.getInstance(COMMON_RSA)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            String(
                rsaSplitCodec(
                    cipher, Cipher.DECRYPT_MODE, AESBase64.decode(data),
                    privateKey.modulus.bitLength()
                ), charset(CHARSET)
            )
        } catch (e: Exception) {
            throw RuntimeException("解密字符串[$data]时遇到异常", e)
        }
    }

    /**
     * 私钥加密
     *
     * @param data
     * @param privateKey
     * @return
     */
    fun privateEncrypt(
        data: String,
        privateKey: RSAPrivateKey
    ): String {
        return try {
            val cipher = Cipher.getInstance(COMMON_RSA)
            cipher.init(Cipher.ENCRYPT_MODE, privateKey)
            AESBase64.encode(
                rsaSplitCodec(
                    cipher,
                    Cipher.ENCRYPT_MODE,
                    data.toByteArray(charset(CHARSET)),
                    privateKey.modulus.bitLength()
                )
            )
        } catch (e: Exception) {
            throw RuntimeException("加密字符串[$data]时遇到异常", e)
        }
    }

    /**
     * 公钥解密
     *
     * @param data
     * @param publicKey
     * @return
     */
    fun publicDecrypt(
        data: String,
        publicKey: RSAPublicKey
    ): String {
        return try {
            val cipher = Cipher.getInstance(COMMON_RSA)
            cipher.init(Cipher.DECRYPT_MODE, publicKey)
            String(
                rsaSplitCodec(
                    cipher, Cipher.DECRYPT_MODE, AESBase64.decode(data),
                    publicKey.modulus.bitLength()
                ), charset(CHARSET)
            )
        } catch (e: Exception) {
            throw RuntimeException("解密字符串[$data]时遇到异常", e)
        }
    }

    private fun rsaSplitCodec(
        cipher: Cipher,
        opcode: Int,
        data: ByteArray,
        keySize: Int
    ): ByteArray {
        var maxBlock = 0
        maxBlock = if (opcode == Cipher.DECRYPT_MODE) {
            keySize / 8
        } else {
            keySize / 8 - 11
        }
        val out = ByteArrayOutputStream()
        var offSet = 0
        var buff: ByteArray
        var i = 0
        try {
            while (data.size > offSet) {
                buff = if (data.size - offSet > maxBlock) {
                    cipher.doFinal(data, offSet, maxBlock)
                } else {
                    cipher.doFinal(data, offSet, data.size - offSet)
                }
                out.write(buff, 0, buff.size)
                i++
                offSet = i * maxBlock
            }
        } catch (e: Exception) {
            throw RuntimeException("加解密阀值为[$maxBlock]的数据时发生异常", e)
        }
        val resultData = out.toByteArray()
        IOUtils.close(out)
        return resultData
    }
}