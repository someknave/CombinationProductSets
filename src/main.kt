package org.myprojects.hexany

import java.math.BigInteger
import kotlin.math.log2
import kotlin.math.pow
//import

val factors = listOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27)

fun main() {

}

fun intervalsBySteps (notes: List<Fraction>): List<List<Fraction>>{
    val size = notes.size
    val range = (1..(size/2))
    return range.map { nDegreeIntervals(notes, it)}
}
/*
fun multiModulateHexany(pair: HexanyPair): MultiModulation{
    val medkey = pair.mediant.key
    val flakey = pair.flank.key
    val shared = medkey and flakey
    if (countBits(shared) != 3) {return MultiModulation(pair.mediant, 0, emptyList(), pair.flank)
    }
    val flaktor = (flakey - (shared))
    val dekany = CPSXany((makeHexName(medkey+flaktor)), medkey+flaktor)
    val multiModulation = dekany.notes
            .union(cps(pair.mediant.name, 3).map{it / factors[binaryPower(flaktor)]})
            .toList().sortedBy {it.numerator.toFloat() / it.denominator}
    return MultiModulation(pair.mediant, factors[binaryPower(flaktor)], multiModulation, dekany )
}
*/


fun modulateCPS(pair: CPSPair): Modulation {
    val mediantCode = pair.mediant.key
    val flankCode = pair.flank.key
    val medDeg = mediantCode.shr(24)
    val flankDeg = flankCode.shr(24)
    val intersect = (mediantCode and flankCode) % 1.shr(24)
    val medFreedom = makeCPSName(mediantCode - intersect).generators
    val flankFreedom = makeCPSName(flankCode-intersect).generators
    val modulations = mutableListOf<Fraction>()
    for (i in 0..countBits(intersect)){
        modulations.addAll(listProduct(
                cpsInner(medFreedom, medDeg - i, 0 ),
                cpsInner(flankFreedom, flankDeg - i, 0, inverse = true)))
    }
    return Modulation(pair, listProduct(pair.flank.notes, modulations).distinct())
}

fun listProduct(listA: List<Fraction>, listB: List<Any>): List<Fraction>{
    if (listA.isEmpty() or listB.isEmpty()){return emptyList()}
    val product = mutableListOf<Fraction>()
    for (a in listA){
        product.addAll(listB.map { a * it})
    }
    return product.sortedBy { it.numerator.toFloat()/it.denominator }
}


fun makeCPSPairs(polyanies: List<CPSXany>): List<CPSPair> {
    val cpsPairs = mutableListOf<CPSPair> ()
    val size = polyanies.size
    for (i in 0 until (size - 1) ){
        for (j in (i + 1) until size){
            cpsPairs.add(CPSPair(mediant = polyanies[i], flank = polyanies[j]))
            cpsPairs.add(CPSPair(mediant = polyanies[j], flank = polyanies[i]))
        }
    }
    cpsPairs.sortBy { it.mediant.key }
    return cpsPairs
}

fun makeCPS(hexKey:Int, order: Int = 2): CPSXany {
    val name = makeCPSName(hexKey)
    val notes = cps(name)
    return CPSXany(name, hexKey, notes)
}

fun makeCPSName(key:Int, order:Int = 14): CPSName{
    val name = mutableListOf<Int>()
    for (factor in 0 until order) {
        if (key.nthBit(factor) ==
                1) {
            name.add(factors[factor])
        }
    }
    return CPSName(name, key.shr(24))
}

fun generateKeys(factors:Int = 14, deg: Int = 2 , order:Int = 4): List<Int> {
    val keys = (0..(2.0.pow(factors) - 1).toInt() + deg.shl(24)).toList()
    return keys.filter { countBits(it) == order }
}

fun nDegreeIntervals(notes: List<Fraction>, n: Int): List<Fraction> {
    val size = notes.size
    return notes
            .mapIndexed{index, frac ->  frac.invertFraction() * notes[(n + index) % size]}
            .sortedBy { it.numerator.toFloat() / it.denominator }
            .toSet().toList()
}

fun countBits(a: Int): Int{
    var n = a % 1.shr(24)
    var count = 0
    while ( n > 0) {
        n = n and (n - 1)
        count++
    }
    return count
}



fun Int.nthBit(shift: Int): Int {
    return this.shr(shift).and(1)
}

fun binaryPower(num: Int): Int {
    return log2(num.toFloat()).toInt()
}

fun cps(name: CPSName, inverse: Boolean = false): List<Fraction>{
    val notes = cpsInner(name.generators, name.deg, 0, inverse= inverse)
    return notes.sortedBy { it.numerator.toFloat() / it.denominator }.toSet().toList()
}

fun cpsInner(generators: List<Int>, deg:Int, start: Int, size: Int = generators.size, inverse: Boolean = false): List<Fraction> {
    if ((deg < 0) or (deg + start >= size)) {return emptyList()}
    if (deg == 0) { return listOf(Fraction(1))}
    if (deg == 1) {
        if (inverse) {
            return generators.subList(start, size).map { Fraction(it).invertFraction() }
        }
        return generators.subList(start, size).map { Fraction(it) }
    }
    val notes = mutableListOf<Fraction>()
    for (i in start..(size-deg)) {
        val tempList = cpsInner(generators, deg-1, i+1, size, inverse).map { it * generators[i] }
        notes.addAll(tempList)
    }
    return notes

}

fun nPk(n:Int,k:Int = 0 ):BigInteger { //k=0 returns n factorial
    require(k in 0..n)  {"k not in range 0 to n"}
    return ((n- k + 1)..n).fold (BigInteger.ONE) { //converts accumulator to BigInteger to handle large numbers
        acc, i -> acc * BigInteger.valueOf (i.toLong())
    }
}

fun nCk(n: Int, k: Int):Int {
    require(k in 0..n) {"k not in range 0 to n"}
    return (nPk(n, k) / nPk(k, 0)).toInt()// returns nPk divided by k  factorial converted to integer
}

fun gcd(a:Int, b:Int): Int {
    var i = a
    var j = b
    while (j > 0) {
        val m = i.coerceAtMost(j) //min of i and j
        j = i + j - (m *2) //the difference between the max and the min of i and j
        i = m
    }
    return i
}

val primes = setOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)

fun isConstantStructure (scale: List<List<Fraction>>): Boolean{
    val size = scale.size
    val intersects = mutableListOf<Set<Fraction>>()
    for (i in 0 until (size - 1)) {
        for (j in (i+1) until size) {
            val set = scale[i].intersect(scale[j])
            if (set.isNotEmpty()) {
                intersects.add(set)
            }
        }
    }
    return intersects.isEmpty()
}

fun isProper (scale: List<List<Fraction>>): Boolean{
    val size = scale.size - 1
    for (i in 0 until size) {
        if (scale[i].last() > scale[i + 1][0]) {
            return false
        }
    }
    return true
}

fun primeDivisors(num: Int): Int{
    var d = 1
    for(p in primes) {
        if (num % p == 0) {
            d *= p
        }
    }
    return d
}

fun Int.powerOf2():Int {
    return 2.0.pow(log2(this.toFloat()).toInt()).toInt()
}
fun Float.powerOf2():Int {
    return 2.0.pow(log2(this).toInt()).toInt()
}
fun nameToKey(name: List<Int>, deg: Int=2):Int {
    var key = deg.shl(24)
    for (i in name) {
        val pow = factors.indexOf(i)
        if(pow == -1) {throw IllegalArgumentException("Invalid name passed to nameToKey")}
        key+= 1.shl(pow)
    }
    return key
}

fun stellateHexany(hex: CPSXany): Mandala{
    if(hex.cpsName.order != 4) {return Mandala(hex, emptyList())}
    val posPoints = hex.cpsName.generators.map { it-> Fraction(it*it) }
    val negPoints = posPoints.map { it-> hex.product/it }
    val allPoints = posPoints.union(negPoints).union(hex.notes).sortedBy { it.numerator.toFloat() / it.denominator }
    return Mandala(hex, allPoints)
}
/*
fun stellateDekany(dek: CPSXany): Mandala{

}
*/
val greek: Map<Int, String> = mapOf(1 to "Mono", 2 to "Die", 3 to "Tria", 4 to "Tetra", 5 to "Penta",
        6 to "Hexa", 7 to "Hepta", 8 to "Okta", 9 to "Ennea", 10 to "Deka", 11 to "Hendeka", 12 to "Dodeka",
        13 to "Triskaideka", 14 to "Tetradeka", 15 to "Pendeka", 16 to "Hekkaideka", 17 to "Heptadeka",
        18 to "Oktadeka", 19 to "Enkaideka", 20 to "Eikosa" ,-20 to "Eikosi", -1 to "Heiskai", -2 to "kaiDie",
        -3 to "kaiTria", -4 to "kaiTetra", -5 to "kaiPenta",-6 to "kaiHexa", -7 to "kaiHepta", -8 to "kaiOkta",
        -9 to "kaiEnnea", 30 to "Trikonta", 40 to "Tessarakonta", 50 to "Pentekonta", 60 to "Hexekonta",
        70 to "Hebdomekonta", 80 to "Ogdoekonta", 90 to "Enenekonta", 100 to "Hekato", 200 to "Dikosio",
        300 to "Triakosio", 400 to "Tetrakosio", 500 to "Pentakosio", 600 to "Hexakosio", 700 to "Heptakosio",
        800 to "Oktakosio", 900 to "Enneakosio",21 to "HeiskaiEikosa", 22 to "NoIdeany", 23 to "VeryBigany", 0 to ""
)
fun greekName(n: Int): String? {
    if(n<1){return greek[22]}
    if(n>=1000){return greek[23]}
    var name:String = ""
    val remainder = n % 100
    if (n>= 100) {
        if (remainder <= 1) {return greek[-remainder] + greek[n-remainder]+"ny"}
        name = name + greek[n-remainder] + "kai"
    }
    if (remainder <= 21) return name + greek[remainder]+"ny"
    val units = n%10
    if (units==1) name = name + greek[-1]
    name = name + greek[if(remainder<30) -20 else remainder-units]
    if (units > 1) return name + greek[- units] + "ny"
    return name + "ny"
}