package snippet.services

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import snippet.component.AssetServiceClient
import snippet.component.AuthorizationServiceClient
import snippet.component.PrintScriptServiceClient
import snippet.dtos.requests.CreateSnippetRequestDTO
import snippet.dtos.PaginatedSnippetsDTO
import snippet.dtos.SnippetFilterDTO
import snippet.dtos.responses.SnippetResponseDTO
import snippet.dtos.SortField
import snippet.dtos.SortOrder
import snippet.dtos.requests.UpdateSnippetRequestDTO
import snippet.dtos.responses.StreamSnippetResponseDTO
import snippet.entities.ComplianceStatus
import snippet.entities.Snippet
import snippet.repositories.SnippetRepository
import snippet.repositories.SnippetSpecifications
import snippet.producers.AsyncTaskProducer // <-- 1. IMPORTAR EL PRODUCER

@Service
class SnippetServiceImpl(
    private val repository: SnippetRepository,
    private val assetServiceClient: AssetServiceClient,
    private val authorizationServiceClient: AuthorizationServiceClient,
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val asyncTaskProducer: AsyncTaskProducer,
) : SnippetService {
    private val log = org.slf4j.LoggerFactory.getLogger(SnippetServiceImpl::class.java)

    @Transactional
    override fun createSnippet(
        requestDTO: CreateSnippetRequestDTO,
        ownerId: String,
        author: String,
    ): SnippetResponseDTO {
        log.info("Creating snippet for owner $ownerId, name: ${requestDTO.name}")
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

        // Guardar contenido en Asset Service
        assetServiceClient.createOrUpdateAsset(
            container = saved.bucketContainer,
            key = bucketKey,
            content = requestDTO.content ?: "",
        )

        // Validar con PrintScript Service
        validateAndUpdateCompliance(saved)

        repository.save(saved)
        log.warn("Snippet created with id ${saved.id}, compliance: ${saved.complianceStatus}")

        return saved.toDto()
    }

    override fun getSnippetById(
        id: Long,
        requesterId: String,
    ): SnippetResponseDTO {
        log.info("Fetching snippet $id for requester $requesterId")
        val entity =
            repository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                action = "read",
                snippetId = id.toString(),
                ownerId = entity.ownerId,
            )

        if (!hasPermission) {
            log.warn("Permission denied for requester $requesterId on snippet $id")
            throw IllegalAccessException("You don't have permission to access this snippet")
        }

        log.warn("Snippet $id retrieved successfully")
        return entity.toDto()
    }

    override fun getMySnippets(
        requesterId: String,
        page: Int,
        pageSize: Int,
        filterDTO: SnippetFilterDTO,
    ): PaginatedSnippetsDTO {
        log.info("Fetching snippets for requester $requesterId, page $page, pageSize $pageSize")
        val safePage = if (page < 0) 0 else page
        val safeSize = if (pageSize <= 0) 20 else pageSize

        val snippetIdsWithReadPermission =
            authorizationServiceClient.getSnippetsByPermission(
                userId = requesterId,
                permission = "read",
            )
        val sharedSnippetIds = snippetIdsWithReadPermission.mapNotNull { it.toLongOrNull() }

        var spec: Specification<Snippet> =
            SnippetSpecifications.ownershipFilter(
                requesterId,
                sharedSnippetIds,
                filterDTO.ownership.name,
            )

        // Add name filter
        filterDTO.name?.let { name ->
            SnippetSpecifications.nameContains(name)?.let { nameSpec ->
                spec = spec.and(nameSpec)
            }
        }

        // Add language filter
        filterDTO.language?.let { language ->
            SnippetSpecifications.hasLanguage(language)?.let { languageSpec ->
                spec = spec.and(languageSpec)
            }
        }

        // Add compliance filter
        SnippetSpecifications.complianceFilter(filterDTO.compliance.name)?.let { complianceSpec ->
            spec = spec.and(complianceSpec)
        }

        // Build sort
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

        val pageable = PageRequest.of(safePage, safeSize, Sort.by(sortDirection, sortField))

        val pageResult = repository.findAll(spec, pageable)

        val result =
            PaginatedSnippetsDTO(
                page = pageResult.number,
                pageSize = pageResult.size,
                count = pageResult.totalElements,
                snippets = pageResult.content.map { it.toDto() },
            )
        log.warn("Retrieved ${result.snippets.size} snippets for requester $requesterId")
        return result
    }

    override fun getSnippetsThatUserHaveAccess(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO {
        log.info("Fetching snippets with access for requester $requesterId")
        val safePage = if (page < 0) 0 else page
        val safeSize = if (pageSize <= 0) 20 else pageSize
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"))
        val snippets = authorizationServiceClient.getSnippetsByPermission(requesterId, "read")
        val snippetIds = snippets.mapNotNull { it.toLongOrNull() }
        val pageResult = repository.findByIdIn(snippetIds, pageable)
        val result =
            PaginatedSnippetsDTO(
                page = pageResult.number,
                pageSize = pageResult.size,
                count = pageResult.totalElements,
                snippets = pageResult.content.map { it.toDto() },
            )
        log.warn("Retrieved ${result.snippets.size} accessible snippets")
        return result
    }

    override fun getSnippetsThatUserHavePermission(
        requesterId: String,
    ): List<StreamSnippetResponseDTO> {
        log.info("Fetching stream snippets with permissions for requester $requesterId")
        val snippets = authorizationServiceClient.getSnippetsByPermission(requesterId, "read")
        val snippetIds = snippets.mapNotNull { it.toLongOrNull() }
        val snippetEntities = repository.findAllById(snippetIds)

        val result =
            snippetEntities.map { snippet ->
                StreamSnippetResponseDTO(
                    snippetId = snippet.id ?: 0L,
                    bucketContainer = snippet.bucketContainer,
                    bucketKey = snippet.bucketKey ?: "",
                    language = snippet.language,
                    version = snippet.version,
                )
            }
        log.warn("Retrieved ${result.size} stream snippets")
        return result
    }

    override fun getAllMySnippets(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO {
        val safePage = if (page < 0) 0 else page
        val safeSize = if (pageSize <= 0) 20 else pageSize

        val snippetIdsWithReadPermission =
            authorizationServiceClient.getSnippetsByPermission(
                userId = requesterId,
                permission = "read",
            )

        val snippetIds = snippetIdsWithReadPermission.mapNotNull { it.toLongOrNull() }

        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"))

        val pageResult = repository.findByOwnerIdOrIdIn(requesterId, snippetIds, pageable)

        return PaginatedSnippetsDTO(
            page = pageResult.number,
            pageSize = pageResult.size,
            count = pageResult.totalElements,
            snippets = pageResult.content.map { it.toDto() },
        )
    }

    override fun getSnippetThatUserIsOwner(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO {
        val safePage = if (page < 0) 0 else page
        val safeSize = if (pageSize <= 0) 20 else pageSize
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"))

        val pageResult = repository.findByOwnerId(requesterId, pageable)

        return PaginatedSnippetsDTO(
            page = pageResult.number,
            pageSize = pageResult.size,
            count = pageResult.totalElements,
            snippets = pageResult.content.map { it.toDto() },
        )
    }

    @Transactional
    override fun updateSnippet(
        id: Long,
        requestDTO: UpdateSnippetRequestDTO,
        requesterId: String,
    ): SnippetResponseDTO {
        log.info("Updating snippet $id for requester $requesterId")
        val existing =
            repository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                action = "edit",
                snippetId = id.toString(),
                ownerId = existing.ownerId,
            )

        if (!hasPermission) {
            log.warn("Permission denied for requester $requesterId to update snippet $id")
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

            // Re-validar después de actualizar contenido
            validateAndUpdateCompliance(existing)

            // <-- 4. LLAMAR A LA NUEVA FUNCIÓN HELPER
            triggerAsyncTesting(existing)
        }

        val saved = repository.save(existing)
        log.warn("Snippet $id updated successfully")

        return saved.toDto()
    }

    @Transactional
    override fun deleteSnippet(
        id: Long,
        requesterId: String,
    ) {
        val existing =
            repository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                action = "delete",
                snippetId = id.toString(),
                ownerId = existing.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to delete this snippet")
        }

        val bucketKey =
            existing.bucketKey ?: throw IllegalStateException("Snippet has no bucket key")

        assetServiceClient.deleteAsset(
            container = existing.bucketContainer,
            key = bucketKey,
        )

        repository.deleteById(id)
    }

    private fun validateAndUpdateCompliance(snippet: Snippet) {
        try {
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
            } else {
                snippet.complianceStatus = ComplianceStatus.NON_COMPLIANT
                snippet.lastValidationError =
                    validation.violations.joinToString("\n") {
                        "Line ${it.line}, Column ${it.column}: ${it.message}"
                    }
            }
        } catch (e: Exception) {
            snippet.complianceStatus = ComplianceStatus.FAILED
            snippet.lastValidationError = "Validation failed: ${e.message}"
        }
    }

    private fun triggerAsyncTesting(snippet: Snippet) {
        val snippetId = snippet.id ?: return
        val bucketKey = snippet.bucketKey ?: return

        println("📤 [Snippet Service] Disparando tests asíncronos para snippet: $snippetId")

        asyncTaskProducer.requestTesting(
            snippetId = snippetId,
            bucketContainer = snippet.bucketContainer,
            bucketKey = bucketKey,
            version = snippet.version,
        )
    }

    private fun Snippet.toDto() =
        SnippetResponseDTO(
            snippetId = this.id ?: 0L,
            name = this.name,
            description = this.description,
            language = this.language,
            version = this.version,
            author = this.author,
            content = assetServiceClient.getAsset(this.bucketContainer, this.bucketKey!!),
            complianceStatus = this.complianceStatus.name,
            validationErrors = this.lastValidationError,
        )
}
