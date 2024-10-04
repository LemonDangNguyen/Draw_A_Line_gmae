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
    class LineData {
        @SerializedName("line")
        val line: String = ""
    }
}
