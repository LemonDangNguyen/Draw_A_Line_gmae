package com.example.drawalinegame

import com.google.gson.annotations.SerializedName

class ShapeModel {
    @SerializedName("hinhVuong")
    val hinhVuong: SquareData? = null
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

    @SerializedName("line")
    val line: LineData? = null
    @SerializedName("tamGiac")
    val tamGiac: TriangleData? = null
    // Thêm các loại hình khác nếu cần
    // @SerializedName("circle")
    // val circle: CircleData? = null
    class LineData {
        @SerializedName("line")
        val line: String = ""
    }
    class TriangleData{
        @SerializedName("canh1")
        val canh1: String = ""
        @SerializedName("canh2")
        val canh2: String = ""
        @SerializedName("canh3")
        val canh3: String = ""
    }
}
