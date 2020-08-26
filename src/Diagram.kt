package org.myprojects.hexany

import java.awt.*
import javax.swing.JFrame
import javax.swing.JPanel

class DiagramScreen(title: String, xSize:Int, ySize:Int) : JFrame() {

    init {
        createUI(title, xSize, ySize)
    }

    private fun createUI(title: String, xSize:Int, ySize:Int) {

        setTitle(title)

        val diagram = Diagram(xSize, ySize, DiagramLine(14, 154, 295,
                20, 3.3f, Color.CYAN, 0))
        add(diagram)

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(xSize+17, ySize+40)
        setLocationRelativeTo(null)
    }



}

class Diagram(val xSize:Int, val ySize:Int, val line: DiagramLine):JPanel(){
    private fun doDrawing(g:Graphics){
        val g2d:Graphics2D = g as Graphics2D
        val bs1 = BasicStroke(2f)
        g2d.drawLine(0, 0, xSize, ySize)
        g2d.stroke = bs1
        g2d.drawLine(0, ySize, xSize, 0)
        g2d.stroke = BasicStroke(line.width)
        g2d.color = line.colour
        g2d.drawLine(line.x1, line.y1, line.x2, line.y2)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        doDrawing(g)
    }
}

private fun createAndShowGUI() {


    val frame = DiagramScreen("Canvas Size", 400, 250)
    frame.isVisible = true
}

fun main() {
    EventQueue.invokeLater(::createAndShowGUI)
}



