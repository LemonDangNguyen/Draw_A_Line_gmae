package com.example.drawalinegame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.PathParser

class DrawView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var currentPath: Path? = null
    private val userPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 80f // Độ dày nét vẽ của người dùng
        color = Color.YELLOW // Màu vẽ của người dùng
    }

    private var squareEdges: MutableList<Pair<Path, Paint>> = mutableListOf() // Danh sách lưu cạnh và màu sắc từ JSON
    private val userDrawnPaths: MutableList<Path> = mutableListOf() // Đường vẽ của người dùng

    private var squareBounds: RectF? = null // Giữ các thông tin về bounds của hình vuông
    private var snapThreshold = 40f // Khoảng cách tối thiểu để snap vào cạnh

    private var lastPoint: PointF? = null // Điểm cuối cùng của nét vẽ
    private var topDrawn = false
    private var rightDrawn = false
    private var bottomDrawn = false
    private var leftDrawn = false

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

        // Paint riêng cho các cạnh từ JSON, sử dụng màu #FFD9D9D9
        val edgePaint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = 30f // Chiều rộng nét vẽ bằng chiều rộng của cạnh
            color = Color.parseColor("#FFD9D9D9") // Màu ban đầu
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

            // Vẽ các cạnh của hình từ JSON bằng paint riêng cho mỗi cạnh
            squareEdges.forEach { (path, paint) -> canvas.drawPath(path, paint) }

            // Vẽ đường của người dùng với userPaint
            userDrawnPaths.forEach { path -> canvas.drawPath(path, this.userPaint) }

            // Vẽ đường hiện tại mà người dùng đang vẽ
            currentPath?.let { path -> canvas.drawPath(path, this.userPaint) }

            // Nếu người dùng đã hoàn thành việc vẽ các cạnh, đổi màu thành xanh cho tất cả các đường user vẽ
            if (areAllEdgesDrawn()) {
                this.userPaint.color = Color.GREEN // Đổi màu đường người dùng vẽ thành xanh
                userDrawnPaths.forEach { path -> canvas.drawPath(path, this.userPaint) }

                // Đổi màu các cạnh của hình vuông
                squareEdges.forEach { (path, paint) ->
                    paint.color = Color.GREEN // Đổi màu các cạnh thành xanh
                    canvas.drawPath(path, paint)
                }
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
                    if (isOnEdge(snappedPoint.x, snappedPoint.y)) {
                        currentPath = Path().apply { moveTo(snappedPoint.x, snappedPoint.y) }
                        lastPoint = snappedPoint // Lưu điểm bắt đầu
                        return true
                    } else {
                        return false
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    currentPath?.let {
                        val snappedPoint = snapToSquare(x, y)
                        if (isOnEdge(snappedPoint.x, snappedPoint.y)) {
                            lastPoint?.let { last ->
                                // Sử dụng lineTo để nối trực tiếp các điểm
                                it.lineTo(snappedPoint.x, snappedPoint.y)
                            }
                            lastPoint = snappedPoint // Cập nhật điểm cuối
                            invalidate() // Cập nhật và vẽ lại
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    currentPath?.let { path ->
                        val snappedPoint = snapToSquare(x, y)

                        // Kiểm tra xem điểm đầu và điểm cuối có gần nhau không (snap lại điểm cuối)
                        val startPoint = PointF()
                        val pathMeasure = PathMeasure(path, false)
                        val coords = FloatArray(2)

                        // Lấy điểm đầu của đường
                        pathMeasure.getPosTan(0f, coords, null)
                        startPoint.set(coords[0], coords[1])

                        // Nếu khoảng cách giữa điểm đầu và điểm cuối nhỏ hơn ngưỡng snapThreshold, đổi màu đường vẽ
                        if (distanceBetween(startPoint.x, startPoint.y, snappedPoint.x, snappedPoint.y) < snapThreshold) {
                            userPaint.color = Color.GREEN // Đổi màu cho đường vẽ
                        }

                        if (!areAllEdgesDrawn()) {
                            userDrawnPaths.clear() // Xóa đường vẽ nếu chưa hoàn thành
                        } else {
                            userDrawnPaths.add(path) // Thêm đường vẽ vào danh sách
                        }
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

    // Hàm tính khoảng cách giữa hai điểm
    private fun distanceBetween(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    // Hàm snap vị trí vẽ vào cạnh gần nhất của hình vuông
    private fun snapToSquare(x: Float, y: Float): PointF {
        val bounds = squareBounds ?: return PointF(x, y) // Trả về tọa độ gốc nếu bounds chưa có giá trị

        val threshold = snapThreshold

        // Snap vào cạnh trái hoặc phải
        val snappedX = when {
            x <= bounds.left + threshold -> bounds.left
            x >= bounds.right - threshold -> bounds.right
            else -> x
        }

        // Snap vào cạnh trên hoặc dưới
        val snappedY = when {
            y <= bounds.top + threshold -> bounds.top
            y >= bounds.bottom - threshold -> bounds.bottom
            else -> y
        }

        // Nếu điểm quá gần cả hai trục (góc), ưu tiên giữ nguyên một trục và snap vào trục kia
        return if (Math.abs(snappedX - x) < threshold && Math.abs(snappedY - y) < threshold) {
            if (Math.abs(snappedX - bounds.left) < Math.abs(snappedY - bounds.top)) {
                PointF(snappedX, y)
            } else {
                PointF(x, snappedY)
            }
        } else {
            PointF(snappedX, snappedY)
        }
    }

    // Kiểm tra xem tất cả các cạnh đã được vẽ chưa
    private fun areAllEdgesDrawn(): Boolean {
        return topDrawn && rightDrawn && bottomDrawn && leftDrawn
    }

    // Kiểm tra xem điểm có nằm trên cạnh không
    private fun isOnEdge(x: Float, y: Float): Boolean {
        // Kiểm tra điểm có nằm gần bất kỳ cạnh nào không
        // Bạn cần định nghĩa logic cho phương thức này
        return true // Thay đổi theo logic kiểm tra của bạn
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
