import kotlinx.serialization.Serializable

@Serializable
data class Record(val id: UUID, val data: String)

@Serializable
data class RecordWIP(val data: String)

