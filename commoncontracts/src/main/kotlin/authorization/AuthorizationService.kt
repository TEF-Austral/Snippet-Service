package authorization

import dtos.responses.PermissionResponseDTO

interface AuthorizationService {

    fun checkPermission(
        userId: String,
        action: UserAction,
        snippetId: String,
        ownerId: String,
    ): Boolean

    fun grantPermission(
        requesterId: String,
        ownerId: String,
        granteeId: String,
        snippetId: String,
        canRead: Boolean,
        canEdit: Boolean,
    ): PermissionResponseDTO

    fun revokePermission(
        requesterId: String,
        userId: String,
        snippetId: String,
    )

    fun getSnippetPermissions(
        requesterId: String,
        snippetId: String,
    ): List<PermissionResponseDTO>

    fun getSnippetsByPermission(
        userId: String,
        permission: String,
    ): List<String>
}
