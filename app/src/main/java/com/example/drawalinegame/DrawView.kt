package com.example.drawalinegame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.PathParser

class DrawView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var currentPath: Path? = null
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 30f // Độ dày nét vẽ
        color = Color.YELLOW // Màu vẽ của người dùng
    }

    private var squareEdges: MutableList<Pair<Path, Paint>> = mutableListOf() // Danh sách lưu cạnh và màu sắc từ JSON
    private val userDrawnPaths: MutableList<Path> = mutableListOf() // Đường vẽ của người dùng

    private var squareBounds: RectF? = null // Giữ các thông tin về bounds của hình vuông
    private var snapThreshold = 50f // Khoảng cách tối thiểu để snap vào cạnh

    // Trạng thái đã vẽ các cạnh
    private var topDrawn = false
    private var rightDrawn = false
    private var bottomDrawn = false
    private var leftDrawn = false

    // Kiểm tra cạnh cuối cùng đã vẽ
    private var lastEdge: Int? = null

    // Thiết lập hình từ JSON
    fun setShape(shape: ShapeModel, shapeType: String) {
        squareEdges.clear()
        squareBounds = RectF() // Khởi tạo lại bounds của hình vuông

        when (shapeType) {
            "hinhVuong" -> shape.hinhVuong?.let { square ->
                // Thêm các cạnh từ JSON vào danh sách, cùng với màu fill
                addEdge(square.left)
                addEdge(square.top)
                addEdge(square.right)
                addEdge(square.bottom)
            }
        }
        invalidate() // Vẽ lại View
    }

    // Hàm để thêm từng cạnh của hình vào danh sách `squareEdges`
    private fun addEdge(pathData: String) {
        val path = parsePathData(pathData)
        val fillColor = UtilsPainting.extractColor(pathData)

        // Lấy chiều rộng của cạnh từ đường Path
        val edgePaint = Paint().apply {
            style = Paint.Style.FILL // Chỉnh thành STROKE để vẽ đường viền
            isAntiAlias = true
            strokeWidth = 30f // Chiều rộng nét vẽ bằng chiều rộng của cạnh
            color = fillColor
        }
        squareEdges.add(Pair(path, edgePaint))

        // Cập nhật vùng bounds của hình vuông
        val bounds = RectF()
        path.computeBounds(bounds, true)
        squareBounds?.union(bounds)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        squareBounds?.let { bounds ->
            // Dịch chuyển canvas để căn giữa hình vuông
            val offsetX = (width - bounds.width()) / 2 - bounds.left
            val offsetY = (height - bounds.height()) / 2 - bounds.top
            canvas.translate(offsetX, offsetY)

            // Vẽ các cạnh của hình từ JSON
            squareEdges.forEach { (path, paint) -> canvas.drawPath(path, paint) }

            // Vẽ đường của người dùng
            userDrawnPaths.forEach { path -> canvas.clipPath(path) }

            // Vẽ đường hiện tại
            currentPath?.let { path -> canvas.drawPath(path, this.paint) }

            // Nếu người dùng đã hoàn thành việc vẽ các cạnh, đổi màu thành xanh
            if (areAllEdgesDrawn()) {
                this.paint.color = Color.GREEN // Đổi màu thành xanh lá khi hoàn tất
                squareEdges.forEach { (path, _) -> canvas.drawPath(path, this.paint) }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        squareBounds?.let { bounds ->
            val x = event.x - ((width - bounds.width()) / 2 - bounds.left) // Điều chỉnh theo offset
            val y = event.y - ((height - bounds.height()) / 2 - bounds.top) // Điều chỉnh theo offset

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val snappedPoint = snapToSquare(x, y)
                    currentPath = Path().apply { moveTo(snappedPoint.x, snappedPoint.y) }
                    lastEdge = getEdgeIndex(snappedPoint.x, snappedPoint.y)
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    currentPath?.let {
                        val snappedPoint = snapToSquare(x, y)
                        if (isOnEdge(snappedPoint.x, snappedPoint.y)) { // Kiểm tra có nằm trên cạnh không
                            it.lineTo(snappedPoint.x, snappedPoint.y)
                            invalidate()
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    currentPath?.let {
                        // Nếu người dùng chưa vẽ kín, xóa tất cả các đường vẽ
                        if (!areAllEdgesDrawn()) {
                            userDrawnPaths.clear() // Xóa các đường vẽ trước đó
                        } else {
                            userDrawnPaths.add(it)
                        }
                        updateEdgeStatus(x, y)
                        currentPath = null
                    }
                    invalidate()
                    return true
                }

                else -> {}
            }
        }
        return false
    }

    // Hàm snap vị trí vẽ vào cạnh gần nhất của hình vuông
    private fun snapToSquare(x: Float, y: Float): PointF {
        val snappedX = when {
            x <= squareBounds!!.left + snapThreshold -> squareBounds!!.left
            x >= squareBounds!!.right - snapThreshold -> squareBounds!!.right
            else -> x
        }

        val snappedY = when {
            y <= squareBounds!!.top + snapThreshold -> squareBounds!!.top
            y >= squareBounds!!.bottom - snapThreshold -> squareBounds!!.bottom
            else -> y
        }

        return PointF(snappedX, snappedY)
    }

    // Kiểm tra xem tất cả các cạnh đã được vẽ chưa
    private fun areAllEdgesDrawn(): Boolean {
        return topDrawn && rightDrawn && bottomDrawn && leftDrawn
    }

    // Cập nhật trạng thái của cạnh đang vẽ
    private fun updateEdgeStatus(x: Float, y: Float) {
        val edgeIndex = getEdgeIndex(x, y)
        when (edgeIndex) {
            0 -> topDrawn = true
            1 -> rightDrawn = true
            2 -> bottomDrawn = true
            3 -> leftDrawn = true
        }
    }

    // Xác định cạnh mà người dùng đang vẽ
    private fun getEdgeIndex(x: Float, y: Float): Int? {
        squareBounds?.let { bounds ->
            return when {
                y in bounds.top..(bounds.top + snapThreshold) -> 0 // Cạnh trên
                x in bounds.right..(bounds.right + snapThreshold) -> 1 // Cạnh phải
                y in bounds.bottom..(bounds.bottom + snapThreshold) -> 2 // Cạnh dưới
                x in bounds.left..(bounds.left + snapThreshold) -> 3 // Cạnh trái
                else -> null
            }
        }
        return null
    }

    // Kiểm tra xem điểm có nằm trên cạnh không
    private fun isOnEdge(x: Float, y: Float): Boolean {
        squareEdges.forEach { (path, _) ->
            val region = Region()
            path.computeBounds(RectF(), true) // Cần xác định các bounds cho đường path
            region.setPath(path, Region(Rect(0, 0, width, height))) // Xác định vùng cho cạnh
            if (region.contains(x.toInt(), y.toInt())) {
                return true // Nếu điểm nằm trong vùng của cạnh
            }
        }
        return false
    }

    // Hàm chuyển đổi dữ liệu pathData từ JSON thành Path
    private fun parsePathData(pathData: String): Path {
        val path = Path()
        val extractedData = UtilsPainting.extractPathData(pathData)
        PathParser.createPathFromPathData(extractedData)?.let {
            path.addPath(it)
        }
        return path
    }
}
