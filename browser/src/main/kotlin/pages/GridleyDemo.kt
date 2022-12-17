package pages

import pages.gridley.Gridley
import react.FC
import react.Props
import kotlin.math.floor
import kotlin.random.Random

/**
 * Our custom record data type.
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

/**
 * This fronts the demo by creating some records.
 * The data in the records are intended to make it easy to narrow down the page count using search.
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
    }
}
