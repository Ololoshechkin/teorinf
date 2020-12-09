import java.io.File
import kotlin.experimental.and

typealias Data = List<Int>

val ALPHABET = 256

fun ux(whatToEnter: String, transform: (Data) -> Data) {
    println("Enter full absolute path to a file:")
    val path = readLine()
    if (path == null) {
        println("Sorry, you entered an empty or nul path. Exiting...")
        return
    }
    val file = File(path)
    if (!file.exists()) {
        println("Sorry, but a file with path \"$path\" does not exist. Exiting...")
        return
    }
    val data = try {
        file.readBytes()
    } catch (e: Exception) {
        println("Failed to read file.\nError: ${e.message}.\n Exiting...")
        return
    }.map { it.toInt() and 0xff }

    val transformed = transform(data)
    println("Enter $whatToEnter:")
    val name = readLine()
    if (name == null) {
        println("Sorry, you entered an empty or nul name. Exiting...")
        return
    }

    val outputFile = File(name)
    outputFile.createNewFile()

    outputFile.writeBytes(transformed.map { it.toByte() }.toByteArray())
}