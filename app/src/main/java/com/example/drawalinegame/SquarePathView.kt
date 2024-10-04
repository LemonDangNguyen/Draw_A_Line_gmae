package com.example.drawalinegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser

class SquarePathView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val pathList = mutableListOf<PathData>()  // Lưu thông tin về path và màu sắc
    private val completedPaths = mutableSetOf<Path>()
    private var currentPath: Path? = null
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private var squareData: ShapeModel.SquareData? = null

    data class PathData(val path: Path, val color: Int) // Lớp lưu path và màu sắc

    // Thiết lập dữ liệu hình vuông
    fun setSquareData(squareData: ShapeModel.SquareData) {
        this.squareData = squareData
        createSquarePaths()
        invalidate()
    }

    // Tạo các đường path từ dữ liệu hình vuông
    private fun createSquarePaths() {
        val square = squareData ?: return
        pathList.clear()

        try {
            // Trích xuất và tạo path từ dữ liệu của từng cạnh, kèm theo màu sắc
            val leftPath = PathParser.createPathFromPathData(UtilsPainting.extractPathData(square.left))
            val rightPath = PathParser.createPathFromPathData(UtilsPainting.extractPathData(square.right))
            val topPath = PathParser.createPathFromPathData(UtilsPainting.extractPathData(square.top))
            val bottomPath = PathParser.createPathFromPathData(UtilsPainting.extractPathData(square.bottom))

            // Giả định bạn có các màu sắc từ JSON
            val leftColor = UtilsPainting.extractColor(square.left)
            val rightColor = UtilsPainting.extractColor(square.right)
            val topColor = UtilsPainting.extractColor(square.top)
            val bottomColor = UtilsPainting.extractColor(square.bottom)

            // Thêm các path và màu vào danh sách pathList
            pathList.add(PathData(leftPath, leftColor))
            pathList.add(PathData(rightPath, rightColor))
            pathList.add(PathData(topPath, topColor))
            pathList.add(PathData(bottomPath, bottomColor))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Lỗi dữ liệu hình học", Toast.LENGTH_SHORT).show()
        }
    }

    // Vẽ các đường path
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (pathList.isEmpty()) return

        // Không scale hay căn chỉnh, vẽ path với màu và tỷ lệ chính xác từ JSON
        for (pathData in pathList) {
            paint.color = pathData.color
            canvas.drawPath(pathData.path, paint)
        }
    }

    // Kiểm tra sự kiện khi người dùng vẽ
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = findPathAtPoint(x, y)
                return currentPath != null
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath?.let {
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                currentPath?.let {
                    completedPaths.add(it)
                    checkIfCompleted()
                    currentPath = null
                }
                invalidate()
            }
        }
        return true
    }

    // Kiểm tra nếu người dùng đã vẽ xong tất cả các đường
    private fun checkIfCompleted() {
        if (completedPaths.size == pathList.size) {
            Toast.makeText(context, "Thắng!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findPathAtPoint(x: Float, y: Float): Path? {
        // Trích xuất path từ các cạnh của hình vuông
        squareData?.let { square ->
            val paths = listOf(
                UtilsPainting.extractPathData(square.left),
                UtilsPainting.extractPathData(square.right),
                UtilsPainting.extractPathData(square.top),
                UtilsPainting.extractPathData(square.bottom)
            )

            for (pathData in paths) {
                val bounds = UtilsPainting.getPathBounds(pathData)
                if (bounds.contains(x, y)) {
                    return androidx.core.graphics.PathParser.createPathFromPathData(pathData)
                }
            }
        }
        return null
    }

}
