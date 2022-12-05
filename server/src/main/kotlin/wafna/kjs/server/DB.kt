package wafna.kjs.server

import com.zaxxer.hikari.HikariDataSource
import java.util.*

class DB(private val dataSource: HikariDataSource) {

    fun createRecord(record: Record): Unit = dataSource.connection.use { cx ->
        cx.autoCommit = true
        cx.prepareStatement("INSERT INTO record (id, data) VALUES (?, ?)").use { ps ->
            ps.setObject(1, record.id)
            ps.setString(2, record.data)
            ps.executeUpdate().also { check(1 == it) }
        }
    }

    fun readRecords(): List<Record> = dataSource.connection.use { cx ->
        cx.prepareStatement("SELECT id, data FROM record").use { ps ->
            ps.executeQuery().use { rs ->
                mutableListOf<Record>().also { records ->
                    while (rs.next()) {
                        var ix = 0
                        records += Record(rs.getObject(++ix) as UUID, rs.getString(++ix))
                    }
                }
            }
        }
    }

    fun updateRecord(record: Record): Boolean = dataSource.connection.use { cx ->
        cx.autoCommit = true
        cx.prepareStatement("UPDATE record SET data = ? WHERE id = ?").use { ps ->
            ps.setString(1, record.data)
            ps.setObject(2, record.id)
            when (ps.executeUpdate()) {
                0 -> false
                else -> true
            }
        }
    }

    fun deleteRecord(id: UUID): Boolean = dataSource.connection.use { cx ->
        cx.autoCommit = true
        cx.prepareStatement("DELETE FROM record WHERE id = ?").use { ps ->
            ps.setObject(1, id)
            when (ps.executeUpdate()) {
                0 -> false
                else -> true
            }
        }
    }
}