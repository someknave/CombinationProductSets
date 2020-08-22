package org.myprojects.hexany

import java.math.BigInteger

//import

fun main() {

}

fun modulateCPS(pair: CPSPair): Modulation {                //I have developed a new form of modulation but the margins of this
    val mediantCode = pair.mediant.key % 1.shl(24)  //code are too small to contain it. Please see Readme.md for theory.
    val flankCode = pair.flank.key % 1.shl(24)      //First this function takes the binary keys of each CPS and zeroes
    val medDeg = pair.mediant.cpsName.deg                   //the first 8 bits that are used to store the degree information.
    val flankDeg = pair.flank.cpsName.deg                   //The degree information is separately stored in the "medDeg" and
    val intersect = (mediantCode and flankCode)             //"flankDeg" values. The intersection between the generator sets
    val medFreedom = makeCPSName(mediantCode - intersect)   //is calculated by "bitwise and" on the binary codes. This
    val flankFreedom = makeCPSName(flankCode - intersect)   //intersection is removed from the mediant and flank codes
    val modulations = modulationsInner(intersect,               //to get the "Freedom", the generators outside the intersection.
            medDeg, medFreedom, flankDeg, flankFreedom)         //A separate  inner function takes these factors and generates a list of
    return Modulation(pair, pair.flank.scale.listProduct(modulations)) //modulations (transpositions) to be applied to the entire Flank CPS.
}

fun modulationsInner(intersect:Int,                         //This is the inner function that takes in the freedoms degrees and intersections
                     medDeg:Int, medFreedom:CPSName,        //and returns a list of modulations (transpositions) to apply to the Flank.
                     flankDeg:Int, flankFreedom:CPSName)    //A variable modulations is created as an empty scale. as modulations are generated they will be added to this scale.
        : Scale {                                           //Lets call the number of generators in the intersection k. For every possible degree "i" in the
    var modulations = Scale(emptyList())                    //intersection, (between 0 and k) we take the CPS of the medFreedom with degree
    for (i in 0..intersect.countBits()) {                    //"medDeg" - i, and the same with the inverse CPS of the flankFreedom. Both degrees require
        modulations = modulations.addition(                 //"deg-i" to be between 0 and the size of the freedom otherwise it will return
                medFreedom.cps(degree = medDeg - i).listProduct(    //an empty scale. It then takes a tensor product of the two scales, and adds the result to
                        flankFreedom.cps(true, flankDeg - i))) //intersection, (between 0 and k) we take the CPS of the medFreedom with degree
    }                                                       //"medDeg" - i, and the same with the inverse CPS of the flankFreedom. In either case this
    return modulations                                      //is added to the scale of modulations. Once the for loop is complete the resulting scale
}                                                            //of modulations is returned.

fun multiModulateCPS(pair: CPSPair): Modulation{
    val mediantCode = pair.mediant.key % 1.shl(24)
    val flankCode = pair.flank.key % 1.shl(24)
    val medDeg = pair.mediant.cpsName.deg
    val flankDeg = pair.flank.cpsName.deg
    val intersect = (mediantCode and flankCode)
    val flankFreCode = flankCode - intersect
    val flankFreedom = makeCPSName(flankFreCode)
    val interSize = intersect.countBits()
    val keys = (0..mediantCode).toList()
            .filter { it and mediantCode == it }
            .filter { it.countBits() == interSize}
    var scale = Scale(emptyList())
    for (key in keys) {
        val modulations = modulationsInner(key,  medDeg,
                makeCPSName(mediantCode-key),
                flankDeg, flankFreedom)
        val modifiedFlank = makeCPSName(key+flankFreCode).cps(false, flankDeg)
        scale = scale.addition(modulations.listProduct(modifiedFlank))
    }
    return Modulation(pair, scale, true)
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

fun makeCPS(key:Int): CPSXany {
    val name = makeCPSName(key)
    val notes = name.cps()
    return CPSXany(name, key, notes)
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

fun generateKeys(limit:Int = 14, deg: Int = 2 , order:Int = 4): List<Int> {
    val keys = (0..(1.shl(limit) - 1)).toList().map{deg.shl(24) + it}
    return keys.filter { (it and 16777215).countBits() == order }
}

fun genAllKeysTo(limit: Int = 14, deg: Int = 3, order: Int=6): List<Int> {
    val keyAcc = mutableListOf<Int>(0)
    for (i in 1..order){
        for (j in 1..deg.coerceAtMost(i)) {
            keyAcc.addAll(generateKeys(limit, j, i))
        }
    }
    return keyAcc.toList()
}

fun cpsInner(generators: List<Int>, deg:Int, start: Int, size: Int = generators.size, inverse: Boolean = false): List<Fraction> {
                                                    //Inner recursive function for creating Combination Product Sets. This function returns
    if (deg !in 0..(size - start)) {return emptyList()}  //a list of fractions that are each the product of "deg" many generators.
    if (deg == 0) { return listOf(1.toFraction())}      //First it checks for invalid inputs and returns an empty list in those cases.
    if (deg == 1) {                                     //If degree == 0 it returns list containing just the fraction 1/1.
        if (inverse) {                                  //True Base case degree==1 returns a list of fractions. This list contains elements
             return generators.subList(start, size).map{1.toFraction(it)} //from the generator list converted into fractions. These fractions
        }                                                               //are inverted if inverse is true. The sublist of generators include
        return generators.subList(start, size).map{it.toFraction()}     //all generators from index "start" to the last generator.
    }                                                                               //For higher degree cases, recursion is needed.
    val notes = mutableListOf<Fraction>()                                           //"notes" is initiated as an empty mutable list of Fractions.
    if (!inverse){                                                                  //The following section is split in to two nearly identical
        for (i in start..(size-deg)) {                                              //cases. If inversion is false, it creates a for loop
            val tempList = cpsInner(generators, deg-1, i+1, size, inverse) //that iterates over the generator list starting from index
                    .map { it * generators[i].toFraction() }                    //"start" and going to "size - deg" this leaves just enough
            notes.addAll(tempList)                                              //indexes for deeper levels of the recursion algorithm to
        }                                                                       //return with lists of Fractions.
        return notes                                                            //Within the For loop sets the generator to the "i"th index
    }                                                                           //and calls another instance of cpsInner. This new instance has
    for (i in start..(size-deg)) {                                              //"deg-1" degrees as a generator has already been set. it also has
        val tempList = cpsInner(generators, deg-1, i+1, size, inverse)  //a start of "i+1", this ensures that we don't produce the same combination
                .map { it * 1.toFraction(generators[i]) }                   //of generators twice. Each Fraction in the output is multiplied by the
        notes.addAll(tempList)                                              //current generator "i" and then this list is appended to the "notes" list
    }                                                                   //once the for loop is completed it returns the list "notes".
    return notes    //The only difference with the second case is that the List of notes is multiplied by the inversion of the generator.

}

    fun Int.nPk(k:Int = this ):BigInteger {           //permutation and factorial function only defined for k between 0 and
    if(k !in 0..this)  {return - BigInteger.ONE}   //non negative n (otherwise returns -1). The function multiplies together all
    return ((this- k + 1)..this).fold (BigInteger.ONE) {    //numbers between (n - k + 1) and n, by multiplying each number in
        acc, i -> acc * BigInteger.valueOf (i.toLong())     //that list onto the Accumulator. The Accumulator is initiated as 1.
    }                                               //this allows k==n to return a value of 1. Each number is converted to
}                                                   //the BigInteger type via Long type. This allows the total multiplication
                                                    //to reach very large numbers. Especially important for high n with low k.

fun Int.nCk(k: Int):Int {               //combinatorics function, divides permutations by k factorial. Only defined for k
    if(k !in 0..this) {return -1}       //between 0 and non negative n. otherwise returns -1.
    return (this.nPk(k)/k.nPk()).toInt()// returns nPk divided by k  factorial. Total converted back from BigInteger to Integer.
}
fun Int.nthBit(shift: Int): Int {        //simple function uses bit shifting to check the nth bit in binary notation.
    return this.shr(shift).and(1)
}
fun Int.countBits(): Int{               //Efficient algorithm for counting the number of 1 bits in the binary representation of an Int.
    var n = this                        //the bitwise and operation of an integer n with (n-1) returns n with its final 1 bit flipped to 0.
    var count = 0                       // this algorithm counts the number of times this operation needs to be performed before it reaches.
    while ( n > 0) {                    // 0. The number of operations = the number of 1 bits that needed to be flipped.
        n = n and (n - 1)
        count++
    }
    return count
}

fun Int.gcd(b:Int): Int {           //Efficient function for greatest common divisor (factor) uses Euclidean Algorithm.
    var i = this
    var j = b
    while (j > 0) {                 // repeatedly subtracts the smaller number from the bigger number and saves the results
        val m = i.coerceAtMost(j)   //min of i and j
        j = i + j - (m *2)          //the difference between the max and the min of i and j
        i = m                       //once the smaller number is 0 the other number must be the gcd.
    }
    return i
}

fun Int.primeDivisors(): Int{           //this function returns the product of the prime divisors of an integer. it is currently only used.
    var d = 1                           //by the Fraction hash code function. 12 will return 6, 300 wil return 30 etc.
    for(p in primes) {                  // it is fairly straight forward and it only cares about primes up to 29 as no higher primes can be
        if (this % p == 0) {            // generated. if the prime list is increased this will be updated also.
            d *= p
        }
    }
    return d
}
fun Int.leading1():Int{                 //this function returns the power of 2 under a given number. it does this using bit level operations.
    var mask = this                     //it makes a mask that will be used to zero out any 1s after the leading 1 in binary notation.
    for (i in listOf(1, 2, 4, 8, 16)){  //the mask starts as a copy of the original integer and the bitwise or function is used repeatedly
        mask = mask or mask.shr(i)      //with the mask shifted to the right in increasing amounts each time setting digits to the right
    }                                   //of the leading 1 to 1 as well.
    return this and (mask.shr(1).inv())  //finally the mask is shifted 1 space to the right and the bitwise and function is used
}                                       //with the inverted mask and the original number this will leave only the leading 1 as 1.

fun Int.exp(other:Int):Int{     //integer power function. Does not allow fractional or negative powers.
    if (other<0){return 0}
    var acc = 1
    for (i in 0 until other){
        acc *= this
    }
    return acc
}

fun Int.toFraction(other:Int = 1):Fraction{                                  //Integer to fraction converter can take divisor as an argument.
    if ((this == 0) or (other == 0)) {return Fraction(0, 0)}        //for the purpose of tuning theory 0 is nonsensical as
    var num = this                                                           //a numerator or as a divisor so either option is sent 0/0.
    var div = other                                //numerator and divisor are initialised from input, these are variables as they will
    if (num>=div) {                                 //be modified in the following steps.
        div *= (num / div).leading1()               //In tuning theory factors of 2 are generally seen as irrelevant as they only change
    } else {                                        //the octave, not the note name. This section multiplies either the numerator or
        num *= (div/num).leading1().shl(1)  //the divisor by a power of 2 to put the fraction in the range between 1 and 2.
    }                                               //This process ensures that all notes will be within the same octave.
    val gcd = num.gcd(div)                          //Finally both numerator and divisor are divided by the greatest common divisor
    num /= gcd                                      //To put them in the simplest form. This ensures that equal fractions will look the same.
    div /= gcd                                      //The first step removed 0s so we don't have to worry about divide by zero faults.
    return Fraction(num, div)                       //Simplest form of Fraction returned.
}

val primes = setOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)                      //current list of primes and factors needed in the CPS that the
val factors = listOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27)     //functions produce. could be expanded for higher prime limit.
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
fun greekName(n: Int): String? {            //this is a deliberately idiosyncratic name generator based on the names that Erv Wilson
    if(n<1){return greek[22]}               //had already chosen for various Combination Product Sets.
    if(n>=1000){return greek[23]}           //First the function returns stupid names for inputs out of the 1-999 range.
    var name = ""                           //The name is initialised as an empty string to built up depending on input
    val remainder = n % 100                 //Hundreds information is split from the remainder
    if (n>= 100) {                          //If the number is at least 100 and the remainder is 0 or 1 the name is instantly generated
        if (remainder <= 1) {return greek[-remainder] + greek[n-remainder]+"ny"}        //as these are special cases where there is nothing
        name = name + greek[n-remainder] + "kai"                    //between the hundreds and the suffix.
    }                                                               //Other numbers over 100 have the hundred segment added to the string.
    if (remainder <= 21) return name + greek[remainder]+"ny"        //Remainders under 22 can be added as is, and don't require additional
    val units = n%10                                                //processing. (numbers that are 1 + n hundred have already been managed.)
    if (units<=1) {return name + greek[-units] + greek[remainder - units] +"ny"}    //units are split from 10s info and numbers that
    name +=  greek[if(remainder<30) -20 else remainder-units]       //end in 1 or 0 are returned for similar reasons to the hundreds case.
    return name + greek[- units] + "ny"              //numbers in the 20s have a different form,the greek form of the tens is then added.
}                                                    //and finally the units and suffix are added and the name is returned.

