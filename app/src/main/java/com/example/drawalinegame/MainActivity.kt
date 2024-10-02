package com.example.drawalinegame

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo Custom View
        val squarePathView = SquarePathView(this, null)
        setContentView(squarePathView)

        // Đọc dữ liệu từ file JSON trong thư mục assets
        val jsonString = loadJSONFromAsset("tdata.json")

        // Chuyển JSON thành ShapeModel
        val shapeModel = Gson().fromJson(jsonString, Array<ShapeModel>::class.java)

        // Thiết lập dữ liệu hình vuông
        shapeModel?.firstOrNull()?.hinhVuong?.let { squarePathView.setSquareData(it) }

       // val  clear = findViewById<Button>(R.id.xoa)
//        clear.setOnClickListener {
//            // Code to execute when the button is clicked
//            // For example, to clear the SquarePathView:
//            squarePathView.clearUserPath()
//        }
    }

    // Hàm đọc file JSON từ thư mục assets
    private fun loadJSONFromAsset(fileName: String): String? {
        return try {
            val inputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

}
