package requests

data class FormattingRequestEvent(
    val requestId: String,
    val bucketContainer: String,
    val bucketKey: String,
    val languageId: String,
    val version: String,
    val userId: String,
)
