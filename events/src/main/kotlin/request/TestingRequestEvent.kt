package request

data class TestingRequestEvent(
    val requestId: String,
    val snippetId: Long,
    val bucketContainer: String,
    val bucketKey: String,
    val version: String,
    val testId: Long,
)
