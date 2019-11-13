package com.example.nonogramapp.parse_nonogram

import com.example.nonogramapp.exceptions.SizeNotFoundException
import com.example.nonogramapp.exceptions.UnreadablePatternException
import com.example.nonogramapp.nonogram.Nonogram
import org.json.JSONException
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.io.BufferedReader
import java.util.*
import kotlin.collections.ArrayList

/**
 * This class reads the json file for a nonogram table, then returns the items labeled in the json.
 *
 * Required elements of a nonogram table file:
 *  - size (two numbers: height & width) or height & width (integers)
 *  - either:
 *      * pattern (of an array of strings)
 *      * pattern (of 1 string) & delimiter (1 Character >> Defaulted to '*')
 * Optional elements of a nonogram table file:
 *  - x & check (1 character) >> Defaulted to 0 & 1 respectively
 */
class NonogramReader(private val bufferedReader: BufferedReader): NonogramParser {

    private lateinit var nonogramJSONObject: JSONObject
    private val sizeRegex = Regex("""(\d).*(\d)""")

    /**
     * Reads the json file. Does *not* close the BufferedReader.
     */
    fun read(): Nonogram {
        try {
            nonogramJSONObject = retreiveJSONObject()
            return extractJSONElementsIntoNonogram()
        } catch (exception: Exception) {
            throw exception
        }
    }

    private fun retreiveJSONObject(): JSONObject {
        val jsonParser = JSONParser()
        try {
            return jsonParser.parse(bufferedReader) as JSONObject
        } catch (exception: ParseException) {
            throw exception
        }
    }


    private fun extractJSONElementsIntoNonogram(): Nonogram {
        try {
            val size = extractSize()
            val delimiter = extractDelimiter()
            val pattern = extractPattern(size, delimiter ?: '*')
            val xAndCheck = extractxAndCheck()
            val consecutiveChecks = extractConsecutiveChecks(size, pattern, xAndCheck ?: Pair('0', '1'))
            return Nonogram(size, pattern, consecutiveChecks)
        } catch (exception: Exception) {
            throw exception
        }
    }

    @Throws(SizeNotFoundException::class)
    private fun extractSize(): Pair<Int, Int> {
        try {
            (nonogramJSONObject["size"] as String).let { sizeString ->
                val sizeResult = sizeRegex.find(sizeString)
                // try to get numbers from the "size" string //
                try {
                    val height = sizeResult!!.groupValues[1]
                    val width = sizeResult.groupValues[2]
                    return Pair(height.toInt(), width.toInt())
                } catch(exception: Exception) {
                    throw SizeNotFoundException("Could not find width or height from 'size' in the jason file.")
                }
            }
        } catch (exception: JSONException) {
            // maybe the user used "height" and "width" instead of "size" //
            try {
                nonogramJSONObject.let {
                    return Pair(it["height"] as Int, it["width"] as Int)
                }
            } catch (exception: JSONException) {
                throw SizeNotFoundException("There was no height or width")
            }
        }
    }

    private fun extractDelimiter(): Char? {
        var delimiter: Char? = null
        nonogramJSONObject["delimiter"]?.let {
            (it as String).let {delimiterString ->
                if(delimiterString.length == 1)
                    delimiter = delimiterString[0]
            }
        }
        return delimiter
    }

    @Throws(UnreadablePatternException::class)
    private fun extractPattern(size: Pair<Int, Int>, delimiter: Char = '*'): Array<String> {
        val pattern = Array(size.first) { "" }
        (nonogramJSONObject["pattern"] as JSONArray).let {
            try {
                if(it.size != size.first) throw UnreadablePatternException("Can't read pattern. Only ${it.size} rows for height = ${size.first}")

                for (index in 0 until it.size) {
                    val patternLine = it[index] as String
                    if(patternLine.length != size.second) throw UnreadablePatternException("Can't read pattern. Wrong length on pattern line $index")

                    pattern[index] = patternLine
                }
            } catch(exception: Exception) {
                throw UnreadablePatternException("Can't read pattern. Is there not a string?")
            }
        }
            /*?: nonogramJSONObject.optString("pattern").let {
            // maybe the user used the string version of "pattern" instead of an array of strings //
            val patternLines = it.split(delimiter)
            if(patternLines.size != size.first) throw UnreadablePatternException("Can't read pattern. Only ${patternLines.size} rows for height = ${size.first}")

            for(index in patternLines.indices) {
                val patternLine = patternLines[index]
                if(patternLine.length != size.second) throw UnreadablePatternException("Can't read pattern. Wrong length on pattern line $index")

                pattern[index] = patternLine
            }
        }*/
        return pattern
    }

    private fun extractxAndCheck(): Pair<Char, Char>? {
        var xAndCheck: Pair<Char, Char>? = null

        val x = nonogramJSONObject["x"]
        val check = nonogramJSONObject["check"]

        if(x != null && check != null) xAndCheck = Pair((x as String)[0], (check as String)[0])

        return xAndCheck
    }

    /**
     * Reads the pattern, returning the numbers of each consecutive check in every row and column
     */
    private fun extractConsecutiveChecks(size: Pair<Int, Int>, pattern: Array<String>,
                               xAndCheck: Pair<Char, Char> = Pair('0', '1')): Pair<Array<ArrayList<Int>>, Array<ArrayList<Int>>> {
        // size = Pair<Height, Width> //
        val columnChecks = Array(size.first) { ArrayList<Int>() }
        val rowChecks = Array(size.second) { ArrayList<Int>() }
        val isConsecutiveRowCheck = Array(size.second) { false }
        val consecutiveRowIndex = Array(size.second) { -1 }

        var isConsecutiveColCheck = false

        var consecutiveColIndex = -1

        for(column in pattern.indices) {
            for(row in pattern[column].indices) {
                pattern[column][row].let {
                    if(it == xAndCheck.second) {
                        if (isConsecutiveColCheck) columnChecks[column][consecutiveColIndex]++
                        else {
                            consecutiveColIndex++
                            columnChecks[column].add(1)
                            isConsecutiveColCheck = true
                        }

                        if(isConsecutiveRowCheck[row]) rowChecks[row][consecutiveRowIndex[row]]++
                        else {
                            consecutiveRowIndex[row]++
                            rowChecks[row].add(1)
                            isConsecutiveRowCheck[row] = true
                        }
                    } else {
                        isConsecutiveColCheck = false
                        isConsecutiveRowCheck[row] = false
                    }
                }
            }
            consecutiveColIndex = -1
        }

        return Pair(rowChecks, columnChecks)
    }
}