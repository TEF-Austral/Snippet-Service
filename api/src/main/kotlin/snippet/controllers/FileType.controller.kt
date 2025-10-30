package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import snippet.dtos.FileTypeDTO

@RestController
@RequestMapping("/filetypes")
class FileTypeController {

    @GetMapping
    fun getFileTypes(): ResponseEntity<List<FileTypeDTO>> {
        val fileTypes =
            listOf(
                FileTypeDTO("printscript", "prs"),
                FileTypeDTO("java", "java"),
                FileTypeDTO("python", "py"),
                FileTypeDTO("golang", "go"),
            )
        return ResponseEntity.ok(fileTypes)
    }
}
