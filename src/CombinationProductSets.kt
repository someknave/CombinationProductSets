package org.myprojects.hexany

import kotlin.math.pow

data class CPSXany(val cpsName: CPSName, val key: Int = cpsName.nameToKey(),
                   val notes:Scale = cpsName.cps(),
                   val origin: Fraction = Fraction(1),
                   val product: Fraction = Fraction(cpsName.generators.reduce{ acc, i -> acc * i })) {
    override fun toString(): String {
        return "{$cpsName, $notes |O:$origin, P:$product}\n"
    }

}


open class Name(val generators: List<Int>, val deg: Int = 1, val order:Int = generators.size){
    override fun toString(): String {
        return "$generators($deg|$order)"
    }
    fun nameToKey():Int {
        var key = deg.shl(24)
        for (i in generators) {
            val pow = factors.indexOf(i)
            if(pow == -1) {throw IllegalArgumentException("Invalid name passed to nameToKey")}
            key+= 1.shl(pow)
        }
        return key
    }
    fun cps(inverse: Boolean = false, degree: Int = deg): Scale{
        val notes = cpsInner(generators, degree, 0, inverse= inverse)
        return Scale(notes.sortedBy { it.numerator.toFloat() / it.denominator })
    }
}
class CPSName(generators: List<Int>, deg: Int = 2, order: Int = generators.size,
              val greek: String = greekName(nCk(deg, order))?:"WTFany") :
        Name(generators, deg, generators.size){
    override fun toString(): String{
        return "$generators($deg|$order):$greek"
    }

}

class Scale(val notes: List<Fraction>) {
    val intervals: List<List<Fraction>> by lazy {intervalsBySteps(notes)}
    fun isConstantStructure(): Boolean {
        val size = this.notes.size
        for (i in 0 until (size - 1)) {
            for (j in (i + 1) until size) {
                if (this.intervals[i].intersect(this.intervals[j]).isNotEmpty()) {
                    return false
                }
            }
        }
        return true
    }
    fun isProper(): Boolean {
        val size = intervals.size - 1
        for (i in 0 until size) {
            if (intervals[i].last() > intervals[i + 1][0]) {
                return false
            }
        }
        return true
    }
    fun nDegreeIntervals(n: Int): List<Fraction> {
        val size = notes.size
        return notes
                .mapIndexed{index, frac ->  frac.invertFraction() * notes[(n + index) % size]}
                .sortedBy { it.numerator.toFloat() / it.denominator }.distinct()
    }
    fun intervalsBySteps(notes: List<Fraction>): List<List<Fraction>>{
        val size = notes.size
        val range = (1..(size/2))
        return range.map { this.nDegreeIntervals(it)}
    }
    fun listProduct(other: Scale): Scale{
        if (notes.isEmpty() or other.notes.isEmpty()){return Scale(emptyList())}
        val product = mutableListOf<Fraction>()
        for (a in notes){
            product.addAll(other.notes.map { a * it})
        }
        return Scale(product.sortedBy { it.numerator.toFloat()/it.denominator }.distinct())
    }

}



class Fraction(val numerator:Int = 1, var denominator:Int = 1) {
    init {
        val order = (numerator.toFloat()/denominator).powerOf2()
        denominator *= order
    }
    override fun toString(): String {
        return "$numerator/$denominator"
    }
    override fun hashCode(): Int {
        return (numerator*denominator).toFloat().pow(2).toInt() / primeDivisors(denominator)
    }
    override fun equals(other: Any?): Boolean {
        if (other !is Fraction) {return false}
        return (numerator == other.numerator) and (denominator == other.denominator)
    }
    operator fun times(other: Any?): Fraction {
        if (other is Int) {
            return times(Fraction(numerator = other))
        }
        if (other is Fraction) {
            val num = numerator * other.numerator
            val den = denominator * other.denominator
            val d = gcd(num, den)
            val ord = (num.toFloat()/den).powerOf2()
            if ((num/d) % 2 == 0) { return Fraction(num/d/ord, den/d)}
            return Fraction(num/d, den*ord/d)
        }
        return Fraction(0, 0)
    }
    operator fun div(other: Any?): Fraction{
        if (other is Int) {
            return times(Fraction(other).invertFraction())
        }
        if (other is Fraction) {
            return times(other.invertFraction())
        }
        return Fraction(0, 0)
    }
    fun invertFraction(): Fraction{
        if (numerator % 2 == 0) {return Fraction( denominator , numerator/2)}
        return Fraction(denominator * 2, numerator)
    }
    operator fun compareTo(other: Any?): Int {
        if (other is Fraction) {
            val fra1 = (numerator.toFloat()/denominator)
            val fra2 = (other.numerator.toFloat()/other.denominator)
            return  fra1.compareTo(fra2)
        }
        if (other is Int) {return compareTo(Fraction(other))}
        if (other is Float) {(numerator.toFloat()/denominator).compareTo(other)}
        throw IllegalArgumentException("Float, Int or Fraction required")
    }
}

class CPSPair(val mediant: CPSXany, val flank: CPSXany) {
    override fun toString(): String {
        return "(${mediant.cpsName}|${flank.cpsName})"
    }
}

class Modulation(val name:CPSPair, val notes: Scale) {
    override fun toString(): String {
        return "$name: ${notes.notes}"
    }
}


class Mandala(val core: CPSXany, val stellations: Scale) {
    override fun toString(): String {
        return "${core.cpsName}: ${stellations.notes}"
    }
}

class Diamond(val name: Name, val key: Int = name.nameToKey(),
              val notes: Scale = name.cps(inverse= false)
                      .listProduct(name.cps(inverse=  true))){

}



