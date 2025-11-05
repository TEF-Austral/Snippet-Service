package request

data class FormattingRequestEvent(
    val requestId: String,
    val snippetId: Long,
    val bucketContainer: String,
    val bucketKey: String,
    val version: String,
    val userId: String,
)
