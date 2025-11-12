package services

import AsyncTaskRequestContext
import authorization.AuthorizationService
import authorization.UserAction
import dtos.requests.CreateSnippetRequestDTO
import dtos.requests.SnippetFilterDTO
import dtos.requests.SortField
import dtos.requests.SortOrder
import dtos.requests.UpdateSnippetRequestDTO
import dtos.responses.PaginatedSnippetsDTO
import dtos.responses.SnippetResponseDTO
import common.dtos.types.ComplianceStatus
import component.AssetService
import component.ExecutionServiceClient
import dtos.responses.StreamSnippetResponseDTO
import entity.Snippet
import filters.SnippetFilterComposer
import filters.SnippetFilterFactory
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import producers.AsyncTaskProducerInt
import producers.strategy.TaskType
import repositories.SnippetRepository

@Service
class PrintScriptSnippetService(
    private val repository: SnippetRepository,
    private val assetServiceClient: AssetService,
    private val authorizationServiceClient: AuthorizationService,
    private val printScriptServiceClient: ExecutionServiceClient,
    private val asyncTaskProducer: AsyncTaskProducerInt,
    private val filterFactory: SnippetFilterFactory,
) : SnippetService {
    private val log = LoggerFactory.getLogger(PrintScriptSnippetService::class.java)

    @Transactional
    override fun createSnippet(
        requestDTO: CreateSnippetRequestDTO,
        ownerId: String,
        author: String,
        bucketContainer: String,
    ): SnippetResponseDTO {
        log.info(
            "Creating snippet: name=${requestDTO.name}, ownerId=$ownerId, language=${requestDTO.language}, version=${requestDTO.version}",
        )

        val entity =
            Snippet(
                name = requestDTO.name,
                description = requestDTO.description,
                ownerId = ownerId,
                bucketContainer = "snippets",
                language = requestDTO.language,
                version = requestDTO.version,
                author = author,
                complianceStatus = ComplianceStatus.PENDING,
            )

        val saved = repository.save(entity)

        val bucketKey =
            saved.bucketKey
                ?: throw IllegalStateException("Snippet was saved but has no bucket key")

        updateAsset(saved, bucketKey, requestDTO)

        validateAndUpdateCompliance(saved)

        repository.save(saved)

        log.info(
            "Snippet created successfully: snippetId=${saved.id}, name=${saved.name}, complianceStatus=${saved.complianceStatus}",
        )
        return saved.toDto()
    }

    override fun getSnippetById(
        id: Long,
        requesterId: String,
    ): SnippetResponseDTO {
        log.info("Getting snippet by ID: snippetId=$id, requesterId=$requesterId")

        val snippet =
            repository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        checkReadPermission(requesterId, id, snippet)

        log.debug("Snippet retrieved successfully: snippetId=$id, name=${snippet.name}")
        return snippet.toDto()
    }

    private fun checkReadPermission(
        requesterId: String,
        id: Long,
        snippet: Snippet?,
    ) {
        if (snippet == null) {
            throw NoSuchElementException("Snippet not found: $id")
        }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                action = UserAction.READ,
                snippetId = id.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to access this snippet")
        }
    }

    override fun getMySnippets(
        requesterId: String,
        page: Int,
        pageSize: Int,
        filterDTO: SnippetFilterDTO,
    ): PaginatedSnippetsDTO {
        log.info("Getting my snippets: requesterId=$requesterId, page=$page, pageSize=$pageSize")

        val sharedSnippetIds = snippetIdsFor(requesterId)

        // Create all filters using the factory
        val filters = filterFactory.createFilters(filterDTO, requesterId, sharedSnippetIds)

        // Compose filters into a single specification
        val spec =
            SnippetFilterComposer()
                .apply { filters.forEach { addFilter(it) } }
                .build()

        val sortField =
            when (filterDTO.sortBy) {
                SortField.NAME -> "name"
                SortField.LANGUAGE -> "language"
                SortField.COMPLIANCE -> "complianceStatus"
            }

        val sortDirection =
            when (filterDTO.sortOrder) {
                SortOrder.ASC -> Sort.Direction.ASC
                SortOrder.DESC -> Sort.Direction.DESC
            }

        val pageable = createPageable(page, pageSize, sortField, sortDirection)

        val pageResult = repository.findAll(spec, pageable)

        log.debug(
            "Retrieved ${pageResult.totalElements} snippets for user: requesterId=$requesterId",
        )
        return toPaginatedSnippetsDTO(pageResult)
    }

    override fun getSnippetsThatUserHaveAccess(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO {
        val pageable = createPageable(page, pageSize, "id", Sort.Direction.DESC)
        val snippetIds = snippetIdsFor(requesterId)
        val pageResult = repository.findByIdIn(snippetIds, pageable)
        return toPaginatedSnippetsDTO(pageResult)
    }

    override fun getSnippetsThatUserHavePermission(
        requesterId: String,
    ): List<StreamSnippetResponseDTO> {
        val snippetIds = snippetIdsFor(requesterId)
        val snippetEntities = repository.findAllById(snippetIds)

        return snippetEntities.map { snippet ->
            StreamSnippetResponseDTO(
                snippetId = snippet.id ?: 0L,
                bucketContainer = snippet.bucketContainer,
                bucketKey = snippet.bucketKey ?: "",
                language = snippet.language,
                version = snippet.version,
            )
        }
    }

    override fun getAllMySnippets(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO {
        val snippetIds = snippetIdsFor(requesterId)

        val pageable = createPageable(page, pageSize, "id", Sort.Direction.DESC)

        val pageResult = repository.findByOwnerIdOrIdIn(requesterId, snippetIds, pageable)

        return toPaginatedSnippetsDTO(pageResult)
    }

    override fun getSnippetThatUserIsOwner(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO {
        val pageable = createPageable(page, pageSize, "id", Sort.Direction.DESC)

        val pageResult = repository.findByOwnerId(requesterId, pageable)

        return toPaginatedSnippetsDTO(pageResult)
    }

    @Transactional
    override fun updateSnippet(
        id: Long,
        requestDTO: UpdateSnippetRequestDTO,
        requesterId: String,
    ): SnippetResponseDTO {
        log.info("Updating snippet: snippetId=$id, requesterId=$requesterId")

        val existing =
            repository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                action = UserAction.EDIT,
                snippetId = id.toString(),
                ownerId = existing.ownerId,
            )

        if (!hasPermission) {
            log.warn(
                "User does not have permission to update snippet: snippetId=$id, requesterId=$requesterId",
            )
            throw IllegalAccessException("You don't have permission to update this snippet")
        }

        requestDTO.name?.let { existing.name = it }
        requestDTO.description?.let { existing.description = it }
        requestDTO.language?.let { existing.language = it }
        requestDTO.version?.let { existing.version = it }

        requestDTO.content?.let { content ->
            assetServiceClient.createOrUpdateAsset(
                container = existing.bucketContainer,
                key =
                    existing.bucketKey ?: throw IllegalStateException(
                        "Snippet has no bucket key",
                    ),
                content = content,
            )

            validateAndUpdateCompliance(existing)

            triggerAsyncTesting(existing)
        }

        val saved = repository.save(existing)

        log.info("Snippet updated successfully: snippetId=$id, name=${saved.name}")
        return saved.toDto()
    }

    @Transactional
    override fun deleteSnippet(
        id: Long,
        requesterId: String,
    ) {
        log.info("Deleting snippet: snippetId=$id, requesterId=$requesterId")

        val existing =
            repository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                action = UserAction.DELETE,
                snippetId = id.toString(),
                ownerId = existing.ownerId,
            )

        if (!hasPermission) {
            log.warn(
                "User does not have permission to delete snippet: snippetId=$id, requesterId=$requesterId",
            )
            throw IllegalAccessException("You don't have permission to delete this snippet")
        }

        val bucketKey =
            existing.bucketKey ?: throw IllegalStateException("Snippet has no bucket key")

        assetServiceClient.deleteAsset(
            container = existing.bucketContainer,
            key = bucketKey,
        )

        repository.deleteById(id)

        log.info("Snippet deleted successfully: snippetId=$id")
    }

    private fun validateAndUpdateCompliance(snippet: Snippet) {
        try {
            log.debug("Validating compliance for snippet: snippetId=${snippet.id}")

            val validation =
                printScriptServiceClient.analyzeSnippet(
                    container = snippet.bucketContainer,
                    key = snippet.bucketKey!!,
                    version = snippet.version,
                    userId = snippet.ownerId,
                )

            if (validation.isValid) {
                snippet.complianceStatus = ComplianceStatus.COMPLIANT
                snippet.lastValidationError = null
                log.debug("Snippet validation passed: snippetId=${snippet.id}")
            } else {
                snippet.complianceStatus = ComplianceStatus.NON_COMPLIANT
                snippet.lastValidationError =
                    validation.violations.joinToString("\n") {
                        "Line ${it.line}, Column ${it.column}: ${it.message}"
                    }
                log.warn(
                    "Snippet validation failed: snippetId=${snippet.id}, violations=${validation.violations.size}",
                )
            }
        } catch (e: Exception) {
            snippet.complianceStatus = ComplianceStatus.FAILED
            snippet.lastValidationError = "Validation failed: ${e.message}"
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error("Validation error for snippet ${snippet.id} at $location: ${e.message}", e)
        }
    }

    private fun triggerAsyncTesting(snippet: Snippet) {
        val snippetId = snippet.id ?: return
        val bucketKey = snippet.bucketKey ?: return

        log.info("Triggering async tests for snippet: snippetId=$snippetId")

        val context =
            AsyncTaskRequestContext(
                snippetId = snippetId,
                bucketContainer = snippet.bucketContainer,
                bucketKey = bucketKey,
                version = snippet.version,
            )

        asyncTaskProducer.request(
            TaskType.TESTING,
            context = context,
        )

        log.debug("Async testing request sent for snippet: snippetId=$snippetId")
    }

    private fun Snippet.toDto() =
        SnippetResponseDTO(
            snippetId = this.id ?: 0L,
            name = this.name,
            description = this.description,
            language = this.language,
            version = this.version,
            author = this.author,
            content =
                assetServiceClient.getAsset(this.bucketContainer, this.bucketKey!!)
                    ?: throw IllegalStateException("No content found for snippet"),
            complianceStatus = this.complianceStatus.name,
            validationErrors = this.lastValidationError,
        )

    private fun updateAsset(
        saved: Snippet,
        bucketKey: String,
        requestDTO: CreateSnippetRequestDTO,
    ) {
        assetServiceClient.createOrUpdateAsset(
            container = saved.bucketContainer,
            key = bucketKey,
            content = requestDTO.content ?: "",
        )
    }

    private fun safePage(page: Int) = if (page < 0) 0 else page

    private fun safeSize(pageSize: Int) = if (pageSize <= 0) 20 else pageSize

    private fun createPageable(
        page: Int,
        pageSize: Int,
        sortField: String,
        direction: Sort.Direction,
    ): PageRequest =
        PageRequest.of(safePage(page), safeSize(pageSize), Sort.by(direction, sortField))

    private fun toPaginatedSnippetsDTO(pageResult: Page<Snippet>): PaginatedSnippetsDTO =
        PaginatedSnippetsDTO(
            page = pageResult.number,
            pageSize = pageResult.size,
            count = pageResult.totalElements,
            snippets = pageResult.content.map { it.toDto() },
        )

    private fun snippetIdsFor(userId: String): List<Long> =
        authorizationServiceClient.getSnippetsByPermission(userId, "read").mapNotNull {
            it.toLongOrNull()
        }
}
