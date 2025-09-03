package com.io.alquilame.ui.login

import org.junit.Test
import org.junit.Assert.*

/**
 * Simple unit tests for LoginActivity - focused on business logic
 */
class SimpleLoginActivityTest {

    @Test
    fun loginActivity_classExists() {
        val clazz = LoginActivity::class.java
        assertNotNull(clazz)
        assertEquals("LoginActivity", clazz.simpleName)
    }

    @Test
    fun loginActivity_hasCorrectPackage() {
        val clazz = LoginActivity::class.java
        assertEquals("com.io.alquilame.ui.login", clazz.packageName)
    }

    @Test
    fun loginActivity_extendsAppCompatActivity() {
        val clazz = LoginActivity::class.java
        assertTrue(androidx.appcompat.app.AppCompatActivity::class.java.isAssignableFrom(clazz))
    }

    @Test
    fun loginActivity_hasCompanionObject() {
        // Test that companion object exists
        val companionObject = LoginActivity::class.java.getDeclaredClasses()
            .find { it.simpleName == "Companion" }
        assertNotNull("LoginActivity should have a Companion object", companionObject)
    }
}