package com.example.updater

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun `isNewerVersion returns true when remote patch is higher`() {
        assertTrue(AppUpdateManager.isNewerVersion("1.0.0", "1.0.1"))
        assertTrue(AppUpdateManager.isNewerVersion("1.0.0", "v1.0.1"))
    }

    @Test
    fun `isNewerVersion returns true when remote minor is higher`() {
        assertTrue(AppUpdateManager.isNewerVersion("1.0.5", "1.1.0"))
        assertTrue(AppUpdateManager.isNewerVersion("1.0.5", "v1.1.0"))
    }

    @Test
    fun `isNewerVersion returns true when remote major is higher`() {
        assertTrue(AppUpdateManager.isNewerVersion("1.9.9", "2.0.0"))
        assertTrue(AppUpdateManager.isNewerVersion("1.9.9", "v2.0.0"))
    }

    @Test
    fun `isNewerVersion returns false when remote is same or older`() {
        assertFalse(AppUpdateManager.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.0", "v1.0.0"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.1", "1.0.0"))
        assertFalse(AppUpdateManager.isNewerVersion("2.0.0", "v1.9.9"))
    }

    @Test
    fun `isNewerVersion handles irregular version segments gracefully`() {
        assertTrue(AppUpdateManager.isNewerVersion("1.0", "1.0.1"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.1", "1.0"))
    }
}
