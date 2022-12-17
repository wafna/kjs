package pages

import csstype.ClassName
import csstype.Color
import emotion.react.css
import pages.gridley.DisplayColumn
import pages.gridley.Gridley
import react.FC
import react.Props
import react.dom.html.ReactHTML as h
import kotlin.math.floor
import kotlin.random.Random

/**
 * Our custom record data type.
 * The data in the records are intended to make it easy to narrow down the page count using search.
 *
 * @param id This will be sorted numerically.
 * @param name A string of random lower case letters (for sorting and searching).
 * @param number A string of random decimal digits (for sorting and searching).
 * @param stuff An excuse to do some pretty UI.
 */
data class GridRecord(val id: Int, val name: String, val number: String, val stuff: Pair<Boolean, Boolean>)

private fun randomString(chars: List<Char>, length: Int): String {
    require(0 <= length)
    return buildString {
        repeat(32) {
            append(
                chars[floor(Random.nextDouble() * chars.size).toInt()]
            )
        }
    }
}

private val chars = ('a'..'z').toList()
private val digits = ('0'..'9').toList()

// Specializations on DisplayColumn to avoid repetition.

private abstract class DisplayColumnStdHdr(headerText: String) : DisplayColumn() {
    override val header: FC<Props> = FC {
        h.span {
            className = ClassName("gridley-header")
            +headerText
        }
    }
}

private abstract class DisplayColumnInt(headerText: String) : DisplayColumnStdHdr(headerText) {
    abstract fun value(record: GridRecord): Int
    override val searchFunction: ((GridRecord) -> String) = { value(it).toString() }
    override val comparator: Comparator<GridRecord> = Comparator { a, b -> value(a).compareTo(value(b)) }
    override fun renderField(record: GridRecord): FC<Props> = FC { h.pre { +value(record).toString() } }
}

private abstract class DisplayColumnPre(headerText: String) : DisplayColumnStdHdr(headerText) {
    abstract fun value(record: GridRecord): String
    override val searchFunction: ((GridRecord) -> String) = { value(it) }
    override val comparator: Comparator<GridRecord> = Comparator { a, b -> value(a).compareTo(value(b)) }
    override fun renderField(record: GridRecord): FC<Props> = FC { h.pre { +value(record) } }
}

private val Red = Color("#008000")
private val Green = Color("#800000")

/**
 * This hosts the demo by creating some records.
 */
val GridleyDemo = FC<Props> {
    val totalRecords = 1800

    Gridley {
        pageSize = 15
        recordSet = (0 until totalRecords).map { id ->
            GridRecord(
                id,
                randomString(chars, 32),
                randomString(digits, 32),
                Pair(Random.nextBoolean(), Random.nextBoolean())
            )
        }
        columns = listOf(
            object : DisplayColumnInt("Id") {
                override fun value(record: GridRecord): Int = record.id
            },
            object : DisplayColumnPre("Name") {
                override fun value(record: GridRecord): String = record.name
            },
            object : DisplayColumnPre("Number") {
                override fun value(record: GridRecord): String = record.number
            },
            object : DisplayColumn() {
                override val searchFunction: ((GridRecord) -> String)? = null
                override val comparator: Comparator<GridRecord> =
                    Comparator { a, b -> 10 * a.stuff.first.compareTo(b.stuff.first) + a.stuff.second.compareTo(b.stuff.second) }
                override val header: FC<Props> =
                    FC { +"This"; h.br {}; +"That" }

                override fun renderField(record: GridRecord): FC<Props> =
                    // You can do anything you want in here.
                    FC {
                        fun icon(s: Boolean) {
                            if (s) {
                                h.span {
                                    css { color = Red }
                                    +"✓"
                                }
                            } else {
                                h.span {
                                    css { color = Green }
                                    +"X"
                                }
                            }
                        }
                        icon(record.stuff.first)
                        h.br {}
                        icon(record.stuff.second)
                    }
            }
        )
        emptyMessage = FC {
            h.div {
                className = ClassName("alert alert-warning")
                h.em { +"No records." }
            }
        }
    }
}
