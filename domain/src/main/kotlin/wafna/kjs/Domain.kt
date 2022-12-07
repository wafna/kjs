package wafna.kjs

import java.util.*

private fun newID(): UUID = UUID.randomUUID()

data class Record(val id: UUID, val data: String)

data class RecordWIP(val data: String) {
    fun commit(): Record = Record(newID(), data)
}