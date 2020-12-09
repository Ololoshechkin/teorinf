@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import java.io.File
import kotlin.math.log

fun entropy(xs: List<Double>) = -xs.map { x -> x * log(x, 2.0) }.sum()

fun printEntropy(data: Data) {
    val d = hashMapOf<Int, Int>()
    val d_2 = hashMapOf<Pair<Int, Int>, Int>()
    val d_condition = hashMapOf<Int, HashMap<Int, Int>>()
    val d_condition_2 = hashMapOf<Pair<Int, Int>, HashMap<Int, Int>>()

    var filelen = 0

    var prev_byte = -1
    var byte_before_prev = -1

    for (byte in data) {
        d[byte] = d.getOrDefault(byte, 0) + 1
        filelen += 1

        if (prev_byte != -1) {
            val p = Pair(prev_byte, byte)
            d_2[p] = d_2.getOrDefault(p, 0) + 1
            d_condition[prev_byte] = d_condition.getOrDefault(prev_byte, hashMapOf())
            d_condition[prev_byte]!![byte] = d_condition[prev_byte]!!.getOrDefault(byte, 0) + 1
        }

        if (byte_before_prev != -1) {
            val p = Pair(byte_before_prev, prev_byte)
            d_condition_2[p] = d_condition_2.getOrDefault(p, hashMapOf())
            d_condition_2[p]!![byte] = d_condition_2[p]!!.getOrDefault(byte, 0) + 1
        }

        byte_before_prev = prev_byte
        prev_byte = byte
    }

    val entropy_value = entropy(d.values.map { it.toDouble() / filelen })

    var conditional_entropy = 0.0

    for ((k, v) in d_condition.entries) {
        conditional_entropy += (d[k]!!.toDouble() / filelen) * entropy(v.values.map{ it.toDouble() / d[k]!! })
    }
    var conditional_entropy_2 = 0.0
    for ((k, v) in d_condition_2.entries) {
        conditional_entropy_2 += (d_2[k]!!.toDouble() / (filelen - 1.0)) * entropy(v.values.map { it.toDouble() / d_2[k]!! })
    }

    println("   H(X) : $entropy_value")
    println("   H(X|X) : $conditional_entropy")
    println("   H(X|XX) : $conditional_entropy_2")
}

fun encrypt(data: Data): Data {
    val (s, i) = barrows.encode(data)
    val m = mtf.encode(s)
    return monotone.encode(listOf(i) + m)
}

fun decrypt(data: Data): Data {
    val d = monotone.decode(data)

    val i = d[0]
    val m = d.drop(1)

    val s = mtf.decode(m)

    return barrows.decode(s, i)
}

fun main() {
    println("encryption:")
    var totalCnt = 0
    File("/Users/Vadim.Briliantov/Downloads/calgarycorpus/")
        .listFiles()
        .forEach { file ->
            println("- ${file.name}")

            val data = file.readBytes().map { it.toInt() and 0xff }

            println("   initial : ${data.size} bytes")
            var result = encrypt(data)

            val result0 = encrypt(listOf(0) + data)

            var cnt = 1
            var result2 = encrypt(listOf(cnt) + result)
            var bestResult = result0
            while (result2.size < bestResult.size && cnt < 256) {
                bestResult = result2
                cnt++
                result = encrypt(result)
                result2 = encrypt(listOf(cnt) + result)
            }
            println(cnt)
            totalCnt += cnt

            println("   encrypted : ${bestResult.size} bytes")
            println("   difference : ${100.0 * (1.0 - result.size.toDouble() / data.size.toDouble())} %")

            val output = File("output/${file.name}.a")
            output.createNewFile()
            output.writeBytes(bestResult.map { it.toByte() }.toByteArray())
        }
    println("total cnt : $totalCnt")

    println()
    println("decryption:")
    File("/Users/Vadim.Briliantov/Downloads/calgarycorpus/")
        .listFiles()
        .map { File("output/${it.name}.a") }
        .forEach { file ->
            println("- ${file.name}")
            val data = file.readBytes().map { it.toInt() and 0xff }

            println("   initial : ${data.size} bytes")
            var result = decrypt(data)

            val cnt = result[0]
            result = result.drop(1)
            repeat(cnt) {
                result = decrypt(result)
            }

            println("   decrypted : ${result.size} bytes")

            val output = File("output/${file.name.removeSuffix(".a")}")
            output.createNewFile()
            output.writeBytes(result.map { it.toByte() }.toByteArray())
        }
    println()
    println("verify:")

    var total_comp = 0L

    File("/Users/Vadim.Briliantov/Downloads/calgarycorpus/")
        .listFiles()
        .forEach { file ->
            println("- ${file.name}")
            val data1 = file.readBytes()
            val data2 = File("/Users/Vadim.Briliantov/untitled2/output/${file.name}").readBytes()

            println("   equals : ${data1.contentEquals(data2)}")
            printEntropy(data1.map { it.toInt() })

            val compressed = File("/Users/Vadim.Briliantov/untitled2/output/${file.name}.a").readBytes()
            println("   initial len : ${data1.size}")
            println("   compressed len : ${compressed.size}")
            println("   avg. len per symbol : ${compressed.size.toDouble() * 8.0 / data1.size.toDouble()}")
            total_comp += compressed.size
        }
    println("total compressed size : $total_comp bytes")
}