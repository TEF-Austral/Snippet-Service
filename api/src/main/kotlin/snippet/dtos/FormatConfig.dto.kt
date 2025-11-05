package snippet.dtos

data class FormatConfigDTO(
    val spaceBeforeColon: Boolean,
    val spaceAfterColon: Boolean,
    val spacesInAssignation: Int,
    val newLineBeforePrintln: Int,
)
