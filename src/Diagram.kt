package org.myprojects.hexany

import java.awt.*
import javax.swing.JFrame
import javax.swing.JPanel

class DiagramScreen(title: String, diagram: ProcessedDiagram) : JFrame() {

    init {
        createUI(title, diagram)
    }

    private fun createUI(title: String, diagram: ProcessedDiagram) {

        setTitle(title)
        val xSize = diagram.x
        val ySize = diagram.y

        val image = Diagram(xSize, ySize, diagram)
        add(image)

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(xSize+17, ySize+40)
        setLocationRelativeTo(null)
    }



}

class Diagram(val xSize:Int, val ySize:Int, val diagram: ProcessedDiagram):JPanel(){
    private fun doDrawing(g:Graphics){
        val g2d:Graphics2D = g as Graphics2D
        for(line in diagram.lines) {
            g2d.stroke = BasicStroke(line.width)
            g2d.color = line.colour
            g2d.drawLine(line.x1, line.y1, line.x2, line.y2)
        }
        for(point in diagram.points) {
            val w = point.width
            g2d.color = point.colour
            g2d.fillOval(point.x - w/2, point.y - w/2, w, w)
        }

    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        doDrawing(g)
    }
}




private fun createAndShowGUI() {
    val map:XYMap = separatedMap
    val fac:FactorScale = noOneFactors
    val dek2 = CPSXany(CPSName(listOf(3, 7, 9, 11, 13),2))
    val dek3 = CPSXany(CPSName(listOf(1, 5, 9, 11, 13),2))
    val mod = dek2.cpsModulation(dek3).toXYStructure(map, fac + inc9FactorScale )
    val mmod = multiModulateCPS(CPSPair(dek3, dek2)).toXYStructure(map, fac)
    val h1 = dek3.toXYStructure(map, inc9FactorScale).toHighlight(Color.RED, 10, 3.75f, false)
    val h2 = dek2.toXYStructure(map, fac).toHighlight(Color.BLUE, 10, 3.5f, true)
    val h3 = dek2.scale.modulate(1.toFraction(3)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color(0, 180, 255), 10, 3.75f,true)
    val h4 = dek2.scale.modulate(5.toFraction(7)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color(0, 180, 255), 10, 3.75f,true)
    val h5 = dek2.scale.modulate(5.toFraction(21)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.GREEN, 10, 3.75f,true)
    val h6 = dek2.scale.modulate(5.toFraction(3)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color(0, 255, 180), 10, 3.75f,false)
    val h7 = dek2.scale.modulate(1.toFraction(7)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color(0, 255, 180), 10, 3.75f,false)
    val h8 = mmod.toHighlight(Color.black, 8, 2.5f, true, true )
    val h9 = mmod.toHighlight(Color.black, 8, 2.5f, false, true )
    val h10 = dek3.toXYStructure(map, inc9FactorScale).toHighlight(Color.RED, 10, 3.75f, true)
    /* val hex1 = CPSXany( CPSName(listOf(1, 3, 7, 13),2))
    val hex2 = CPSXany( CPSName(listOf(1, 5, 11, 13),2))
    val mod = hex1.cpsModulation(hex2).toXYStructure(map, fac)
    val struct1  = hex1.toXYStructure(map,fac).toHighlight(Color.blue, true)
    val struct1a = hex1.scale.modulate(1.toFraction(3)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.cyan, true)
    val struct1b = hex1.scale.modulate(1.toFraction(7)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.magenta, true)
    val struct1c = hex1.scale.modulate(11.toFraction(3)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.yellow, true)
    val struct1d = hex1.scale.modulate(11.toFraction(7)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.orange, true)
    val struct1e = hex1.scale.modulate(11.toFraction(21)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.green, true)
    val struct2  = hex2.toXYStructure(map, fac).toHighlight(Color.RED, false)
    val struct2a  = hex2.toXYStructure(map,fac).toHighlight(Color.RED, true)
    val fullMod = multiModulateCPS(CPSPair(hex2, hex1)).toXYStructure(map,fac)
    val struct3 = fullMod.toHighlight(Color.black, true,true)
    val struct3a = fullMod.toHighlight(Color.black, false,true)*/
    val diagram = mod.toDiagram(Color.black, 10, 1.4f, listOf(h1, h2, h3, h4, h5, h6, h7, h8, h9, h10)).toProcessedDiagram(100, 50)
    val frame = DiagramScreen("Dekany Modulation", diagram)
    frame.isVisible = true
}

fun main() {

    EventQueue.invokeLater(::createAndShowGUI)

}



