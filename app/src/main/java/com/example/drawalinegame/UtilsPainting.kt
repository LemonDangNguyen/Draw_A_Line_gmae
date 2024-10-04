package com.example.drawalinegame

import android.graphics.Color
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import androidx.core.graphics.PathParser

object UtilsPainting {

    // Hàm trích xuất pathData từ chuỗi
    internal fun extractPathData(input: String): String {
        val regex = """android:pathData="([^"]+)"""".toRegex()
        val matchResult = regex.find(input)
        return matchResult?.groupValues?.get(1) ?: ""
    }

    // Hàm trích xuất màu từ chuỗi pathData
    fun extractColor(pathData: String): Int {
        // Biểu thức chính quy được cải tiến để bỏ qua các khoảng trắng trước "android:fillColor"
        val colorRegex = """\s*android:fillColor="([^"]+)"""".toRegex()
        val matchResult = colorRegex.find(pathData)

        return if (matchResult != null) {
            Color.parseColor(matchResult.groupValues[1])  // Trích xuất và chuyển chuỗi màu thành int
        } else {
            Color.BLACK  // Mặc định là màu đen nếu không tìm thấy màu
        }
    }


    // Hàm lấy kích thước thực tế của Path
    fun getPathBounds(pathData: String): RectF {
        val path = PathParser.createPathFromPathData(extractPathData(pathData))
        val bounds = RectF()
        path.computeBounds(bounds, true)
        return bounds
    }
    // Hàm lấy điểm bắt đầu của Path
    fun getStartPoint(pathData: String): PointF {
        val path = PathParser.createPathFromPathData(extractPathData(pathData))
        val pathMeasure = android.graphics.PathMeasure(path, false)
        val coords = FloatArray(2)

        // Lấy tọa độ của điểm đầu tiên
        pathMeasure.getPosTan(0f, coords, null)
        return PointF(coords[0], coords[1])
    }

    // Hàm lấy điểm kết thúc của Path
    fun getEndPoint(pathData: String): PointF {
        val path = PathParser.createPathFromPathData(extractPathData(pathData))
        val pathMeasure = android.graphics.PathMeasure(path, false)
        val coords = FloatArray(2)

        // Lấy tọa độ của điểm cuối cùng
        pathMeasure.getPosTan(pathMeasure.length, coords, null)
        return PointF(coords[0], coords[1])
    }
}
