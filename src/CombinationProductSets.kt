package org.myprojects.hexany

import kotlin.math.pow

data class CPSXany(val cpsName: CPSName, val key: Int = cpsName.nameToKey(),
                   val scale:Scale = cpsName.cps(),
                   val origin: Fraction = 1.toFraction(),
                   val product: Fraction = cpsName.generators.fold(1){ acc, i -> acc * i }.toFraction()) {
    override fun toString(): String {
        return "{$cpsName, $scale |O:$origin, P:$product}\n"
    }
    fun stellation(full: Boolean = false): Mandala {
        if (full) {
            val genScale = Scale(cpsName.generators.map{it.toFraction()})
            val posScale = genScale.selfProduct(cpsName.deg)
            val negScale = genScale.inversion().selfProduct(cpsName.order - cpsName.deg).modulate(product)
            return Mandala(this, posScale.addition(negScale), true)}
        val posPoints = cpsName.generators.map { it.exp(cpsName.deg).toFraction() }
        val negPoints = cpsName.generators.map { product / it.exp(cpsName.order - cpsName.deg)}
        return Mandala(this, Scale(posPoints.union(negPoints).union(scale.notes).toList()))
    }
    fun cpsModulation(mediant: CPSXany):Modulation{
        return modulateCPS(CPSPair(mediant, this))
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
        return Scale(notes.sortedBy { it.num.toFloat() / it.div })
    }
}
class CPSName(generators: List<Int>, deg: Int = 2, order: Int = generators.size,
              val greek: String = greekName(order.nCk(deg))?:"WTFany") :
        Name(generators, deg, generators.size){
    override fun toString(): String{
        return "$generators($deg|$order):$greek"
    }

}

class Scale(val notes: List<Fraction>) {
    val intervals: List<List<Fraction>> by lazy {intervalsBySteps(notes)}
    override fun toString(): String {
        return notes.toString()
    }
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
                .sortedBy { it.num.toFloat() / it.div }.distinct()
    }
    fun intervalsBySteps(notes: List<Fraction>): List<List<Fraction>>{
        val size = notes.size
        val range = (1..(size/2))
        return range.map { this.nDegreeIntervals(it)}
    }
    fun inversion(): Scale{ return Scale(notes.map { it.invertFraction() })}
    fun selfProduct(n:Int):Scale {
        if (n <0) {return Scale(emptyList())}
        var scaleAcc = Scale(listOf(1.toFraction()))
        for (i in 0 until n) {
            scaleAcc = scaleAcc.listProduct(this)
        }
        return scaleAcc
    }
    fun listProduct(other: Scale): Scale{
        if (notes.isEmpty() or other.notes.isEmpty()){return Scale(emptyList())}
        val product = mutableListOf<Fraction>()
        for (a in notes){
            product.addAll(other.notes.map { a * it})
        }
        return Scale(product.sortedBy { it.num.toFloat()/it.div }.distinct())
    }
    fun addition(other: Scale): Scale{
        return Scale(notes.union(other.notes).toList().sortedBy { it.num.toFloat()/it.div })
    }
    fun modulate(interval: Fraction = 1.toFraction()):Scale {
        return Scale(notes.map{it * interval}.sortedBy { it.num.toFloat()/it.div })
    }
    fun toFactorScale(limit: Int = 10): FactorScale{
        return FactorScale(notes.map { it.factor(limit) })
    }

}



class Fraction(var num:Int = 1, var div:Int = 1) {
    override fun toString(): String {
        return "$num/$div"
    }
    override fun hashCode(): Int {
        return (num*div).toFloat().pow(2).toInt() / div.primeDivisors()
    }
    override fun equals(other: Any?): Boolean {
        if (other !is Fraction) {return false}
        return (num == other.num) and (div == other.div)
    }
    operator fun times(other: Any?): Fraction {
        if (other is Int) {
            return (other * num).toFraction(div)
        }
        if (other is Fraction) {
            return (num * other.num).toFraction( div * other.div)
        }
        return Fraction(0, 0)
    }
    operator fun div(other: Any?): Fraction{
        if (other is Int) {
            return num.toFraction(other*div)
        }
        if (other is Fraction) {
            return (num *other.div).toFraction(div*other.num)
        }
        return Fraction(0, 0)
    }
    fun invertFraction(): Fraction{
        return div.toFraction(num)
    }
    operator fun compareTo(other: Any?): Int {
        if (other is Fraction) {
            val fra1 = (num.toFloat()/div)
            val fra2 = (other.num.toFloat()/other.div)
            return  fra1.compareTo(fra2)
        }
        if (other is Int) {return compareTo(other.toFraction())}
        if (other is Float) {(num.toFloat()/div).compareTo(other)}
        throw IllegalArgumentException("Float, Int or Fraction required")
    }
    fun factor(limit:Int = 10): FactorNote{
        val factors = MutableList<Int>(limit) {0}
        if ((num == 0)or (div == 0))  {
            return FactorNote(this, factors.toList())}
        val fraction = mutableListOf(num, div)
        for (i in 0..limit){
            val prime = primes[i]
            while (fraction[0] % prime == 0) {
                factors[i] += 1
                fraction[0] /= prime
            }
            while (fraction[1] % prime == 0) {
                factors[i] -= 1
                fraction[1] /= prime
            }
        }
        return FactorNote(this, factors.toList())
    }
}

class CPSPair(val mediant: CPSXany, val flank: CPSXany) {
    override fun toString(): String {
        return "(${mediant.cpsName}|${flank.cpsName})"
    }
}

class Modulation(val name:CPSPair, val scale: Scale, val multiMod: Boolean = false) {
    override fun toString(): String {
        return if (multiMod) {"Multiple Modulation $name: ${scale.notes}"}
        else {"$name: ${scale.notes}"}
    }
}


class Mandala(val core: CPSXany, val stellations: Scale, val fullStellation: Boolean=false) {
    override fun toString(): String {
        return "${core.cpsName}: ${stellations.notes}"
    }
}

class Diamond(val name: Name, val key: Int = name.nameToKey(),
              val scale: Scale = name.cps(inverse= false)
                      .listProduct(name.cps(inverse=  true))){

}

class FactorNote(val name:Fraction, val factors:List<Int>){
    override fun toString(): String {
        return "$name: $factors"
    }
    fun add(other: FactorNote):FactorNote{
        val adFactor = this.factors.zip(other.factors) {a:Int, b:Int -> a+b}
        return FactorNote(adFactor.toFraction(), adFactor)
    }
    fun difference(other:FactorNote):FactorNote{
        val difFactor = this.factors.zip(other.factors) { a:Int, b:Int -> a-b}
        return FactorNote(difFactor.toFraction(), difFactor)
    }
}

class FactorScale(val notes:List<FactorNote>) {
    override fun toString(): String {
        return "$notes"
    }
    fun map(interval: FactorNote):IntervalMap{
        val facMap = mutableMapOf<FactorNote, FactorNote>()
        for (note in notes) {
            val note2 = note.add(interval)
            if (note2 in notes) {
                facMap.put(note, note2 )
            }
        }
        return IntervalMap(interval, facMap.toMap())
    }
    fun makeStructure(intervals:FactorScale = primeFactors):ScaleStructure {
        val maps = mutableListOf<IntervalMap>()
        for (int in intervals.notes) {
            val intMap = this.map(int)
            if (intMap.map.isNotEmpty()) {
                maps.add(intMap)
            }
        }
        return ScaleStructure(this, maps.toList())
    }
}

class IntervalMap(val interval: FactorNote, val map: Map<FactorNote, FactorNote>) {
    override fun toString(): String {
        return "$interval:$map\n"
    }
}

class ScaleStructure(val scale: FactorScale, val maps:List<IntervalMap>) {
    override fun toString(): String {
        return "$scale\n$maps"
    }
}



