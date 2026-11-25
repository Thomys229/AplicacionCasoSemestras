package com.example.reservaescuela

import com.example.reservaescuela.ui.components.fileTypeLabel
import com.example.reservaescuela.ui.components.isPdfOrWord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArchivosTest {

    @Test
    fun aceptaArchivosPdf() {
        assertTrue(isPdfOrWord("application/pdf"))
    }

    @Test
    fun identificaArchivosWord() {
        assertEquals(
            "Word",
            fileTypeLabel("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        )
    }
}
