package com.io.alquilame

import org.junit.Test
import org.junit.Assert.*

/**
 * Simple unit tests for MainActivity - focused on business logic
 */
class SimpleMainActivityTest {

    @Test
    fun mainActivity_classExists() {
        val clazz = MainActivity::class.java
        assertNotNull(clazz)
        assertEquals("MainActivity", clazz.simpleName)
    }

    @Test
    fun mainActivity_hasCorrectPackage() {
        val clazz = MainActivity::class.java
        assertEquals("com.io.alquilame", clazz.packageName)
    }

    @Test
    fun mainActivity_extendsAppCompatActivity() {
        val clazz = MainActivity::class.java
        assertTrue(androidx.appcompat.app.AppCompatActivity::class.java.isAssignableFrom(clazz))
    }
}