package org.myprojects.hexany

import java.awt.Color
import kotlin.math.pow
import kotlin.random.Random

class AntColonyOptimization (val graph: Graph, val c:Double = 1.0, val alpha:Double = 1.0,
                             val beta:Double = 1.8, val evaporation:Double = .5,
                             val Q:Double= 500.0, val antFactor:Double = 0.9, val randomFactor:Double = 0.02,
                             val maxIterations:Int = 500, val numberOfNotes:Int = graph.edges.size,
                             val numberOfAnts:Int = (numberOfNotes * antFactor).toInt(),
                             var trails:Array<DoubleArray> = Array(numberOfNotes){DoubleArray(numberOfNotes){c}},
                             var ants: Array<Ant> = Array(numberOfAnts){Ant(numberOfNotes)},
                             var index: Int = 0, var bestTourOrder: IntArray = IntArray(0),
                             var bestTourLength:Int = 0 ) {

    fun solve():IntArray{
        setupAnts()
        trails = Array(numberOfNotes){DoubleArray(numberOfNotes){c}}
        for (iter:Int in 0 until maxIterations) {
            moveAnts()
            updateTrails()
            updateBest()
        }
        println("Best tour length: " + (bestTourLength - numberOfNotes).toString())
        println("Best tour order: " + bestTourOrder.map { graph.scale.notes[it].name })
        return bestTourOrder
    }
    fun setupAnts(){
        for (ant in ants) {
            ant.clear()
            ant.visitNote(-1, Random.nextInt(numberOfNotes))
            index = 0
        }
    }
    fun moveAnts(){
        for (i in 0 until numberOfNotes -1) {
            for (ant in ants) {
                ant.visitNote(i, selectNextNote(ant))
            }
            index++
        }
    }
    fun selectNextNote(ant: Ant):Int{
        if (Random.nextDouble()<randomFactor) {
            val availableNotes = (0 until numberOfNotes).filter { !(ant.visited(it) )}
            return availableNotes[Random.nextInt(availableNotes.size)]
        }
        val probabilities =calculateProbabilities(ant)
        var total = 0.0
        val r = Random.nextDouble()
        for (i in 0 until numberOfNotes) {
            total += probabilities[i]
            if (r <= total) {
                return i
            }
        }
        throw RuntimeException("No Available Notes")
    }
    fun calculateProbabilities(ant:Ant):DoubleArray{
        val probabilities = DoubleArray(numberOfNotes)
        val i = ant.trail[index]
        var total = 0.0
        for (j in probabilities.indices) {
            if (ant.visited(j)) {
                probabilities[j] = 0.0
            } else {
                val prob = trails[i][j].pow(alpha) * (1.0 / graph.edges[i][j]).pow(beta)
                probabilities[j] = prob
                total += prob
            }
        }
        return probabilities.map { it / total }.toDoubleArray()
    }
    fun updateTrails(){
        for (i in trails.indices){
            trails[i] = trails[i].map { it * evaporation }.toDoubleArray()
        }
        for (ant in ants) {
            val contribution = Q /ant.trailLength(graph)
            for (i in 1 until numberOfNotes){
                trails[ant.trail[i-1]][ant.trail[i]] += contribution
            }
            trails[ant.trail.last()][ant.trail[0]] += contribution
        }
    }
    fun updateBest() {
        if (bestTourLength <= 0){
            bestTourLength = ants[0].trailLength(graph)
            bestTourOrder = ants[0].trail
        }
        for (ant in ants){
            val antTrailLength = ant.trailLength(graph)
            if(antTrailLength<bestTourLength) {
                bestTourLength = antTrailLength
                bestTourOrder = ant.trail
            }
            ant.clear()
            ant.visitNote(-1, Random.nextInt(numberOfNotes))
        }
        index = 0
    }

}
class Ant(val tourSize: Int, var visited:BooleanArray = BooleanArray(tourSize){false},
          var trail:IntArray = IntArray(tourSize)) {
    fun visitNote(index: Int, note: Int) {
        trail[index + 1] = note
        visited[note] = true
    }

    fun visited(note: Int): Boolean {
        return visited[note]
    }
    fun trailLength(graph: Graph):Int {
        var length = graph.edges[trail.last()][trail[0]]
        for (i in 1 until trail.size){
            length += graph.edges[trail[i -1]][trail[i]]
        }
        return length
    }
    fun clear() {
        visited = BooleanArray(visited.size){false}
        trail = IntArray(tourSize){-1}
    }

    override fun toString(): String {
        return "\n${visited.toList()}\n${trail.toList()}"
    }

}


fun main(){
    val scale = CPSXany( CPSName(listOf(1, 3, 5, 7, 11),2)).cpsModulation(CPSXany( CPSName(listOf(1, 5, 7, 9),2)))
    val graph = scale.scale.toFactorScale().toGraph()
    val tour = scale.scale.toFactorScale().shortTour(gradyXYMap)
    val h1 = tour.toHighlight(Color.blue, 12, 4.0f, outline = true)
    val h2 = tour.toHighlight(Color.green, 8, lineWidth = 4.0f, outline = false, ghost = true)
    val diag = scale.toXYStructure(gradyXYMap).toDiagram(Color.red, 10, 2.5f, highlights = listOf(h1, h2)).toProcessedDiagram()
    val frame = DiagramScreen("Short Tour", diag)
    frame.isVisible = true







}