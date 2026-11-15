package app.dopamine.domain.content

import app.dopamine.domain.model.NoteBlock
import app.dopamine.domain.model.Notes
import app.dopamine.domain.model.NotesRef
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NotesValidatorTest {
    private fun notes(vararg b: NoteBlock, minApp: Int = 1) = Notes("n", 1, "T", b.toList(), minApp)
    private val text = NoteBlock("a", "text", body = "hi")

    @Test fun validNotesPass() = assertNull(NotesValidator.validate(notes(text)))
    @Test fun emptyNotesFail() = assertNotNull(NotesValidator.validate(notes()))
    @Test fun duplicateBlockIdsFail() = assertNotNull(NotesValidator.validate(notes(text, text)))
    @Test fun unknownKindIsRejected() = assertNotNull(NotesValidator.validate(notes(text, NoteBlock("b", "hologram"))))
    @Test fun lockedNotesPass() = assertNull(NotesValidator.validate(notes(NoteBlock("b", "hologram"), minApp = ContentContract.APP_VERSION + 1)))
    @Test fun listNeedsItems() = assertNotNull(NotesValidator.validate(notes(NoteBlock("l", "list", title = "T"))))
    @Test fun codeBlockNeedsCode() {
        assertNotNull(NotesValidator.validate(notes(NoteBlock("c", "code", title = "T"))))
        assertNull(NotesValidator.validate(notes(NoteBlock("c", "code", title = "T", code = "x = 1"))))
    }
    @Test fun manifestMismatchFails() {
        assertNotNull(NotesValidator.validate(notes(text), NotesRef("other", "T", "a/b.json", 1, "h", 1)))
    }
}
