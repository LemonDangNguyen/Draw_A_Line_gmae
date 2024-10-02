package com.example.drawalinegame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.PathParser

class SquarePathView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Paint để vẽ từ JSON
    private val paint: Paint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
    }

    // Paint để vẽ cho đường vẽ của user
    private val userPathPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.RED // Màu đỏ cho đường vẽ tay
    }

    // Biến để lưu dữ liệu hình vuông
    private var squareData: ShapeModel.SquareData? = null

    // Đường vẽ của người dùng
    private var userPath: Path = Path()

    // Phương thức để cập nhật dữ liệu hình vuông từ JSON
    fun setSquareData(squareData: ShapeModel.SquareData) {
        this.squareData = squareData
        invalidate() // Gọi invalidate() để vẽ lại
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Lấy kích thước canvas
        val canvasWidth = width
        val canvasHeight = height

        // Tính toán dịch chuyển để căn giữa Path
        squareData?.let { data ->
            // Tính toán các bounds của toàn bộ path
            val pathBounds = getCombinedPathBounds(data)
            val dx = (canvasWidth - pathBounds.width()) / 2 - pathBounds.left
            val dy = (canvasHeight - pathBounds.height()) / 2 - pathBounds.top

            // Dịch chuyển canvas để vẽ Path ở giữa màn hình
            canvas.save()
            canvas.translate(dx, dy)

            // Vẽ các cạnh từ JSON
            drawPath(canvas, data.left)
            drawPath(canvas, data.bottom)
            drawPath(canvas, data.right)
            drawPath(canvas, data.top)

            // Phục hồi canvas để vẽ đường userPath không bị ảnh hưởng
            canvas.restore()
        }

        // Vẽ đường mà người dùng vẽ
        canvas.drawPath(userPath, userPathPaint)
    }

    // Hàm vẽ Path dựa vào pathData từ JSON
    private fun drawPath(canvas: Canvas, pathData: String) {
        val path = PathParser.createPathFromPathData(UtilsPainting.extractPathData(pathData))
        val colorString = UtilsPainting.extractFillColor(pathData)

        // Thiết lập màu cho Paint từ JSON
        paint.color = Color.parseColor(colorString)

        // Vẽ Path từ JSON
        canvas.drawPath(path, paint)
    }

    // Xử lý sự kiện chạm của người dùng
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Bắt đầu một đường mới khi chạm vào màn hình
                userPath.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                // Kéo để tạo ra đường khi di chuyển ngón tay
                userPath.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                // Khi người dùng nhấc ngón tay, đường vẽ đã hoàn thành
                userPath.lineTo(x, y)
            }
        }

        // Gọi invalidate để vẽ lại đường vẽ của người dùng
        invalidate()
        return true
    }

    // Hàm tính toán kích thước tổng thể của tất cả các path để căn giữa
    private fun getCombinedPathBounds(squareData: ShapeModel.SquareData): RectF {
        val bounds = RectF()
        val pathList = listOf(squareData.left, squareData.bottom, squareData.right, squareData.top)

        pathList.forEach { pathData ->
            val pathBounds = UtilsPainting.getPathBounds(pathData)
            bounds.union(pathBounds) // Hợp nhất tất cả các bounds
        }
        return bounds
    }
}
