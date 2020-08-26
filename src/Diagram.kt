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
        for(point in diagram.pointOutline) {
            val w = point.width
            g2d.color = point.colour
            g2d.fillOval(point.x - w/2, point.y - w/2, w, w)
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
    val map:XYMap = penta2XYMap
    val fac:FactorScale = penta2Factb
    val hex1 = CPSXany( CPSName(listOf(1, 3, 5, 7),2))
    val hex2 = CPSXany( CPSName(listOf(1, 5, 11, 13),2))
    val mod = hex1.cpsModulation(hex2).toXYStructure(map, fac)
    val struct1  = hex1.toXYStructure(map,fac).toHighlight(Color.blue, true)
    val struct1a = hex1.scale.modulate(11.toFraction(3)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.cyan, true)
    val struct1b = hex1.scale.modulate(11.toFraction(7)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.magenta, true)
    val struct1c = hex1.scale.modulate(13.toFraction(3)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.yellow, true)
    val struct1d = hex1.scale.modulate(13.toFraction(7)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.orange, true)
    val struct1e = hex1.scale.modulate(143.toFraction(21)).toFactorScale()
            .makeStructure(fac).toXYStructure(map).toHighlight(Color.green, true)
    val struct2  = hex2.toXYStructure(map, fac).toHighlight(Color.RED, false)
    val struct2a  = hex2.toXYStructure(map,fac).toHighlight(Color.RED, true)
    val fullMod = multiModulateCPS(CPSPair(hex2, hex1)).toXYStructure(map,fac)
    val struct3 = fullMod.toHighlight(Color.black, true,true)
    val struct3a = fullMod.toHighlight(Color.black, false,true)
    val diagram = mod.toDiagram(Color.black, listOf(struct1, struct1a, struct1b, struct1c, struct1d,
            struct1e, struct2, struct2a, struct3, struct3a)).toProcessedDiagram()
    val frame = DiagramScreen("Canvas Size", diagram)
    frame.isVisible = true
}

fun main() {

    EventQueue.invokeLater(::createAndShowGUI)

}



