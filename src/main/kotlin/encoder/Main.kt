package encoder

import ux

fun main() = ux("a short name for a new archive") { data ->
    println("initial : ${data.size} bytes")
    val (s, i) = barrows.encode(data)
    val m = mtf.encode(s)
    val result = monotone.encode(listOf(i) + m)
    println("encrypted : ${result.size} bytes")
    result
}