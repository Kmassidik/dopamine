package app.dopamine.domain

import app.dopamine.domain.model.ProfileName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileNameTest {
    @Test fun trimsAndCollapsesSpaces() = assertEquals("Ana Maria", ProfileName.clean("  Ana    Maria "))
    @Test fun blankIsRejected() { assertNull(ProfileName.clean("")); assertNull(ProfileName.clean("   ")) }
    @Test fun tooLongIsRejected() = assertNull(ProfileName.clean("x".repeat(ProfileName.MAX + 1)))
    @Test fun maxLengthIsAccepted() = assertEquals("x".repeat(ProfileName.MAX), ProfileName.clean("x".repeat(ProfileName.MAX)))
    @Test fun controlCharactersAreStripped() = assertEquals("Ab", ProfileName.clean("A\u0000b\n"))
    @Test fun unicodeNamesSurvive() = assertEquals("Åsa 田中", ProfileName.clean("Åsa 田中"))
}
