package security

interface AuthenticatedUserProviderInt {

    fun getCurrentUserId(): String

    fun getCurrentUserName(): String?
}
