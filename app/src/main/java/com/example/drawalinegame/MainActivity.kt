package com.example.drawalinegame

import android.os.Bundle
import android.util.Log
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
        shapeList = loadShapesFromAssets()



        // Hiển thị hình đầu tiên
        drawView.setShape(shapeList[currentShapeIndex], determineShapeType(currentShapeIndex))

        // Xử lý nút next
        findViewById<Button>(R.id.btnNext).setOnClickListener {
            currentShapeIndex = (currentShapeIndex + 1) % shapeList.size
            Log.d("MainActivity", "Current Shape Index: $currentShapeIndex")
            Log.d("MainActivity", "Shape Type: ${determineShapeType(currentShapeIndex)}")
            drawView.setShape(shapeList[currentShapeIndex], determineShapeType(currentShapeIndex))
        }

    }

    private fun loadShapesFromAssets(): Array<ShapeModel> {
        val inputStream = assets.open("tdata.json")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val json = StringBuilder()
        reader.forEachLine { json.append(it) }
        reader.close()

         val shapes = Gson().fromJson(json.toString(), Array<ShapeModel>::class.java)
        Log.d("MainActivity", "Loaded ${shapes.size} shapes") // Dòng log này
        return shapes

    }

    private fun determineShapeType(index: Int): String {
        val shape = shapeList[index]

        val shapeMap = mapOf(
            "hinhVuong" to shape.hinhVuong,
            // Bạn có thể thêm các loại hình khác ở đây
        )

        return shapeMap.entries.firstOrNull { it.value != null }?.key ?: "unknown" // Trả về tên hình đầu tiên không null hoặc "unknown"
    }

}
