package events

import common.Language
import java.io.Serializable

data class SnippetEvent(
    val snippetId: Long,
    val bucketId: String,
    val bucketContainer: String,
    val ownerId: String,
    val name: String,
    val content: String?,
    val language: Language,
    val version: String,
    val operation: SnippetOperation,
) : Serializable
