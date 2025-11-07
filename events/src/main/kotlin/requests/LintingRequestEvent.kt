package requests

data class LintingRequestEvent(
    val requestId: String,
    val bucketContainer: String,
    val bucketKey: String,
    val version: String,
    val languageId: String,
    val userId: String,
    val snippetId: Long,
)
