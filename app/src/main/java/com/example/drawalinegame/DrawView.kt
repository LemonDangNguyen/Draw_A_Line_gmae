package com.example.drawalinegame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.PathParser
import kotlin.math.pow

class DrawView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.YELLOW  // Màu khi vẽ
        isAntiAlias = true
    }

    private val pathPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private var path: Path? = null
    private var userPath: Path = Path()  // Đường người dùng vẽ
    private var isCompleted = false  // Kiểm tra xem đã hoàn thành nối hay chưa
    private lateinit var pathMeasure: PathMeasure

    // Thiết lập đường Path và các điểm đầu cuối
    fun setShape(shape: ShapeModel) {
        path = Path() // Khởi tạo lại đường vẽ mới cho mỗi lần gọi
        userPath.reset()

        shape.line?.let { lineData ->
            pathPaint.color = UtilsPainting.extractColor(lineData.line) // Lấy màu từ line
            path = parsePathData(lineData.line) // Vẽ line

            // Thiết lập PathMeasure để tính toán trên đường Path
            path?.let {
                pathMeasure = PathMeasure(it, false)
            }
        }

        invalidate()  // Yêu cầu vẽ lại view
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path?.let {
            // Lấy giới hạn (bounds) của path
            val bounds = RectF()
            it.computeBounds(bounds, true)

            // Tính toán để di chuyển canvas sao cho path ở giữa view
            val dx = (width - bounds.width()) / 2 - bounds.left
            val dy = (height - bounds.height()) / 2 - bounds.top

            canvas.save()  // Lưu trạng thái canvas
            canvas.translate(dx, dy)  // Di chuyển canvas để path ở giữa

            // Vẽ path chính từ dữ liệu JSON
            canvas.drawPath(it, pathPaint)

            // Vẽ đường của người dùng
            canvas.drawPath(userPath, paint)

            canvas.restore()  // Phục hồi trạng thái ban đầu của canvas
        }
    }

//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        path?.let {
//            canvas.drawPath(it, pathPaint) // Vẽ path chính từ dữ liệu JSON
//        }
//        canvas.drawPath(userPath, paint) // Vẽ đường của người dùng
//    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isCompleted) return false // Nếu đã nối thành công thì không vẽ nữa

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val closestPoint = getClosestPointOnPath(event.x, event.y)
                userPath.moveTo(closestPoint.x, closestPoint.y)  // Bắt đầu từ điểm gần nhất trên Path
            }
            MotionEvent.ACTION_MOVE -> {
                val closestPoint = getClosestPointOnPath(event.x, event.y)
                userPath.lineTo(closestPoint.x, closestPoint.y)  // Tiếp tục vẽ đường dính vào Path
                invalidate()  // Cập nhật lại view
            }
            MotionEvent.ACTION_UP -> {
                val closestPoint = getClosestPointOnPath(event.x, event.y)
                userPath.lineTo(closestPoint.x, closestPoint.y)  // Kết thúc vẽ đường dính vào Path
                checkIfPathCompleted(closestPoint.x, closestPoint.y)  // Kiểm tra nếu đã nối thành công
            }
        }
        return true
    }

    // Tính toán điểm gần nhất trên Path với tọa độ ngón tay của người dùng
    private fun getClosestPointOnPath(x: Float, y: Float): PointF {
        val pathLength = pathMeasure.length
        val pointOnPath = FloatArray(2)
        val closestPoint = PointF()

        var minDistance = Float.MAX_VALUE

        // Lấy mẫu các điểm trên Path để tìm ra điểm gần nhất với vị trí ngón tay
        for (i in 0..1000) {  // Tăng số mẫu nếu cần độ chính xác cao hơn
            val distance = pathLength * i / 1000
            pathMeasure.getPosTan(distance, pointOnPath, null)

            val dx = x - pointOnPath[0]
            val dy = y - pointOnPath[1]
            val dist = dx * dx + dy * dy

            if (dist < minDistance) {
                minDistance = dist
                closestPoint.set(pointOnPath[0], pointOnPath[1])
            }
        }

        return closestPoint
    }

    // Kiểm tra nếu người dùng nối đúng 2 đầu của path
    private fun checkIfPathCompleted(x: Float, y: Float) {
        // Kiểm tra xem người dùng đã nối thành công chưa
        val pathEnd = FloatArray(2)
        pathMeasure.getPosTan(pathMeasure.length, pathEnd, null)

        if (distance(x, y, pathEnd[0], pathEnd[1]) < 30f) {  // Ngưỡng để xác định điểm cuối
            isCompleted = true  // Nối thành công
            paint.color = Color.GREEN  // Đổi màu thành xanh lục khi nối đúng
            invalidate()
        }
    }

    // Tính khoảng cách giữa 2 điểm
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.sqrt(((x2 - x1).toDouble().pow(2.0) + (y2 - y1).toDouble().pow(2.0))).toFloat()
    }

    // Hàm để parse pathData từ chuỗi JSON
    private fun parsePathData(pathData: String): Path {
        val path = Path()
        val extractedData = UtilsPainting.extractPathData(pathData)
        PathParser.createPathFromPathData(extractedData)?.let {
            path.addPath(it)
        }
        return path
    }
}
