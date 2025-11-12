import dtos.types.Language

data class StreamSnippetResponseDTO(
    val snippetId: Long,
    val bucketContainer: String,
    val bucketKey: String,
    val language: Language,
    val version: String,
)
