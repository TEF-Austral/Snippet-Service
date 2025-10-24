package entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "snippets",
    indexes = [
        Index(name = "idx_bucket_id", columnList = "bucket_id"),
        Index(name = "idx_language", columnList = "language"),
    ],
)
data class Snippet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snippet_id")
    val snippetId: Long? = null,
    @Column(name = "name", nullable = false, length = 255)
    var name: String,
    @Column(name = "description", columnDefinition = "TEXT")
    var description: String,
    @Column(name = "bucket_id", nullable = false, length = 255)
    var bucketId: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 50)
    var language: Language,
    @Column(name = "version", length = 50)
    var version: String? = null,
)
