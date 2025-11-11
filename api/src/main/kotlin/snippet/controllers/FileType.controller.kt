package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import snippet.dtos.responses.FileTypeDTO

@RestController
@RequestMapping("/filetypes")
class FileTypeController {
    private val log = org.slf4j.LoggerFactory.getLogger(FileTypeController::class.java)

    @GetMapping
    fun getFileTypes(): ResponseEntity<List<FileTypeDTO>> {
        log.info("GET /filetypes - Fetching available file types")
        val fileTypes =
            listOf(
                FileTypeDTO("PRINTSCRIPT", "prs"),
                FileTypeDTO("JAVA", "java"),
                FileTypeDTO("PYTHON", "py"),
                FileTypeDTO("GOLANG", "go"),
            )
        log.warn("GET /filetypes - Retrieved ${fileTypes.size} file types")
        return ResponseEntity.ok(fileTypes)
    }
}
