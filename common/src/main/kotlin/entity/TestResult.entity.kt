package entity

import jakarta.persistence.Entity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "test_results")
data class TestResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "snippet_id", nullable = false)
    val snippetId: Long,
    @Column(name = "test_id", nullable = false)
    val testId: Long,
    @Column(name = "passed", nullable = false)
    val passed: Boolean,
    @Column(name = "executed_at", nullable = false)
    val executedAt: LocalDateTime = LocalDateTime.now(),
    @ElementCollection
    @CollectionTable(
        name = "test_result_outputs",
        joinColumns = [JoinColumn(name = "test_result_id")],
    )
    @Column(name = "output")
    val outputs: List<String> = emptyList(),
    @ElementCollection
    @CollectionTable(
        name = "test_result_errors",
        joinColumns = [JoinColumn(name = "test_result_id")],
    )
    @Column(name = "error")
    val errors: List<String> = emptyList(),
)
