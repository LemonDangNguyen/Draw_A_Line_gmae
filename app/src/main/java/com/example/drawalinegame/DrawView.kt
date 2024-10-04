package com.example.drawalinegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.PathParser

class DrawView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var currentPath: Path? = null
    private var currentColor: Int = 0
    private var bounds: RectF? = null

    fun setShape(shape: ShapeModel, shapeType: String) {
        Log.d("DrawView", "Setting shape: $shapeType")
        currentPath = null
        bounds = RectF()

        when (shapeType) {
            "line" -> shape.line?.let { lineData ->
                currentPath = parsePathData(lineData.line)
                currentColor = UtilsPainting.extractColor(lineData.line)
                bounds?.union(UtilsPainting.getPathBounds(lineData.line))
            }
            "hinhVuong" -> shape.hinhVuong?.let { square ->
                val squarePaths = listOf(square.left, square.bottom, square.right, square.top)
                val path = Path()
                for (pathData in squarePaths) {
                    path.addPath(parsePathData(pathData))
                }
                currentPath = path
                currentColor = UtilsPainting.extractColor(squarePaths.first())
                squarePaths.forEach { bounds?.union(UtilsPainting.getPathBounds(it)) }
            }
            // Thêm trường hợp khác nếu cần
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bounds?.let { b ->
            canvas.translate((width - b.width()) / 2, (height - b.height()) / 2)
            currentPath?.let {
                paint.color = currentColor
                canvas.drawPath(it, paint)
            }
        }
    }

    private fun parsePathData(pathData: String): Path {
        val path = Path()
        val extractedData = UtilsPainting.extractPathData(pathData)
        PathParser.createPathFromPathData(extractedData)?.let {
            path.addPath(it)
        }
        return path
    }
}
