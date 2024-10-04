package com.example.drawalinegame

import com.google.gson.annotations.SerializedName

class ShapeModel {
    @SerializedName("hinhVuong")
    val hinhVuong: SquareData? = null

    @SerializedName("line")
    val line: LineData? = null

    // Thêm các loại hình khác nếu cần
    // @SerializedName("circle")
    // val circle: CircleData? = null

    class SquareData {
        @SerializedName("left")
        val left: String = ""
        @SerializedName("bottom")
        val bottom: String = ""
        @SerializedName("right")
        val right: String = ""
        @SerializedName("top")
        val top: String = ""
    }

    class LineData {
        @SerializedName("line")
        val line: String = ""
    }
}
