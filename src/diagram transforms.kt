package org.myprojects.hexany
/*
import java.awt.Color
import kotlin.math.*

class XYDouble (val x:Double, val y:Double){
    fun logReduce(scalar:Int = 108, xAdjust:Fraction = 3.toFraction(2),
                  yAdjust:Fraction = 5.toFraction(4)):Double {
        return (log(x, xAdjust.toDouble()) + log(y, yAdjust.toDouble()))*scalar
    }
    fun toXYCoords() = XYCoordinates(x.roundToInt(), y.roundToInt())
}

class FractionLines(val fraction: Fraction, val scalar:Int = 108, val xAdjust:Fraction = fracs[1],
                    val yAdjust:Fraction = fracs[2], val period: Period = octave) {
    private val fracDouble = fraction.toDouble()
    private val xDouble = xAdjust.toDouble()
    private val yDouble = yAdjust.toDouble()
    private val yIntercept = log(fracDouble,yDouble) * scalar
    private val slope = -log(xDouble, yDouble)
    private val pAdjust = log(period.toDouble(), yDouble) * scalar
    fun getCoords(theta: Double, k:Int = 0):XYDouble {
        if (theta.div(180.0) == 90.0) {
            return XYDouble ((yIntercept + k * pAdjust) * scalar, 0.0)
        }
        val newSlope = tan(theta * PI/180)
        val x = (yIntercept + k * pAdjust) * scalar/(newSlope - slope)
        return XYDouble(x, x * newSlope)
    }
    fun listCoords(theta:Double, kVals:IntRange = -2..3):List<XYDouble>{
        if (theta.div(180.0) == 90.0) {
            return kVals.map{XYDouble((yIntercept + it* pAdjust) * scalar, 0.0)}
        }
        val newSlope = tan(theta* PI/180)
        val xvals = kVals.map { (yIntercept + it * pAdjust) * scalar/(newSlope - slope) }
        return xvals.map{ XYDouble(it, it*newSlope)}
    }
}
data class PolarRep(val name:Fraction, var theta:Double = 0.0, var k:Int =0 ){

}

class GeneralDistanceMap(val intervalFractions: List<Fraction>, val scalar: Int = 108, val xAdjust: Fraction = fracs[1],
                         val yAdjust: Fraction = fracs[2], val period: Period = octave){
    private val doubles = intervalFractions.map{it.toDouble()}
    private val xDouble = xAdjust.toDouble()
    private val yDouble = yAdjust.toDouble()
    private val yIntercepts = doubles.map {log(it,yDouble) * scalar}
    private val slope = -log(xDouble, yDouble)
    private val gtheta = (atan(slope) * 180 / PI).div(180.0)
    private val pAdjust = log(period.toDouble(), yDouble) * scalar
    private val intFactors = intervalFractions.map{it.factor(25)}
    fun getXYFacMap(polarFracs:List<PolarRep>):XYFacMap {
        var xMap = mutableMapOf<FactorNote, Int>()
        var yMap = mutableMapOf<FactorNote, Int>()
        for (fraction in polarFracs) {
            var x = 0.0
            var y = 0.0
            val periodAdj = fraction.k * pAdjust
            val theta = if(fraction.theta - gtheta in -5.0..5.0) { gtheta - 5.0 } else { fraction.theta }
            val slopeBase =  tan(theta*PI/180) - slope
            val yMult = tan(theta*PI/180) / slopeBase
            val factorNote = fraction.name.factor(25)
            val factors = factorNote.factors
            for (i in factors.indices) {
                if (factors[i]!=0){
                    val p = factors[i]
                    for (j in intFactors.indices){
                        when (theta) {
                            0.0 -> x -= (yIntercepts[i] + periodAdj) / slope
                            90.0 -> y += yIntercepts[i] + periodAdj
                            else -> {
                                x += (yIntercepts[i] + periodAdj) / slopeBase
                                y += (yIntercepts[i] + periodAdj) * yMult
                            }
                        }
                    }
                }
            }
            xMap.put(factorNote, x.roundToInt())
            yMap.put(factorNote, y.roundToInt())
        }
        return XYFacMap(xMap, yMap, period)
    }
}




fun Double.toRainbow(alpha:Int = 255):Color{
    val int = this.toInt()
    val remainder = this - int
    val colA = rainbow[int % 8]
    val colB = rainbow[(int + 1) % 8]
    val red = (colA.red + (colB.red - colA.red)*remainder).toInt()
    val green = (colA.green + (colB.green - colA.green)*remainder).toInt()
    val blue = (colA.blue + (colB.blue - colA.blue)*remainder).toInt()
    return Color(red, green, blue, alpha)
}



val rainbow = arrayOf(Color(255, 145, 145),
        Color(255,190, 145),
        Color(255, 255, 145),
        Color(145, 250, 145),
        Color(145, 240, 240),
        Color(145, 145, 255),
        Color(200, 145, 255),
        Color(235, 145, 230))


 */