package com.cs407.caloriecam

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FoodImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_image)

        val foodName = intent.getStringExtra("FOOD_NAME")
        val imageResourceId = intent.getIntExtra("IMAGE_RESOURCE_ID", -1)

        val tvFoodName: TextView = findViewById(R.id.tvFoodName)
        val ivFoodImage: ImageView = findViewById(R.id.ivFoodImage)

        if (foodName != null && imageResourceId != -1) {
            tvFoodName.text = foodName
            ivFoodImage.setImageResource(imageResourceId)
        } else {
            Toast.makeText(this, "Error loading food image", Toast.LENGTH_SHORT).show()
        }
    }
}
