package com.xrosstools.idea.gef.routers

import com.xrosstools.idea.gef.figures.Figure

import java.awt.*

class MidpointLocator @JvmOverloads constructor(protected val index: Int = 0) : ConnectionLocator {
    private var figure: Figure? = null

    override fun getLocation(points: PointList): Point {
        val start = points.get(index)
        val end = points.get(index + 1)

        val loc = Point((start.x + end.x) / 2, (start.y + end.y) / 2)
        if (figure != null)
            loc.translate(-figure!!.width / 2, -figure!!.height / 2)
        return loc
    }

    fun getFigure(): Figure? {
        return figure
    }

    fun setFigure(figure: Figure): MidpointLocator {
        this.figure = figure
        return this
    }
}
