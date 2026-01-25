package com.foxelectronic.audioplayer.update

import org.junit.Assert.assertEquals
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun `test current version less than latest version with b suffix`() {
        val result = VersionComparator.compareVersions("0.11b", "0.12b")
        assertEquals(-1, result)
    }

    @Test
    fun `test current version greater than latest version with b suffix`() {
        val result = VersionComparator.compareVersions("0.12b", "0.11b")
        assertEquals(1, result)
    }

    @Test
    fun `test semantic version greater than version with b suffix`() {
        val result = VersionComparator.compareVersions("1.0.0", "0.11b")
        assertEquals(1, result)
    }

    @Test
    fun `test equal versions with b suffix`() {
        val result = VersionComparator.compareVersions("0.11b", "0.11b")
        assertEquals(0, result)
    }

    @Test
    fun `test version with v prefix equals version without prefix`() {
        val result = VersionComparator.compareVersions("v1.0", "1.0")
        assertEquals(0, result)
    }

    @Test
    fun `test version with v prefix equals semantic version without prefix`() {
        val result = VersionComparator.compareVersions("v1.0.0", "1.0.0")
        assertEquals(0, result)
    }

    @Test
    fun `test alpha version less than release version`() {
        val result = VersionComparator.compareVersions("1.0.0-alpha", "1.0.0")
        assertEquals(-1, result)
    }

    @Test
    fun `test beta version less than release version`() {
        val result = VersionComparator.compareVersions("1.0.0-beta", "1.0.0")
        assertEquals(-1, result)
    }

    @Test
    fun `test alpha version less than beta version`() {
        val result = VersionComparator.compareVersions("1.0.0-alpha", "1.0.0-beta")
        assertEquals(-1, result)
    }

    @Test
    fun `test beta version less than b suffix version`() {
        val result = VersionComparator.compareVersions("1.0.0-beta", "1.0.0b")
        assertEquals(1, result) // beta > b according to suffix priority
    }

    @Test
    fun `test b suffix less than release version`() {
        val result = VersionComparator.compareVersions("1.0.0b", "1.0.0")
        assertEquals(-1, result)
    }

    @Test
    fun `test rc version less than release version`() {
        val result = VersionComparator.compareVersions("1.0.0-rc", "1.0.0")
        assertEquals(-1, result)
    }

    @Test
    fun `test rc version greater than beta version`() {
        val result = VersionComparator.compareVersions("1.0.0-rc", "1.0.0-beta")
        assertEquals(1, result)
    }

    @Test
    fun `test three component version comparison`() {
        val result = VersionComparator.compareVersions("1.2.3", "1.2.4")
        assertEquals(-1, result)
    }

    @Test
    fun `test major version difference`() {
        val result = VersionComparator.compareVersions("2.0.0", "1.9.9")
        assertEquals(1, result)
    }

    @Test
    fun `test minor version difference`() {
        val result = VersionComparator.compareVersions("1.5.0", "1.4.9")
        assertEquals(1, result)
    }

    @Test
    fun `test patch version difference`() {
        val result = VersionComparator.compareVersions("1.0.1", "1.0.0")
        assertEquals(1, result)
    }

    @Test
    fun `test versions with different component counts`() {
        val result = VersionComparator.compareVersions("1.0", "1.0.0")
        assertEquals(0, result)
    }

    @Test
    fun `test versions with different component counts and difference`() {
        val result = VersionComparator.compareVersions("1.0", "1.0.1")
        assertEquals(-1, result)
    }

    @Test
    fun `test equal semantic versions`() {
        val result = VersionComparator.compareVersions("1.2.3", "1.2.3")
        assertEquals(0, result)
    }

    @Test
    fun `test versions with whitespace`() {
        val result = VersionComparator.compareVersions(" 1.0.0 ", "1.0.0")
        assertEquals(0, result)
    }

    @Test
    fun `test versions with uppercase V prefix`() {
        val result = VersionComparator.compareVersions("V1.0.0", "v1.0.0")
        assertEquals(0, result)
    }

    @Test
    fun `test empty version strings`() {
        val result = VersionComparator.compareVersions("", "1.0.0")
        assertEquals(-1, result)
    }

    @Test
    fun `test both empty version strings`() {
        val result = VersionComparator.compareVersions("", "")
        assertEquals(0, result)
    }

    @Test
    fun `test version with only suffix`() {
        val result = VersionComparator.compareVersions("alpha", "beta")
        assertEquals(-1, result)
    }

    @Test
    fun `test real world scenario - current app version vs newer`() {
        // Current app version is 0.11b, checking against 0.12b
        val result = VersionComparator.compareVersions("0.11b", "0.12b")
        assertEquals(-1, result)
    }

    @Test
    fun `test real world scenario - current app version vs same`() {
        // Current app version is 0.11b, checking against same version
        val result = VersionComparator.compareVersions("0.11b", "0.11b")
        assertEquals(0, result)
    }

    @Test
    fun `test real world scenario - current app version vs older`() {
        // Current app version is 0.11b, checking against older version
        val result = VersionComparator.compareVersions("0.11b", "0.10b")
        assertEquals(1, result)
    }

    @Test
    fun `test real world scenario - migration to semantic versioning`() {
        // Checking if 1.0.0 is greater than 0.11b (migration scenario)
        val result = VersionComparator.compareVersions("0.11b", "1.0.0")
        assertEquals(-1, result)
    }
}
