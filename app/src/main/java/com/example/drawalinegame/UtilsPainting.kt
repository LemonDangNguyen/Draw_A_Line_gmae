package com.example.drawalinegame

import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.PathParser

object UtilsPainting {

    // Hàm trích xuất pathData từ chuỗi
    internal fun extractPathData(input: String): String {
        val regex = """android:pathData="([^"]+)"""".toRegex()
        val matchResult = regex.find(input)
        return matchResult?.groupValues?.get(1) ?: ""
    }

    // Hàm trích xuất fillColor từ chuỗi
    fun extractFillColor(input: String): String {
        val regex = """android:fillColor="([^"]+)"""".toRegex()
        val matchResult = regex.find(input)
        return matchResult?.groupValues?.get(1) ?: "#FFD9D9D9" // Màu mặc định nếu không tìm thấy
    }

    // Hàm lấy kích thước thực tế của Path
    fun getPathBounds(pathData: String): RectF {
        val path = PathParser.createPathFromPathData(extractPathData(pathData))
        val bounds = RectF()
        path.computeBounds(bounds, true)
        return bounds
    }
}
