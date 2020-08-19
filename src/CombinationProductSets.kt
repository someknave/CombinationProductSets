package org.myprojects.hexany

import kotlin.math.pow

data class CPSXany(val cpsName: CPSName, val key: Int = nameToKey(cpsName.generators, cpsName.deg),
                   val notes: List<Fraction> = cps(cpsName),
                   val origin: Fraction = Fraction(1),
                   val product: Fraction = Fraction(cpsName.generators.reduce{ acc, i -> acc * i })) {
    override fun toString(): String {
        return "{$cpsName, $notes |O:$origin, P:$product}\n"
    }

}

class CPSName(val generators: List<Int>, val deg: Int = 2, val order:Int = generators.size,
              greek:String = greekName(nCk(order, deg))?: "WTFany"){
    override fun toString(): String {
        return "$generators($deg|$order):${greek}"
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

class Modulation(val name:CPSPair, val notes: List<Fraction>) {
    override fun toString(): String {
        return "$name: $notes"
    }
}


class Mandala(val core: CPSXany, val stellations: List<Fraction>) {
    override fun toString(): String {
        return "${core.cpsName}: $stellations"
    }
}

class Diamond(val generators: List<Int>,
              val deg: Int = 1,
              val key: Int = nameToKey(generators, deg),
              val notes: List<Fraction> = listProduct(
                                cpsInner(generators, deg, 0, inverse= false),
                                cpsInner(generators, deg, 0, inverse=  true))
                      .sortedBy { it.numerator.toFloat() / it.denominator}.distinct()
)


