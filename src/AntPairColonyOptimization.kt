package org.myprojects.hexany

import kotlin.math.pow
import kotlin.random.Random

class AntPairColonyOptimization (val graph: Graph, val c:Double = .5, val alpha:Double = 1.53,
                                 val beta:Double = 3.0, val evaporation:Double = .8,
                                 val Q:Double= 80.0, val antFactor:Double = 0.9, val randomFactor:Double = 0.05,
                                 val greedFactor:Double=.3, val maxIterations:Int = 2500,
                                 val numberOfNotes:Int = graph.edges.size, val backFactor:Double = .1,
                                 val numberOfAnts:Int = (numberOfNotes * antFactor).toInt().coerceAtMost(30),
                                 var leftTrails:Array<DoubleArray> = Array(numberOfNotes){DoubleArray(numberOfNotes){c}},
                                 var rightTrails:Array<DoubleArray> = Array(numberOfNotes){DoubleArray(numberOfNotes){c}},
                                 var ants: Array<AntPair> = Array(numberOfAnts){AntPair(numberOfNotes)},
                                 var index: Int = 0, var badAnts:Array<AntPair> = emptyArray(),
                                 var worstPair:AntPair = AntPair(numberOfNotes),
                                 var ibPair:AntPair = AntPair(numberOfNotes), var gbPair:AntPair = AntPair(numberOfNotes),
                                 var rbPair:AntPair = AntPair(numberOfNotes), val resbf:Double = 2.0,
                                 val resmin:Int = 200, var resIt:Int = 0, val updateFactor:Double =.01,
                                 val rChance:Double = .6, val gChance:Double = 1.5,  var resCount:Int = 0) {

    fun solve():Array<IntArray>{
        setupAnts()
        leftTrails = Array(numberOfNotes){DoubleArray(numberOfNotes){c}}
        rightTrails = Array(numberOfNotes){DoubleArray(numberOfNotes){c}}
        for (iter:Int in 0 until maxIterations) {
            moveAnts()
            updateBandW()
            updateTrails()

        }
        println("Restarts: $resCount")
        println("Best tour length: " + gbPair.length.toString())
        println("Best tour order: Left:" + gbPair.left.trail.map { graph.scale.notes[it].name }
                + "\n Right:" +gbPair.right.trail.map { graph.scale.notes[it].name})
        return arrayOf(gbPair.left.trail, gbPair.right.trail)
    }
    fun setupAnts(){
        for (antpair in ants) {
            antpair.clearAnts()
            antpair.left.visitNote(-1, Random.nextInt(numberOfNotes))
            antpair.right.visitNote(-1, Random.nextInt(numberOfNotes))
            index = 0
        }
    }
    fun moveAnts(start:Int = 0, end:Int = numberOfNotes -1){
        for (i in start until end) {
            for (antpair in ants) {
                antpair.leftVisitNote(i, selectNextNote(antpair, true ))
                antpair.rightVisitNote(i, selectNextNote(antpair, false))
            }
            index++
        }
    }
    fun selectNextNote(antpair: AntPair, left:Boolean=true):Int{
        val rand = Random.nextDouble()
        val probabilities =calculateProbabilities(antpair, left)
        if (probabilities.contentEquals(DoubleArray(0))) {return -1}
        val ant = if (left) {antpair.left} else {antpair.right}
        if (rand<randomFactor) {
            val availableNotes = probabilities.indices.filter {it > 0.0}
            return availableNotes[Random.nextInt(availableNotes.size)]
        }
        if (rand<greedFactor) {
            return probabilities.indices.maxByOrNull { probabilities[it] }?:-1
        }
        var total = 0.0
        val r = Random.nextDouble()
        for (i in probabilities.indices) {
            total += probabilities[i]
            if (r <= total) {
                return i
            }
        }
        throw RuntimeException("No Available Notes")
    }
    fun calculateProbabilities(antpair:AntPair, left: Boolean = true):DoubleArray{
        val ant = if (left) {antpair.left} else {antpair.right}
        val i = ant.trail[index]
        if (i == -1) {return DoubleArray(0) }
        var total = 0.0
        val rawProbs = DoubleArray(numberOfNotes)
        val trails = if (left) {leftTrails} else {rightTrails}
        for (j in rawProbs.indices) {
            rawProbs[j] = when {
                ant.visited(j) -> 0.0
                antpair.pathes[i][j] -> 0.0
                antpair.pathes[j][i] -> trails[i][j].pow(alpha) * (1.0 / (graph.edges[i][j] + 5.0)).pow(beta)
                else -> trails[i][j].pow(alpha) * (1.0 / graph.edges[i][j]).pow(beta)
            }
            total += rawProbs[j]
        }
        return if (total == 0.0){
            DoubleArray(0)
        }
         else {rawProbs.map { it / total }.toDoubleArray()}
    }
    fun updateBandW() {
        badAnts = emptyArray()
        ants[0].trailLength(graph)
        worstPair = ants[0]
        ibPair = ants[0]
        if (rbPair.length <= 0.0){
            rbPair = AntPair(numberOfNotes, Ant(numberOfNotes, BooleanArray(numberOfNotes){true},
                    ants[0].left.trail.copyOf(), ants[0].left.length), Ant(numberOfNotes,
                    BooleanArray(numberOfNotes){true}, ants[0].right.trail.copyOf(), ants[0].right.length))
            if (gbPair.length <= 0.0){
                gbPair = AntPair(numberOfNotes, Ant(numberOfNotes, BooleanArray(numberOfNotes){true},
                        ants[0].left.trail.copyOf(), ants[0].left.length), Ant(numberOfNotes,
                        BooleanArray(numberOfNotes){true}, ants[0].right.trail.copyOf(), ants[0].right.length))
            }
        }
        for (antpair in ants){
            val antTrailLength = antpair.trailLength(graph)
            if(antTrailLength<ibPair.length) {
                ibPair = antpair
                if(antTrailLength<rbPair.length) {
                    rbPair = AntPair(numberOfNotes, Ant(numberOfNotes, BooleanArray(numberOfNotes){true},
                            antpair.left.trail.copyOf(), antpair.left.length), Ant(numberOfNotes,
                            BooleanArray(numberOfNotes){true}, antpair.right.trail.copyOf(), antpair.right.length))
                    if(antTrailLength<gbPair.length) {
                        gbPair = AntPair(numberOfNotes, Ant(numberOfNotes, BooleanArray(numberOfNotes){true},
                                antpair.left.trail.copyOf(), antpair.left.length), Ant(numberOfNotes,
                                BooleanArray(numberOfNotes){true}, antpair.right.trail.copyOf(), antpair.right.length))
                    }
                }
            } else if (antTrailLength > worstPair.length){
                worstPair = antpair
            }
            if (antTrailLength>100000.0) {
                badAnts=badAnts.plus(antpair)}

        }
    }
    fun updateTrails(){
        if ((resIt>resmin) and (worstPair.length < gbPair.length * resbf)) {
            resetTrails()
            return
        }
        for (i in leftTrails.indices){
            leftTrails[i] = leftTrails[i].map { it * evaporation }.toDoubleArray()
        }
        for (i in rightTrails.indices){
            rightTrails[i] = rightTrails[i].map { it * evaporation }.toDoubleArray()
        }
        val rand = resIt * updateFactor + Random.nextDouble()
        val randomBest = when {
            rand > gChance -> gbPair
            rand > rChance -> rbPair
            else -> ibPair
        }
        val updatedAnts = ants.filter { it.length <100000.0}.plus(randomBest)
        for (antpair in updatedAnts) {
            val contribution = if (antpair == randomBest){
                Q * 5.0/antpair.trailLength(graph)
            } else  {Q /antpair.trailLength(graph)}
            for (i in 0 until numberOfNotes){
                leftTrails[antpair.left.trail[i]][antpair.left.trail[(1+i) % numberOfNotes]] += contribution *(1-backFactor)
                leftTrails[antpair.left.trail[(1+i) % numberOfNotes]][antpair.left.trail[i]] += contribution *(backFactor)
                rightTrails[antpair.left.trail[(1+i) % numberOfNotes]][antpair.left.trail[i]] += contribution *(backFactor)
                rightTrails[antpair.left.trail[i]][antpair.left.trail[(1+i) % numberOfNotes]] *= evaporation
                rightTrails[antpair.right.trail[i]][antpair.right.trail[(1+i) % numberOfNotes]] += contribution *(1-backFactor)
                rightTrails[antpair.right.trail[(1+i) % numberOfNotes]][antpair.right.trail[i]] += contribution *(backFactor)
                leftTrails[antpair.right.trail[(1+i) % numberOfNotes]][antpair.right.trail[i]] += contribution *(backFactor)
                leftTrails[antpair.right.trail[i]][antpair.right.trail[(1+i) % numberOfNotes]] *= evaporation
            }
        }
        if (badAnts.isEmpty()) {badAnts = badAnts.plus(worstPair)}
        for (pair in badAnts) {
            for (i in 1 until numberOfNotes){
                if (pair.left.trail[i] in leftTrails.indices) {
                    if (pair.left.trail[i] != gbPair.left.trail[(gbPair.left.trail
                                    .indexOf(pair.left.trail[i-1])+1) % numberOfNotes]){
                        if (pair.left.trail[i] != gbPair.right.trail[(gbPair.right.trail
                                        .indexOf(pair.left.trail[i-1])+1) % numberOfNotes]) {
                            leftTrails[pair.left.trail[i-1]][pair.left.trail[i]] *= evaporation
                            rightTrails[pair.left.trail[i-1]][pair.left.trail[i]] *= evaporation
                        }
                        leftTrails[pair.left.trail[i-1]][pair.left.trail[i]] *= evaporation
                    }
                }
                if (pair.right.trail[i] in leftTrails.indices) {
                    if (pair.right.trail[i] != gbPair.right.trail[(gbPair.right.trail
                                    .indexOf(pair.right.trail[i-1])+1) % numberOfNotes]){
                        if (pair.right.trail[i] != gbPair.left.trail[(gbPair.left.trail
                                        .indexOf(pair.right.trail[i-1])+1) % numberOfNotes]) {
                            leftTrails[pair.right.trail[i-1]][pair.right.trail[i]] *= evaporation
                            rightTrails[pair.right.trail[i-1]][pair.right.trail[i]] *= evaporation
                        }
                        rightTrails[pair.right.trail[i-1]][pair.right.trail[i]] *= evaporation
                    }
                }

            }
            if (pair.left.trail.last() in leftTrails.indices) {
                if (pair.left.trail[0] != gbPair.left.trail[(gbPair.left.trail
                                .indexOf(pair.left.trail.last())+1) % numberOfNotes]){
                    if (pair.left.trail[0] != gbPair.right.trail[(gbPair.right.trail
                                    .indexOf(pair.left.trail.last())+1) % numberOfNotes]) {
                        leftTrails[pair.left.trail.last()][pair.left.trail[0]] *= evaporation
                        rightTrails[pair.left.trail.last()][pair.left.trail[0]] *= evaporation
                    }
                    leftTrails[pair.left.trail.last()][pair.left.trail[0]] *= evaporation
                }
            }
            if (pair.right.trail.last() in leftTrails.indices) {
                if (pair.right.trail[0] != gbPair.right.trail[(gbPair.right.trail
                                .indexOf(pair.right.trail.last())+1) % numberOfNotes]){
                    if (pair.right.trail[0] != gbPair.left.trail[(gbPair.left.trail
                                    .indexOf(pair.right.trail.last())+1) % numberOfNotes]) {
                        leftTrails[pair.right.trail.last()][pair.right.trail[0]] *= evaporation
                        rightTrails[pair.right.trail.last()][pair.right.trail[0]] *= evaporation
                    }
                    rightTrails[pair.right.trail.last()][pair.right.trail[0]] *= evaporation
                }
            }

        }
        setupAnts()
        resIt ++

    }
    fun resetTrails(){
        rbPair = AntPair(numberOfNotes)
        leftTrails = Array(numberOfNotes){DoubleArray(numberOfNotes){c}}
        rightTrails = Array(numberOfNotes){DoubleArray(numberOfNotes){c}}
        setupAnts()
        resCount ++
        resIt=0
    }


}
class AntPair(val toursize:Int, val left:Ant = Ant(toursize), val right:Ant = Ant(toursize),
              var length:Double = left.length + right.length,
              var pathes: Array<BooleanArray> = Array(toursize){
                  BooleanArray(toursize){false}}) {
    fun leftVisitNote(index: Int, note: Int) {
        left.visitNote(index, note)
        if(note in 0 until toursize){pathes[left.trail[index]][note]=true}
    }
    fun rightVisitNote(index: Int, note: Int) {
        right.visitNote(index, note)
        if(note in 0 until toursize){pathes[right.trail[index]][note]=true}
    }
    fun trailLength(graph: Graph):Double {
        length = left.trailLength(graph) + right.trailLength(graph.addLines(arrayOf(left.trail), 1.0))
        return length
    }
    fun clearAnts(){
        left.clear()
        right.clear()
        pathes = Array(left.tourSize){ BooleanArray(left.tourSize){false}}
    }
    override fun toString(): String {
        return "$left, $right"
    }
}



fun main(){
    val scale = CPSXany( CPSName(listOf(1, 3, 5, 7, 11),2)).cpsModulation(CPSXany( CPSName(listOf(1, 3, 5, 9),2)))
    val graph =scale.scale.toFactorScale().toGraph()
    val colony = AntPairColonyOptimization(graph)
    colony.solve()




    /*val tour = scale.scale.toFactorScale().shortTours(gradyXYMap, primeFactorScale, 2)
    val h1 = tour[0].toHighlight(Color.blue, 12, 4.0f, outline = true)
    val h2 = tour[1].toHighlight(Color.red, 12, 4.0f, outline = true)
    val h3 = tour[0].toHighlight(Color.green, 8, lineWidth = 4.0f, outline = false, ghost = true)
    val h4 = tour[1].toHighlight(Color.orange, 8, lineWidth = 4.0f, outline = false, ghost = true)
    val diag = scale.toXYStructure(gradyXYMap).toDiagram(Color.yellow, 10, 2.5f, highlights = listOf(h1, h2, h3, h4)).toProcessedDiagram()

    val frame = DiagramScreen("Short Tour", diag)
    frame.isVisible = true*/







}

