package org.myprojects.hexany

import java.io.FileWriter
import java.io.IOException
/*
private val CSV_HEADER = "id, name1, name2, name3, name4, name5, name6, note1, note2, note3, note4, note5, note 6, note7, note8, note9, note10, note11, note12, note13, note14, note15, isProper, isCS, isProperWithOrigin, isCSWithOrigin, isProperWithProduct, isCSWithProduct, isProperWithOandP, isCSWithOandP, scaleSize, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27"

fun printPendexCSV(pendexanies: List<CPSXany>, filename: String = "Pendexany.csv") {

    var fileWriter: FileWriter? = null

    try {
        fileWriter = FileWriter(filename)
        fileWriter.append(CSV_HEADER)
        fileWriter.append('\n')

        for (pendexany in pendexanies) {
            val scale = intervalsBySteps(pendexany.notes)
            val scale2 = intervalsBySteps(pendexany.notes.union(listOf(pendexany.origin)).sortedBy { it.numerator.toFloat() / it.denominator }.toList())
            val scale3 = intervalsBySteps(pendexany.notes.union(listOf(pendexany.product)).sortedBy { it.numerator.toFloat() / it.denominator }.toList())
            val scale4 = intervalsBySteps(pendexany.notes.union(listOf(pendexany.origin, pendexany.product)).sortedBy { it.numerator.toFloat() / it.denominator }.toList())
            fileWriter.append(pendexany.key.toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.cpsName.generators.elementAtOrNull(0).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.cpsName.generators.elementAtOrNull(1).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.cpsName.generators.elementAtOrNull(2).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.cpsName.generators.elementAtOrNull(3).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.cpsName.generators.elementAtOrNull(4).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.cpsName.generators.elementAtOrNull(5).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(0).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(1).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(2).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(3).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(4).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(5).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(6).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(7).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(8).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(9).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(10).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(11).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(12).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(13).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.elementAtOrNull(14).toString())
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
            fileWriter.append(isProper(scale4).toString())
            fileWriter.append(',')
            fileWriter.append(isConstantStructure(scale4).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.notes.size.toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(0).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(1).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(2).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(3).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(4).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(5).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(6).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(7).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(8).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(9).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(10).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(11).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(12).toString())
            fileWriter.append(',')
            fileWriter.append(pendexany.key.nthBit(13).toString())
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

 */