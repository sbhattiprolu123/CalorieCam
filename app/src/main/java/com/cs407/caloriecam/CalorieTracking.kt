package com.cs407.caloriecam

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs407.caloriecam.R

class CalorieTracking : AppCompatActivity() {

    private lateinit var etFoodName: EditText
    private lateinit var etCalories: EditText
    private lateinit var btnAddFood: Button
    private lateinit var listViewFoods: ListView
    private lateinit var tvTotalCalories: TextView
    private lateinit var doneLogging: ImageView

    private val foodList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private var totalCalories = 0

    private val foodImages: Map<String, Int> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calorie_tracking)

        etFoodName = findViewById(R.id.etFoodName)
        etCalories = findViewById(R.id.etCalories)
        btnAddFood = findViewById(R.id.btnAddFood)
        listViewFoods = findViewById(R.id.listViewFoods)
        tvTotalCalories = findViewById(R.id.tvTotalCalories)
        doneLogging = findViewById(R.id.doneLoggingCheck)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, foodList)
        listViewFoods.adapter = adapter

        tvTotalCalories.text = "Total Calories: $totalCalories"

        btnAddFood.setOnClickListener {
            val foodName = etFoodName.text.toString()
            val caloriesStr = etCalories.text.toString()

            if (foodName.isEmpty() || caloriesStr.isEmpty()) {
                Toast.makeText(this, "Please enter both food name and calories", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val calories = caloriesStr.toInt()

                foodList.add("$foodName - $calories kcal")
                totalCalories += calories

                adapter.notifyDataSetChanged()
                tvTotalCalories.text = "Total Calories: $totalCalories"

                etFoodName.text.clear()
                etCalories.text.clear()

            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid number for calories", Toast.LENGTH_SHORT).show()
            }
        }

        listViewFoods.setOnItemClickListener { parent, view, position, id ->
            val selectedFood = foodList[position]
            val foodName = selectedFood.split(" - ")[0]
            val imageResourceId = foodImages[foodName]

            if (imageResourceId != null) {
                val intent = Intent(this, FoodImageActivity::class.java)
                intent.putExtra("FOOD_NAME", foodName)
                intent.putExtra("IMAGE_RESOURCE_ID", imageResourceId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Image not available for $foodName", Toast.LENGTH_SHORT).show()
            }
        }
        doneLogging.setOnClickListener(){
            val prefs = getSharedPreferences("MY_PREFS", MODE_PRIVATE)
            val cachedCals = prefs.getInt("total_calories", 0)
            prefs.edit()
                .putInt("total_calories", totalCalories+cachedCals)
                .apply()


            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
