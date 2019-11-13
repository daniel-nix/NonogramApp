package com.example.nonogramapp.nonogram

data class Nonogram(private val size: Pair<Int, Int>,
                    private val pattern: Array<String>,
                    private val consecutiveChecks: Pair<Array<ArrayList<Int>>, Array<ArrayList<Int>>>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Nonogram

        if (size != other.size) return false
        if (!pattern.contentEquals(other.pattern)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + pattern.contentHashCode()
        return result
    }
}