package com.example.osstime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainactivityTest {

    @Test
    fun verificarContextoApp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.osstime", appContext.packageName)
    }

    @Test
    fun verificarContextoTest() {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        assertEquals("com.example.osstime.test", testContext.packageName)
    }

    @Test
    fun testSanidadBasico() {
        assertTrue("Este test debe pasar", true)
    }

}
