package org.myprojects.hexany

import java.awt.Color
import kotlin.math.pow
import kotlin.random.Random

class AntColonyOptimization (val graph: Graph, val c:Double = 1.0, val alpha:Double = 1.53,
                             val beta:Double = 2.54, val evaporation:Double = .68,
                             val Q:Double= 10.0, val antFactor:Double = 0.9, val randomFactor:Double = 0.02,
                             val greedFactor:Double=.2, val maxIterations:Int = 1000,
                             val numberOfNotes:Int = graph.edges.size, val backFactor:Double = .2,
                             val numberOfAnts:Int = (numberOfNotes * antFactor).toInt().coerceAtMost(30) ,
                             var trails:Array<DoubleArray> = Array(numberOfNotes){DoubleArray(numberOfNotes){c}},
                             var ants: Array<Ant> = Array(numberOfAnts){Ant(numberOfNotes)},
                             var index: Int = 0, var worstAnt:Ant = Ant(numberOfNotes), var ibAnt:Ant = Ant(numberOfNotes),
                             var gbAnt:Ant = Ant(numberOfNotes), var rbAnt:Ant = Ant(numberOfNotes),
                             val resbf:Double = 1.5, val resmin:Int = 200, var resIt:Int = 0,
                             val updateFactor:Double =.01, val rChance:Double = .6, val gChance:Double = 1.5) {

    fun solve():IntArray{
        setupAnts()
        trails = Array(numberOfNotes){DoubleArray(numberOfNotes){c}}
        for (iter:Int in 0 until maxIterations) {
            moveAnts()
            updateBandW()
            updateTrails()

        }
        println("Best tour length: " + gbAnt.length.toString())
        println("Best tour order: " + gbAnt.trail.map { graph.scale.notes[it].name })
        return gbAnt.trail
    }
    fun setupAnts(){
        for (ant in ants) {
            ant.clear()
            ant.visitNote(-1, Random.nextInt(numberOfNotes))
            index = 0
        }
    }
    fun moveAnts(start:Int = 0, end:Int = numberOfNotes -1){
        for (i in start until end) {
            for (ant in ants) {
                ant.visitNote(i, selectNextNote(ant))
            }
            index++
        }
    }
    fun selectNextNote(ant: Ant):Int{
        val rand = Random.nextDouble()
        if (rand<randomFactor) {
            val availableNotes = (0 until numberOfNotes).filter { !(ant.visited(it) )}
            return availableNotes[Random.nextInt(availableNotes.size)]
        }
        val probabilities =calculateProbabilities(ant)
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
    fun calculateProbabilities(ant:Ant):DoubleArray{
        val rawProbs = DoubleArray(numberOfNotes)
        val i = ant.trail[index]
        var total = 0.0
        for (j in rawProbs.indices) {
            if (ant.visited(j)) {
                rawProbs[j] = 0.0
            } else {
                val prob = trails[i][j].pow(alpha) * (1.0 / graph.edges[i][j]).pow(beta)
                rawProbs[j] = prob
                total += prob
            }
        }
        return rawProbs.map { it / total }.toDoubleArray()
    }
    fun updateBandW() {
        ants[0].trailLength(graph)
        worstAnt = ants[0]
        ibAnt = ants[0]
        if (rbAnt.length <= 0.0){
            rbAnt = Ant(numberOfNotes, BooleanArray(numberOfNotes){true},
                    ants[0].trail.copyOf(), ants[0].length)
            if (gbAnt.length <= 0.0){
                gbAnt = Ant(numberOfNotes, BooleanArray(numberOfNotes){true},
                        ants[0].trail.copyOf(), ants[0].length)
            }
        }
        for (ant in ants){
            val antTrailLength = ant.trailLength(graph)
            if(antTrailLength<ibAnt.length) {
                ibAnt = ant
                if(antTrailLength<rbAnt.length) {
                    rbAnt = Ant(numberOfNotes, BooleanArray(numberOfNotes){true},
                            ant.trail.copyOf(), ant.length)
                    if(antTrailLength<gbAnt.length) {
                        gbAnt = Ant(numberOfNotes, BooleanArray(numberOfNotes){true},
                                ant.trail.copyOf(), ant.length)
                    }
                }
            } else if (antTrailLength>worstAnt.length) {
                worstAnt = ant
            }
        }
    }
    fun updateTrails(){
        if ((resIt>resmin) and (worstAnt.length < gbAnt.length * resbf)) {
            resetTrails()
            return
        }
        for (i in trails.indices){
            trails[i] = trails[i].map { it * evaporation }.toDoubleArray()
        }
        val rand = resIt * updateFactor + Random.nextDouble()
        val randomBest = when {
            rand > gChance -> gbAnt
            rand > rChance -> rbAnt
            else -> ibAnt
        }
        val updatedAnts = ants.clone().dropWhile { it ==worstAnt }.plus(randomBest)
        for (ant in updatedAnts) {
            val contribution = Q /ant.trailLength(graph)
            for (i in 0 until numberOfNotes){
                trails[ant.trail[i]][ant.trail[(1+i) % numberOfNotes]] += contribution *(1-backFactor)
                trails[ant.trail[(1+i) % numberOfNotes]][ant.trail[i]] += contribution *(backFactor)
            }
        }
        for (i in worstAnt.trail.indices){
            if (worstAnt.trail[(1+i) % numberOfNotes] !=
                    gbAnt.trail[(gbAnt.trail.indexOf(worstAnt.trail[i]) + 1) % numberOfNotes]){
                trails[worstAnt.trail[i]][worstAnt.trail[(1+i) % numberOfNotes]] *= evaporation
            }
        }
        setupAnts()
        resIt ++

    }
    fun resetTrails(){
        rbAnt = Ant(numberOfNotes)
        trails = Array(numberOfNotes){DoubleArray(numberOfNotes){c}}
        setupAnts()
        resIt=0
    }


}

class Ant(val tourSize: Int, var visited:BooleanArray = BooleanArray(tourSize){false},
          var trail:IntArray = IntArray(tourSize), var length:Double = 0.0) {
    fun visitNote(index: Int, note: Int) {
        trail[index + 1] = note
        if(note in visited.indices){visited[note] = true}
    }

    fun visited(note: Int): Boolean {
        return if(note in visited.indices) {visited[note]} else {false}
    }
    fun trailLength(graph: Graph):Double {
        if (trail.last() == -1) {
            length = 100000.0
            return length
        }
        length = graph.edges[trail.last()][trail[0]]
        for (i in 1 until trail.size){
            length += graph.edges[trail[i -1]][trail[i]]
        }
        return length
    }
    fun clear() {
        visited = BooleanArray(visited.size){false}
        length = 0.0
    }

    override fun toString(): String {
        return "\n${visited.toList()}\n${trail.toList()}"
    }

}


fun main(){
    val scale = CPSXany( CPSName(listOf(1, 3, 5, 7, 11),2)).cpsModulation(CPSXany( CPSName(listOf(1, 3, 5, 9),2)))
    /*val graph =scale.scale.toFactorScale().toGraph()
    val colony = AntColonyOptimization(graph)
    colony.resetTrails()
    for (array in graph.edges) {println(array.toList())}
    println(colony.ants[0].visited.toList())
    println(colony.ants[0].trail[0])
    println(colony.calculateProbabilities(colony.ants[0]).toList())
    println(colony.ants[1].visited.toList())
    println(colony.ants[1].trail[0])
    println(colony.calculateProbabilities(colony.ants[1]).toList())*/



    val tour = scale.scale.toFactorScale().shortTours(gradyXYMap, primeFactorScale, 2)
    val h1 = tour[0].toHighlight(Color.blue, 12, 4.0f, outline = true)
    val h2 = tour[1].toHighlight(Color.red, 12, 4.0f, outline = true)
    val h3 = tour[0].toHighlight(Color.green, 8, lineWidth = 4.0f, outline = false, ghost = true)
    val h4 = tour[1].toHighlight(Color.orange, 8, lineWidth = 4.0f, outline = false, ghost = true)








}

