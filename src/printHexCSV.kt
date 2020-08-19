package org.myprojects.hexany

import java.io.FileWriter
import java.io.IOException

private val CSV_HEADER = "id, name1, name2, name3, name4, note1, note2, note3, note4, note5, note 6, isProper, isCS, isProperWithOrigin, isCSWithOrigin, isProperWithOandP, isCSWithOandP,scaleSize, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27"

fun printHexCSV(hexanies: List<CPSXany>, filename: String = "Hexany.csv") {

    var fileWriter: FileWriter? = null

    try {
        fileWriter = FileWriter(filename)

        fileWriter.append(CSV_HEADER)
        fileWriter.append('\n')

        for (hexany in hexanies) {
            val scale = intervalsBySteps(hexany.notes)
            val scale2 = intervalsBySteps(hexany.notes.union(listOf(hexany.origin)).sortedBy { it.numerator.toFloat() / it.denominator }.toList())
            val scale3 = intervalsBySteps(hexany.notes.union(listOf(hexany.origin, hexany.product)).sortedBy { it.numerator.toFloat() / it.denominator }.toList())
            fileWriter.append(hexany.key.toString())
            fileWriter.append(',')
            fileWriter.append(hexany.cpsName.generators[0].toString())
            fileWriter.append(',')
            fileWriter.append(hexany.cpsName.generators[1].toString())
            fileWriter.append(',')
            fileWriter.append(hexany.cpsName.generators[2].toString())
            fileWriter.append(',')
            fileWriter.append(hexany.cpsName.generators.elementAtOrNull(3).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.notes.elementAtOrNull(0).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.notes.elementAtOrNull(1).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.notes.elementAtOrNull(2).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.notes.elementAtOrNull(3).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.notes.elementAtOrNull(4).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.notes.elementAtOrNull(5).toString())
            fileWriter.append(',')
            fileWriter.append(isProper(scale).toString())
            fileWriter.append(',')
            fileWriter.append(isConstantStructure(scale).toString())
            fileWriter.append(',')
            fileWriter.append(isProper(scale2).toString())
            fileWriter.append(',')
            fileWriter.append(isConstantStructure(scale2).toString())
            fileWriter.append(',')
            fileWriter.append(isProper(scale3).toString())
            fileWriter.append(',')
            fileWriter.append(isConstantStructure(scale3).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.notes.size.toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(0).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(1).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(2).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(3).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(4).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(5).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(6).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(7).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(8).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(9).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(10).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(11).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(12).toString())
            fileWriter.append(',')
            fileWriter.append(hexany.key.nthBit(13).toString())
            fileWriter.append('\n')
        }

        println("Write CSV successfully!")
    } catch (e: Exception) {
        println("Writing CSV error!")
        e.printStackTrace()
    } finally {
        try {
            fileWriter!!.flush()
            fileWriter.close()
        } catch (e: IOException) {
            println("Flushing/closing error!")
            e.printStackTrace()
        }
    }
}