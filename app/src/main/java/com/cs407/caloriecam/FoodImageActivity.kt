package com.cs407.caloriecam

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File

class FoodImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_image)

        val tvFoodName: TextView = findViewById(R.id.tvFoodName)
        val ivFoodImage: ImageView = findViewById(R.id.ivFoodImage)

        // Attempt to get the food name from Intent extras
        val foodName = intent.getStringExtra("FOOD_NAME")
        // If no name is provided, you can default to something else or just show "Unknown"
        tvFoodName.text = foodName ?: "Unknown Food"

        // Retrieve stored references from SharedPreferences
        val prefs = getSharedPreferences("MY_PREFS", MODE_PRIVATE)
        val imagePath = prefs.getString("latest_image_path", null) // if you stored a file path
        val imageUriStr = prefs.getString("latest_image_uri", null) // if you stored a URI string

        // You may have one or both. Decide which to use. For example:
        // If imagePath is available, use it; otherwise, if imageUriStr is available, use that.
        when {
            imagePath != null -> {
                // Load from file path
                val file = File(imagePath)
                if (file.exists()) {
                    Glide.with(this)
                        .load(file)
                        .into(ivFoodImage)
                } else {
                    Toast.makeText(this, "Image file not found at: $imagePath", Toast.LENGTH_SHORT).show()
                }
            }
            imageUriStr != null -> {
                // Load from URI
                val imageUri = Uri.parse(imageUriStr)
                Glide.with(this)
                    .load(imageUri)
                    .into(ivFoodImage)
            }
            else -> {
                // If neither a path nor URI is available, show a default or error
                Toast.makeText(this, "No image found to display", Toast.LENGTH_SHORT).show()
                // Optionally set a default image:
                ivFoodImage.setImageResource(R.drawable.food_icon_foreground)
            }
        }
    }
}
