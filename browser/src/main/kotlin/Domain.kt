import kotlinx.serialization.Serializable

typealias UUID = String

@Serializable
data class Record(val id: UUID, val data: String)

@Serializable
data class RecordWIP(val data: String)

