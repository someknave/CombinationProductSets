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
/*
class Picture(val xSize:Int, val ySize:Int): BufferedImage(){

}*/

class Diagram(val xSize:Int, val ySize:Int, val diagram: ProcessedDiagram):JPanel(){
    private fun doDrawing(g:Graphics){
        val g2d:Graphics2D = g as Graphics2D
        val font = Font("Serif", Font.BOLD, 12)
        g2d.font = font
        val fontMetrics = g2d.getFontMetrics()
        g2d.color = Color.white
        g2d.fillRect(0, 0, xSize, ySize)
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
        if (diagram.labelMode != 0) {
            val divisorsOn = diagram.labels.unzip().second.any { it != "1" }
            val h = fontMetrics.height
            val a = fontMetrics.ascent
            g2d.stroke = BasicStroke(2.0f, 2, 2)

            for (i in diagram.labels.indices) {
                val labx = diagram.labelpoints[i].x + diagram.xOffset
                val laby = diagram.labelpoints[i].y + diagram.yOffset
                val numWidth = fontMetrics.stringWidth(diagram.labels[i].first)
                if (divisorsOn) {
                    val divWidth = fontMetrics.stringWidth(diagram.labels[i].second)
                    val max = maxOf(numWidth, divWidth)
                    val halfDif = (numWidth - divWidth)/2
                    g2d.color = Color(255, 255, 255, 170)
                    g2d.fillRect(labx, laby, max, 2 * h)
                    g2d.color = Color.black
                    g2d.drawLine(labx, laby + h, labx + max, laby + h)
                    if (halfDif >= 0) {
                        g2d.drawString(diagram.labels[i].first, labx, laby + a)
                        g2d.drawString(diagram.labels[i].second, labx + halfDif, laby + a + h)
                    } else {
                        g2d.drawString(diagram.labels[i].first, labx - halfDif, laby + a)
                        g2d.drawString(diagram.labels[i].second, labx, laby + a + h)
                    }

                } else {
                    g2d.color = Color.green
                    g2d.fillRect(labx, laby, numWidth,  h)
                    g2d.color = Color.black
                    g2d.drawString(diagram.labels[i].first, labx, laby + a)
                }




            }
        }

    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        doDrawing(g)
    }
}




private fun createAndShowGUI() {
    val map:XYMap = penta2XYMap
    val fac:FactorScale = penta2Fact
    val hex1 = CPSXany(CPSName(listOf(1, 3, 7, 13),2))
    val hex2 = CPSXany(CPSName(listOf(1, 5, 11, 13),2))
    val eiko = CPSXany(CPSName(listOf(1, 3, 5, 7, 11, 13),3)).scale.modulate(8.toFraction(7))
    val mod = hex1.cpsModulation(hex2)
    val multmod = multiModulateCPS(CPSPair(hex2, hex1))
    val dek2 = CPSXany(CPSName(listOf(1, 3, 5, 7, 11, 13),2))
    val h1 = hex1.toXYStructure(map, fac).toHighlight(Color.blue)
    val h2 = hex2.toXYStructure(map, fac).toHighlight(Color.red, outline = true)
    val h3 = hex1.scale.modulate(5.toFraction(7)).toFactorScale().makeStructure(fac).toXYStructure(map).toHighlight(Color.cyan, outline = true)
    val h4 = hex1.scale.modulate(11.toFraction(7)).toFactorScale().makeStructure(fac).toXYStructure(map).toHighlight(Color.cyan)
    val h5 = hex1.scale.modulate(5.toFraction(3)).toFactorScale().makeStructure(fac).toXYStructure(map).toHighlight(Color.green)
    val h6 = hex1.scale.modulate(11.toFraction(3)).toFactorScale().makeStructure(fac).toXYStructure(map).toHighlight(Color.green, outline = true)
    val h7 = hex2.toXYStructure(map, fac).toHighlight(Color.red)
    val h8 = multmod.toXYStructure(map, fac).toHighlight(Color.LIGHT_GRAY, ghost = true)
    val struct = mod.toXYStructure(map, fac)


    val diagram = struct.toDiagram(Color.YELLOW, 12, 3.5f, listOf(h1, h2, h3, h4, h5, h6, h7, h8)).toProcessedDiagram(100, 50, labelMode = 2)
    val frame = DiagramScreen("Dekany Modulation", diagram)
    frame.isVisible = true
}

fun main() {

    EventQueue.invokeLater(::createAndShowGUI)

}





