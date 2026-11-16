package app.dopamine.data.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseVersionTest {
    @Test fun newerStableReleaseIsDetected() {
        assertTrue(ReleaseVersion.isNewer("v0.3.2", "0.3.1"))
        assertTrue(ReleaseVersion.isNewer("0.4.0", "0.3.1"))
        assertTrue(ReleaseVersion.isNewer("1.0", "0.9.9"))
        assertTrue(ReleaseVersion.isNewer("0.3.1.1", "0.3.1"))
    }

    @Test fun currentOlderAndEquivalentVersionsDoNotPrompt() {
        assertFalse(ReleaseVersion.isNewer("0.3.1", "0.3.1"))
        assertFalse(ReleaseVersion.isNewer("v0.3.1", "0.3.1"))
        assertFalse(ReleaseVersion.isNewer("0.3", "0.3.0"))
        assertFalse(ReleaseVersion.isNewer("0.3.0", "0.3.1"))
        assertFalse(ReleaseVersion.isNewer("0.2.9", "0.3.1"))
    }

    @Test(expected = AppUpdateException::class)
    fun malformedVersionIsRejected() {
        ReleaseVersion.isNewer("ios-v0.3.0-preview", "0.3.1")
    }
}
