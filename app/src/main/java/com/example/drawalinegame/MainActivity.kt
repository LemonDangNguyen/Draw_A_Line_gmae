package com.example.drawalinegame

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var drawView: DrawView
    private var currentShapeIndex = 0
    private lateinit var shapeList: Array<ShapeModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawView = findViewById(R.id.drawView)

        // Tải JSON từ file assets
        shapeList = loadShapesFromAssets()

        // Hiển thị hình đầu tiên (line)
        drawView.setShape(shapeList[currentShapeIndex])

        // Xử lý nút next
        findViewById<Button>(R.id.btnNext).setOnClickListener {
            currentShapeIndex = (currentShapeIndex + 1) % shapeList.size
            drawView.setShape(shapeList[currentShapeIndex])  // Hiển thị hình tiếp theo
        }

        // Nút delete chưa gán chức năng
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            // Chưa gán chức năng
        }
    }

    private fun loadShapesFromAssets(): Array<ShapeModel> {
        val inputStream = assets.open("tdata.json")  // File JSON của bạn trong assets
        val reader = BufferedReader(InputStreamReader(inputStream))
        val json = StringBuilder()
        reader.forEachLine { json.append(it) }
        reader.close()

        return Gson().fromJson(json.toString(), Array<ShapeModel>::class.java)
    }
}
