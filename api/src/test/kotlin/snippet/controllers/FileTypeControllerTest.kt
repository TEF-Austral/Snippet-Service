package snippet.controllers

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FileTypeControllerTest {

    private val controller = FileTypeController()

    @Test
    fun `getFileTypes should return list of supported file types`() {
        val response = controller.getFileTypes()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(4, response.body?.size)
    }

    @Test
    fun `getFileTypes should include PRINTSCRIPT type`() {
        val response = controller.getFileTypes()

        val printscriptType = response.body?.find { it.language == "PRINTSCRIPT" }
        assertNotNull(printscriptType)
        assertEquals("prs", printscriptType.extension)
    }

    @Test
    fun `getFileTypes should include JAVA type`() {
        val response = controller.getFileTypes()

        val javaType = response.body?.find { it.language == "JAVA" }
        assertNotNull(javaType)
        assertEquals("java", javaType.extension)
    }

    @Test
    fun `getFileTypes should include PYTHON type`() {
        val response = controller.getFileTypes()

        val pythonType = response.body?.find { it.language == "PYTHON" }
        assertNotNull(pythonType)
        assertEquals("py", pythonType.extension)
    }

    @Test
    fun `getFileTypes should include GOLANG type`() {
        val response = controller.getFileTypes()

        val golangType = response.body?.find { it.language == "GOLANG" }
        assertNotNull(golangType)
        assertEquals("go", golangType.extension)
    }

    @Test
    fun `getFileTypes should return all expected languages`() {
        val response = controller.getFileTypes()

        val languages = response.body?.map { it.language }
        assertEquals(listOf("PRINTSCRIPT", "JAVA", "PYTHON", "GOLANG"), languages)
    }

    @Test
    fun `getFileTypes should return all expected extensions`() {
        val response = controller.getFileTypes()

        val extensions = response.body?.map { it.extension }
        assertEquals(listOf("prs", "java", "py", "go"), extensions)
    }
}
