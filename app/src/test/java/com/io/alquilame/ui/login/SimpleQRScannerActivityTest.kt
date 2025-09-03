package com.io.alquilame.ui.login

import com.io.alquilame.BuildConfig
import org.junit.Test
import org.junit.Assert.*
import java.security.MessageDigest

/**
 * Simple unit tests for QRScannerActivity - focused on business logic
 */
class SimpleQRScannerActivityTest {

    @Test
    fun qrScannerActivity_classExists() {
        val clazz = QRScannerActivity::class.java
        assertNotNull(clazz)
        assertEquals("QRScannerActivity", clazz.simpleName)
    }

    @Test
    fun qrScannerActivity_hasCorrectPackage() {
        val clazz = QRScannerActivity::class.java
        assertEquals("com.io.alquilame.ui.login", clazz.packageName)
    }

    @Test
    fun qrScannerActivity_extendsAppCompatActivity() {
        val clazz = QRScannerActivity::class.java
        assertTrue(androidx.appcompat.app.AppCompatActivity::class.java.isAssignableFrom(clazz))
    }

    @Test
    fun qrScannerActivity_hasCorrectConstants() {
        assertEquals("qr_result", QRScannerActivity.EXTRA_QR_RESULT)
        assertEquals("username", QRScannerActivity.EXTRA_USERNAME)
        assertEquals("api_key", QRScannerActivity.EXTRA_API_KEY)
    }

    @Test
    fun hashGeneration_shouldBeConsistent() {
        val tenantSlug = "test"
        val secretSlug = "secret" 
        val input = "$tenantSlug|$secretSlug|${BuildConfig.SECRET_STRING_LOGIN}"
        
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val hash1 = bytes.joinToString("") { "%02x".format(it) }
        
        // Generate same hash again
        val bytes2 = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val hash2 = bytes2.joinToString("") { "%02x".format(it) }
        
        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length) // SHA-256 produces 64 character hex string
    }

    @Test
    fun hashGeneration_shouldBeDifferentForDifferentInputs() {
        val input1 = "tenant1|secret1|${BuildConfig.SECRET_STRING_LOGIN}"
        val input2 = "tenant2|secret2|${BuildConfig.SECRET_STRING_LOGIN}"
        
        val bytes1 = MessageDigest.getInstance("SHA-256").digest(input1.toByteArray())
        val hash1 = bytes1.joinToString("") { "%02x".format(it) }
        
        val bytes2 = MessageDigest.getInstance("SHA-256").digest(input2.toByteArray())
        val hash2 = bytes2.joinToString("") { "%02x".format(it) }
        
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun buildConfig_secretStringLoginIsNotEmpty() {
        assertNotNull(BuildConfig.SECRET_STRING_LOGIN)
        assertTrue(BuildConfig.SECRET_STRING_LOGIN.isNotEmpty())
    }
}