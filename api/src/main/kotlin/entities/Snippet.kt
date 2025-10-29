package entities

import common.Language
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "snippets",
    indexes = [
        Index(name = "idx_bucket_key", columnList = "bucket_key"),
        Index(name = "idx_language", columnList = "language"),
        Index(name = "idx_owner_id", columnList = "owner_id"), // Nuevo índice
    ],
)
data class Snippet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "name", nullable = false, length = 255)
    var name: String,
    @Column(name = "description", columnDefinition = "TEXT")
    var description: String,
    @Column(name = "owner_id", nullable = false, length = 255)
    var ownerId: String,
    @Column(name = "bucket_key", nullable = false, length = 255)
    var bucketKey: String? = null,
    @Column(name = "bucket_container", nullable = false, length = 255)
    var bucketContainer: String = "snippets",
    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 50)
    var language: Language,
    @Column(name = "version", length = 50)
    var version: String,
) {
    @PrePersist
    fun ensureBucketKey() {
        if (bucketKey.isNullOrBlank()) {
            bucketKey = UUID.randomUUID().toString()
        }
    }
}
