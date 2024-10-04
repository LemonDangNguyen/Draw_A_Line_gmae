package com.example.drawalinegame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.PathParser

class DrawView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL // Đổi thành STROKE để vẽ viền
        isAntiAlias = true
        strokeWidth = 5f // Độ dày của nét vẽ
        color = Color.YELLOW // Màu viền mặc định
    }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL // Để tô màu
        isAntiAlias = true
        color = Color.YELLOW // Màu tô của đường vẽ của người dùng
    }

    private var currentPath: Path? = null
    private val shapes: MutableList<Pair<Path, Int>> = mutableListOf() // Danh sách lưu các hình từ JSON
    private val userDrawnPaths: MutableList<Path> = mutableListOf() // Đường vẽ của người dùng
    private var bounds: RectF? = null
    private val pathMeasure = PathMeasure()

    // Cờ để kiểm tra xem user đã vẽ kín chưa
    private var isShapeCompleted = false

    // Phương thức để thêm hình vào danh sách
    fun setShape(shape: ShapeModel, shapeType: String) {
        shapes.clear()
        bounds = RectF()

        when (shapeType) {
            "line" -> shape.line?.let { lineData ->
                val path = parsePathData(lineData.line)
                val color = UtilsPainting.extractColor(lineData.line)
                shapes.add(Pair(path, color))
                bounds?.union(UtilsPainting.getPathBounds(lineData.line))
            }
            "hinhVuong" -> shape.hinhVuong?.let { square ->
                val squarePaths = listOf(square.left, square.bottom, square.right, square.top)
                val path = Path()
                squarePaths.forEach { path.addPath(parsePathData(it)) }
                val color = UtilsPainting.extractColor(squarePaths.first())
                shapes.add(Pair(path, color))
                squarePaths.forEach { bounds?.union(UtilsPainting.getPathBounds(it)) }
            }
            "tamGiac" -> shape.tamGiac?.let { triangle ->
                val trianglePaths = listOf(triangle.canh1, triangle.canh2, triangle.canh3)
                val path = Path()
                trianglePaths.forEach { path.addPath(parsePathData(it)) }
                val color = UtilsPainting.extractColor(trianglePaths.first())
                shapes.add(Pair(path, color))
                trianglePaths.forEach { bounds?.union(UtilsPainting.getPathBounds(it)) }
            }
        }

        invalidate() // Vẽ lại View
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bounds?.let { b ->
            // Dịch chuyển canvas để căn giữa
            canvas.translate((width - b.width()) / 2 - b.left, (height - b.height()) / 2 - b.top)

            // Vẽ các đường từ JSON
            shapes.forEach { (shapePath, color) ->
                paint.color = color
                canvas.drawPath(shapePath, paint)
            }

            // Vẽ đường vẽ của người dùng
            userDrawnPaths.forEach { path ->
                canvas.drawPath(path, fillPaint) // Tô màu cho đường vẽ
                canvas.drawPath(path, paint) // Vẽ lại đường viền
            }

            // Vẽ đường hiện tại
            currentPath?.let { path ->
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, paint)
            }

            // Thay đổi màu khi hoàn thành
            if (isShapeCompleted) {
                fillPaint.color = Color.GREEN // Màu xanh nếu hoàn thành
                canvas.drawPath(shapes[0].first, fillPaint) // Tô kín hình đã vẽ
            }
        }
    }

    // Xử lý sự kiện chạm
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Điều chỉnh vị trí chạm dựa trên bounds
        val adjustedX = event.x - ((width - (bounds?.width() ?: 0f)) / 2) - (bounds?.left ?: 0f)
        val adjustedY = event.y - ((height - (bounds?.height() ?: 0f)) / 2) - (bounds?.top ?: 0f)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTouchingPath(adjustedX, adjustedY)) {
                    currentPath = Path().apply { moveTo(adjustedX, adjustedY) }
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath?.let {
                    it.lineTo(adjustedX, adjustedY) // Vẽ tới vị trí hiện tại
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                currentPath?.let {
                    userDrawnPaths.add(it) // Thêm đường vẽ của người dùng vào danh sách
                    checkCompletion() // Kiểm tra xem đã hoàn thành hay chưa
                    currentPath = null // Đặt lại currentPath
                }
                invalidate()
                return true
            }
        }
        return false
    }

    // Kiểm tra xem người dùng đã vẽ kín hình chưa
    private fun checkCompletion() {
        val totalPathLength = shapes.sumOf { shape ->
            pathMeasure.setPath(shape.first, false)
            pathMeasure.length.toDouble() // Ép kiểu thành Double
        }

        val drawnPathLength = userDrawnPaths.sumOf { path ->
            pathMeasure.setPath(path, false)
            pathMeasure.length.toDouble() // Ép kiểu thành Double
        }

        // Nếu chiều dài đường vẽ của người dùng đạt ít nhất 95% chiều dài hình gốc
        isShapeCompleted = drawnPathLength >= totalPathLength * 0.95
    }


    // Kiểm tra xem điểm chạm có gần đường không
    private fun isTouchingPath(x: Float, y: Float): Boolean {
        shapes.forEach { (shapePath, _) ->
            val pathMeasure = PathMeasure(shapePath, false)
            val length = pathMeasure.length
            val tolerance = 10f // Độ chính xác khi chạm vào đường

            for (i in 0 until length.toInt() step 5) {
                val pos = FloatArray(2)
                pathMeasure.getPosTan(i.toFloat(), pos, null)
                val distance = Math.sqrt(((pos[0] - x) * (pos[0] - x) + (pos[1] - y) * (pos[1] - y)).toDouble()).toFloat()

                if (distance < tolerance) {
                    return true
                }
            }
        }
        return false
    }

    // string -> path
    private fun parsePathData(pathData: String): Path {
        val path = Path()
        val extractedData = UtilsPainting.extractPathData(pathData)
        PathParser.createPathFromPathData(extractedData)?.let {
            path.addPath(it)
        }
        return path
    }
}
