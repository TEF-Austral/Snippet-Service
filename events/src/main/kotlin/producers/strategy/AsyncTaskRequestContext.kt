data class AsyncTaskRequestContext(
    val snippetId: Long,
    val bucketContainer: String,
    val bucketKey: String,
    val version: String,
    val userId: String? = null,
    val languageId: String? = null,
)
