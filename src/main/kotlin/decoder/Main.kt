package decoder

import ux

fun main() = ux("output file name") { data ->
    val d = monotone.decode(data)
    val i = d[0]
    val m = d.drop(1)
    val s = mtf.decode(m)
    val result = barrows.decode(s, i)

    result
}