package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import snippet.dtos.responses.FileTypeDTO

@RestController
@RequestMapping("/filetypes")
class FileTypeController {

    @GetMapping
    fun getFileTypes(): ResponseEntity<List<FileTypeDTO>> {
        val fileTypes =
            listOf(
                FileTypeDTO("PRINTSCRIPT", "prs"),
                FileTypeDTO("JAVA", "java"),
                FileTypeDTO("PYTHON", "py"),
                FileTypeDTO("GOLANG", "go"),
            )
        return ResponseEntity.ok(fileTypes)
    }
}
