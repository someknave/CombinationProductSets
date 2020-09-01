package org.myprojects.hexany


import java.awt.Color
import java.awt.Color.BLACK
import java.lang.Math.pow
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

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
    fun toXYStructure(xyMap: XYMap, intervals: FactorScale = primeFactorScale):XYStructure {
        val struct = scale.toFactorScale().makeStructure(intervals)
        return struct.toXYStructure(xyMap, listOf(cpsName))
    }
}


open class Name(val generators: List<Int>, val deg: Int = 1, val order:Int = generators.size, val period: Period = octave){
    override fun toString(): String {
        return if (period == octave) {"$generators($deg|$order)"}
        else {"$generators($deg|$order) Period:(${period.num}/${period.div})"}
    }
    fun nameToKey():Int {
        var key = deg.shl(24)
        for (i in generators) {
            val pow = period.factors.indexOf(i)
            if(pow == -1) {throw IllegalArgumentException("Invalid name passed to nameToKey")}
            key+= 1.shl(pow)
        }
        return key
    }
    fun cps(inverse: Boolean = false, degree: Int = deg): Scale{
        val notes = cpsInner(generators, degree, 0, inverse= inverse, period=period)
        return Scale(notes.sortedBy { it.toFloat() })
    }
}
class CPSName(generators: List<Int>, deg: Int = 2, order: Int = generators.size, period: Period = octave,
              val greek: String = greekName(order.nCk(deg))?:"WTFany") :
        Name(generators, deg, generators.size, period){
    override fun toString(): String{
        return "$generators($deg|$order):$greek"
    }

}

class Scale(val notes: List<Fraction>, val period: Period = octave) {
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
                .mapIndexed{index, frac ->  frac.invertFraction(period) * notes[(n + index) % size]}
                .sortedBy { it.toFloat()}.distinct()
    }
    fun intervalsBySteps(notes: List<Fraction>): List<List<Fraction>>{
        val size = notes.size
        val range = (1..(size/2))
        return range.map { this.nDegreeIntervals(it)}
    }
    fun inversion(): Scale{ return Scale(notes.map { it.invertFraction(period) })}
    fun selfProduct(n:Int):Scale {
        if (n <0) {return Scale(emptyList())}
        var scaleAcc = Scale(listOf(1.toFraction(1, period)))
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
        return Scale(product.sortedBy { it.toFloat() }.distinct(), period)
    }
    fun addition(other: Scale): Scale{
        return Scale(notes.union(other.notes).toList().sortedBy { it.toFloat()}, period)
    }
    fun modulate(interval: Fraction = 1.toFraction(1, period)):Scale {
        return Scale(notes.map{it * interval}.sortedBy { it.toFloat() }, period)
    }
    fun toFactorScale(limit: Int = 11): FactorScale{
        return FactorScale(notes.map { it.factor(limit) })
    }

}



class Fraction(var num:Int = 1, var div:Int = 1, val period: Period = octave) {
    override fun toString(): String {
        return "$num/$div"
    }
    override fun hashCode(): Int {
        return (num*div).toFloat().pow(2).toInt() / div.primeDivisors()
    }
    override fun equals(other: Any?): Boolean {
        if (other !is Fraction) {return false}
        return (num == other.num) and (div == other.div) and (period == other.period)
    }
    fun toFloat():Float{
        return num.toFloat()/div
    }
    operator fun times(other: Any?): Fraction {
        if (other is Int) {
            return (other * num).toFraction(div, period)
        }
        if (other is Fraction) {
            return (num * other.num).toFraction( div * other.div, period)
        }
        return Fraction(0, 0, period)
    }
    operator fun div(other: Any?): Fraction{
        if (other is Int) {
            return num.toFraction(other*div, period)
        }
        if (other is Fraction) {
            return (num *other.div).toFraction(div*other.num, period)
        }
        return Fraction(0, 0)
    }
    fun invertFraction(p:Period = period): Fraction{
        return div.toFraction(num, p)
    }
    operator fun compareTo(other: Any?): Int {
        if (other is Fraction) {
            return  this.toFloat().compareTo(other.toFloat())
        }
        if (other is Int) {return compareTo(other.toFraction(period = period))}
        if (other is Float) {return this.toFloat().compareTo(other)}
        throw IllegalArgumentException("Float, Int or Fraction required")
    }
    fun factor(limit:Int = 11): FactorNote{
        val factors = MutableList<Int>(limit) {0}
        if ((num == 0)or (div == 0))  {
            return FactorNote(this, factors.toList())}
        val fraction = mutableListOf(num, div)
        for (i in 0 until limit){
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
    fun toXYStructure(xyMap: XYMap, intervals: FactorScale = primeFactorScale):XYStructure {
        val struct = scale.toFactorScale().makeStructure(intervals)
        return struct.toXYStructure(xyMap, listOf(name.mediant.cpsName, name.flank.cpsName))
    }
}


class Mandala(val core: CPSXany, val stellations: Scale, val fullStellation: Boolean=false) {
    override fun toString(): String {
        return "${core.cpsName}: ${stellations.notes}"
    }
    fun toXYStructure(xyMap: XYMap, intervals: FactorScale = primeFactorScale):XYStructure {
        val struct = stellations.toFactorScale().makeStructure(intervals)
        return struct.toXYStructure(xyMap, listOf(core.cpsName))
    }
}

class Diamond(val name: Name, val key: Int = name.nameToKey(),
              val scale: Scale = name.cps(inverse= false)
                      .listProduct(name.cps(inverse=  true))){
    fun toXYStructure(xyMap: XYMap, intervals: FactorScale = primeFactorScale):XYStructure {
        val struct = scale.toFactorScale().makeStructure(intervals)
        return struct.toXYStructure(xyMap, listOf(name))
    }

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
    override fun hashCode(): Int {
        return name.hashCode()
    }
    override fun equals(other: Any?): Boolean {
        if(other !is FactorNote) {return false}
        return (name == other.name)
    }
    fun toXYCoord(xyMap: XYMap = wilsonXYMap):XYCoordinates {
        val x = factors.zip(primes){ fac:Int, prime:Int -> (xyMap.mapX[prime]?:0) * fac}
        val y = factors.zip(primes){ fac:Int, prime:Int -> (xyMap.mapY[prime]?:0) * fac}
        return XYCoordinates(x.sum(), y.sum())
    }
}

class FactorScale(val notes:List<FactorNote>) {
    override fun toString(): String {
        return "$notes"
    }

    fun intMap(interval: FactorNote, period: Period = octave): IntervalMap {
        val facMap = mutableMapOf<FactorNote, FactorNote>()
        for (note in notes) {
            val note2 = note.add(interval)
            val note3 = note2.difference(period.num.toFraction(period.div).factor())
            if (note2 in notes) {
                facMap.put(note, note2)
            }
            if (note3 in notes) {
                facMap.put(note, note3)
            }
        }
        return IntervalMap(interval, facMap.toMap())
    }
    operator fun plus(scale: FactorScale): FactorScale {
        return FactorScale(notes.union(scale.notes).toList())
    }
    fun toGraph(weights: DoubleArray = edgeWeights,
                lines: Array<IntArray> = emptyArray(),
                z: Double = 0.8, mod:Double = 0.8): Graph {
        val size = this.notes.size
        val array = Array(size){DoubleArray(size)}
        val edges = Array(size){IntArray(0)}
        for (tour in lines) {
            for (i in tour.indices) {
                edges[i] = edges[i].plus((i+1) % tour.size)
            }
        }
        val addMod = -3*mod/2 +sqrt(pow(mod /2, 2.0) + 1)
        for (i in 0 until size) {
            for (j in i until size) {
                val travelled = when {
                    (j in edges[i]) and (i in edges[j]) -> listOf(14.0, 14.0)
                    j in edges[i] -> listOf(8.0, 3.0)
                    i in edges[j] -> listOf(3.0, 8.0)
                    else -> listOf(0.0, 0.0)
                }
                val interval = this.notes[i].difference(this.notes[j])
                val modifier = interval.name.toFloat().toDouble() *mod + addMod
                val facWeights = interval.factors.zip(weights.toList())
                val maxfac = facWeights.map {
                    if (it.first == 0) {
                        0.0
                    } else {
                        it.second
                    }
                }.maxOrNull() ?: 0.0
                val weight = ((facWeights.map { it.second.times(it.first.coerceAtLeast(-it.first)) }
                        .sum() - maxfac) * z + 1) * maxfac
                array[i][j] = weight * modifier + travelled[0]
                array[j][i] = weight / modifier + travelled[1]
            }
        }
        return Graph(this, array)
    }

    fun makeStructure(intervals: FactorScale = primeFactorScale): ScaleStructure {
        val maps = mutableListOf<IntervalMap>()
        for (int in intervals.notes.filter { it != Fraction(1, 1).factor() }) {
            val intMap = this.intMap(int)
            if (intMap.map.isNotEmpty()) {
                maps.add(intMap)
            }
        }
        return ScaleStructure(this, maps.toList())
    }

    fun shortTours(map: XYMap, intervals: FactorScale = primeFactorScale,
                   tours: Int = 1, lines: Array<IntArray> = emptyArray()): List<XYStructure> {
        if (tours <= 0) {
            return emptyList()
        }
        val graph =this.toGraph(edgeWeights, lines)
        val colony = AntColonyOptimization(graph)
        for (array in graph.edges) {println(array.toList())}
        val tour = colony.solve()
        val starts = tour.map { this.notes[it] }
        val ends = starts.drop(1).plus(starts[0])
        val points = this.notes.map { it.toXYCoord(map) }
        val edges = starts.zip(ends).map { XYLine(it.first.toXYCoord(map), it.second.toXYCoord(map)) }
        val stucture = XYStructure(edges, points, this.makeStructure(intervals), map)
        return if (tours == 1) {
            listOf(stucture)
        } else {shortTours(map, intervals, tours -1, lines.plus(tour))
                .plus(stucture)}
    }
}

class IntervalMap(val interval: FactorNote, val map: Map<FactorNote, FactorNote>) {
    override fun toString(): String {
        return "$interval:$map\n"
    }
}

class ScaleStructure(val scale: FactorScale, val maps:List<IntervalMap>) {
    override fun toString(): String {
        return "$scale\n$maps\n"
    }
    fun toXYStructure(xyMap:XYMap = wilsonXYMap, name: List<Name> = emptyList()):XYStructure {
        val points = scale.notes.map { it.toXYCoord(xyMap) }
        val lines = maps.map { it.map.map{XYLine(it.key.toXYCoord(xyMap), it.value.toXYCoord(xyMap))} }
        return XYStructure(lines.flatten(), points, this, xyMap, name)
    }
}

class Period(val num: Int, val div: Int, val factors: List<Int>) {
    fun toFloat():Float{
        return num.toFloat()/div
    }

    override fun toString(): String {
        return "($num/$div): $factors"
    }

}
class XYMap(val mapX: Map<Int, Int>, val mapY: Map<Int, Int>,val period: Period = octave) {
    override fun toString(): String {
        return "Period:$period, xMap:$mapX, yMap$mapY"
    }

}
class XYCoordinates(val x: Int, val y:Int){
    override fun toString(): String {
        return "($x,$y)"
    }
    override fun equals(other: Any?): Boolean {
        return if (other is XYCoordinates) {
            (x == other.x) and (y == other.y)
        } else {false}
    }

    override fun hashCode(): Int {
        return (x.shl(8) + (y % 256))
    }
    fun getProperties(diag:RawDiagram, outline: Boolean):HProperties {
        val size = diag.highlights.size
        val ghost = this !in diag.structure.points
        val priorityMod = if (outline) {0} else {1024}
        val widthMod = if (outline) {0} else {-4}
        for (i in 0 until size) {
            val highlight = diag.highlights[i]
            if ((highlight.outline == outline)
                    and (ghost or !highlight.ghost)
                    and (this in highlight.structure.points)
                    and (highlight.pointWidth > 0)) {
                return HProperties(highlight.colour, highlight.pointWidth + widthMod,
                        highlight.lineWidth, size - i +priorityMod)
            }
        }
        return HProperties(diag.colour, diag.pointWidth + widthMod, diag.lineWidth, priorityMod)
    }
    fun toDiagramPoint(rShift:Int = 30, dShift:Int = 40,
                       width: Int=10, colour: Color=BLACK, priority: Int=0):DiagramPoint{
        val newx = (rShift + x)
        val newy = (dShift - y)
        return DiagramPoint(newx, newy, width, colour, priority)
    }
}
class XYLine(val start:XYCoordinates, val end:XYCoordinates){
    override fun toString(): String {
        return "$start-$end"
    }

    override fun hashCode(): Int {
        return start.hashCode().shl(16) +
                (end.hashCode() % 1.shl(16))
    }
    override fun equals(other: Any?): Boolean {
        return if (other is XYLine) {
            (start == other.start) and (end == other.end)
        } else {false}
    }
    fun getProperties(diag:RawDiagram, outline: Boolean):HProperties {
        val size = diag.highlights.size
        val priorityMod = if (outline) {0} else {64}
        val widthMod = if (outline) {0} else {-2}
        val ghost = (this !in diag.structure.lines)
        for (i in 0 until size) {
            val highlight = diag.highlights[i]
            if ((highlight.outline == outline)
                    and (ghost or !highlight.ghost)
                    and (this in highlight.structure.lines)
                    and (highlight.lineWidth > 0)) {
                return HProperties( highlight.colour, highlight.pointWidth,
                        highlight.lineWidth + widthMod, priorityMod +size - i)
            }
        }
        return HProperties( diag.colour, diag.pointWidth, diag.lineWidth + widthMod, priorityMod)
    }
    fun toDiagramLine(rShift:Int = 30, dShift:Int = 40,
                      width: Float=3.5f, colour: Color=BLACK, priority: Int = 0):DiagramLine{
        val x1 = (rShift + start.x)
        val x2 = (rShift + end.x)
        val y1 = (dShift - start.y)
        val y2 = (dShift - end.y)
        return DiagramLine(x1, y1, x2, y2, width, colour, priority)
    }
}
class XYStructure(val lines: List<XYLine>, val points: List<XYCoordinates>,
                  val structure: ScaleStructure, val xyMap: XYMap = wilsonXYMap,
                  val name:List<Name> = emptyList()) {
    override fun toString(): String {
        return "$name: $points\n$lines\n"
    }
    fun toHighlight(colour: Color, pointWidth: Int = 14, lineWidth: Float= 4.1f, outline: Boolean = false, ghost: Boolean = false):Highlight{
        return Highlight(this, colour ,pointWidth, lineWidth, outline, ghost)
    }
    fun toDiagram(colour: Color = BLACK, pointWidth: Int = 12, lineWidth: Float = 3.5f, highlights: List<Highlight> = emptyList()):RawDiagram {
        return RawDiagram(this, colour, pointWidth, lineWidth, highlights)
    }
}


class Highlight (val structure: XYStructure, val colour: Color,
                 val pointWidth:Int, val lineWidth:Float,
                 val outline: Boolean = false, val ghost: Boolean = false){
    override fun toString(): String {
        return "${structure.name}; colour:$colour, pwidth:$pointWidth" +
                ", lwidth:$lineWidth, outline:$outline, " +
                "ghost:$ghost\n${structure.points}\n${structure.lines}\n"
    }
}

class RawDiagram (val structure: XYStructure, val colour: Color = BLACK,
                  val pointWidth:Int, val lineWidth:Float,
                  val highlights: List<Highlight> = emptyList()) {
    fun toProcessedDiagram(lMargin: Int = 100, uMargin: Int = lMargin,
                           rMargin: Int = lMargin, dMargin:Int = uMargin,
                           xLen:Int = 400, yLen:Int = 300,
                           byLength:Boolean = false):ProcessedDiagram {
        val printable = listOf(structure)
                .union(highlights.filter { it.ghost }
                        .map { it.structure }).toList()
        val points = printable.flatMap { it.points }.distinct()
        if (points.isEmpty()) {return ProcessedDiagram(
                xLen, yLen, emptyList(), emptyList())}
        val lines = printable.flatMap { it.lines }.distinct()
        val xvals = points.map { it.x }.sorted()
        val xmin = xvals[0]
        val xmax = xvals.last()
        val yvals = points.map { it.y }.sorted()
        val ymin = yvals[0]
        val ymax = yvals.last()
        val xSpan = xmax - xmin
        val ySpan = ymax - ymin
        class CanvasSize(val width: Int, val height:Int, val lGap: Int, val uGap:Int){}
        val canv:CanvasSize = if (byLength) {
            val width = xLen.coerceAtLeast(xSpan +80)
            val height = yLen.coerceAtLeast( ySpan +80)
            val lGap = lMargin.coerceAtMost(width - 40 - xSpan)
            val uGap = uMargin.coerceAtMost(height - 40 - ySpan)
            CanvasSize(width, height, lGap, uGap)
        } else  {
            val width = lMargin + rMargin + xSpan
            val height = uMargin + dMargin + ySpan
            CanvasSize(width, height, lMargin, uMargin)
        }
        val rShift = canv.lGap - xmin
        val dShift = canv.uGap + ySpan +ymin
        val dpoints = mutableListOf<DiagramPoint>()
        val dlines = mutableListOf<DiagramLine>()
        for (point in points){
            if (point in structure.points) {
                val outProperties = point.getProperties(this, true)
                val inProperties = point.getProperties(this, false)
                dpoints.add(point.toDiagramPoint(rShift,
                        dShift, inProperties.pointWidth,
                        inProperties.colour, inProperties.priority))
                dpoints.add(point.toDiagramPoint(rShift,
                        dShift, outProperties.pointWidth,
                        outProperties.colour, outProperties.priority))
            } else {
                val outProperties = point.getProperties(this, true)
                val inProperties = point.getProperties(this, false)
                dpoints.add(point.toDiagramPoint(rShift, dShift,
                        inProperties.pointWidth, Color(inProperties.colour
                        .rgb + 120.shl(24), true), inProperties.priority))
                dpoints.add(point.toDiagramPoint(rShift, dShift,
                        outProperties.pointWidth, Color(outProperties.colour
                        .rgb + 80.shl(24), true), outProperties.priority))
            }
        }
        for (line in lines){
            val outProperties = line.getProperties(this, true)
            val inProperties = line.getProperties(this, false)
            if (line in structure.lines) {
                dlines.add(line.toDiagramLine(rShift, dShift, outProperties
                        .lineWidth, outProperties.colour, outProperties.priority))
                if (inProperties.lineWidth>0) {
                    dlines.add(line.toDiagramLine(rShift, dShift, inProperties.lineWidth,
                            inProperties.colour, inProperties.priority))
                }
            } else {
                if (outProperties.lineWidth>0) {
                    dlines.add(line.toDiagramLine(rShift, dShift, inProperties
                            .lineWidth, Color(inProperties.colour.rgb +
                            120.shl(24), true), inProperties.priority))
                }
                dlines.add(line.toDiagramLine(rShift, dShift, outProperties
                        .lineWidth, Color(outProperties.colour.rgb +
                        80.shl(24), true), outProperties.priority))
            }
        }
        return ProcessedDiagram(canv.width, canv.height,
                dlines.sortedBy { it.priority },
                dpoints.sortedBy { it.priority })
    }
}

class DiagramLine (val x1:Int, val y1:Int, val x2:Int,
                   val y2:Int, val width:Float = 2f,
                   val colour:Color, val priority:Int){
    override fun toString(): String {
        return "{($x1, $y1) - ($x2, $y2), w:$width, c:$colour}"
    }

}
class DiagramPoint (val x:Int, val y:Int, val width:Int = 6,
                    val colour:Color, val priority: Int){
    override fun toString(): String {
        return "{($x, $y), w:$width, c:$colour}"
    }

}
class ProcessedDiagram (val x:Int, val y:Int, val lines: List<DiagramLine>,
                        val points: List<DiagramPoint>){
    override fun toString(): String {
        return "($x, $y) $points\n$lines\n"
    }
    
}
class HProperties (val colour:Color, val pointWidth:Int, val lineWidth:Float, val priority: Int){}

class Graph (val scale: FactorScale, val edges: Array<DoubleArray> =
        Array(scale.notes.size) { DoubleArray(scale.notes.size) }){
    override fun toString(): String {
        val printable = edges.map { Arrays.toString(it)+ "\n"}.toList()
        return "$scale\n$printable"
    }
    fun addLines(lines: Array<IntArray>, factor:Double = 1.0): Graph {
        if (lines.isEmpty()) {return this}
        val size = lines[0].size
        val modEdges = Array(size){IntArray(0)}
        for (tour in lines) {
            for (i in tour.indices) {
                modEdges[i] = modEdges[i].plus((i+1) % tour.size)
            }
        }
        val copyEdges = edges.map {it.copyOf()}.toTypedArray()
        for (i in 0 until size -1) {
            for (j in i+1 until size) {
                val travelled = when {
                    (j in modEdges[i]) and (i in modEdges[j]) -> listOf(14.0, 14.0)
                    j in modEdges[i] -> listOf(8.0, 3.0)
                    i in modEdges[j] -> listOf(3.0, 8.0)
                    else -> listOf(0.0, 0.0)
                }
                copyEdges[i][j] += factor * travelled[0]
                copyEdges[j][i] += factor * travelled[1]
            }
        }
        return Graph(scale, copyEdges)

    }
}




