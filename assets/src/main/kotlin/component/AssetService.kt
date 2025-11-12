package component

interface AssetService {

    fun getAsset(
        container: String,
        key: String,
    ): String?

    fun createOrUpdateAsset(
        container: String,
        key: String,
        content: String,
    )

    fun deleteAsset(
        container: String,
        key: String,
    )
}
