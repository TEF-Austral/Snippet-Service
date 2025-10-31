interface ProductCreatedProducer {
    suspend fun publishEvent(name: String)
}
