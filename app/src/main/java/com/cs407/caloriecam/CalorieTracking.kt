package com.example.calorietracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs407.caloriecam.R

class MainActivity : AppCompatActivity() {

    private lateinit var etFoodName: EditText
    private lateinit var etCalories: EditText
    private lateinit var btnAddFood: Button
    private lateinit var listViewFoods: ListView
    private lateinit var tvTotalCalories: TextView

    private val foodList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private var totalCalories = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calorie_tracking)

        etFoodName = findViewById(R.id.etFoodName)
        etCalories = findViewById(R.id.etCalories)
        btnAddFood = findViewById(R.id.btnAddFood)
        listViewFoods = findViewById(R.id.listViewFoods)
        tvTotalCalories = findViewById(R.id.tvTotalCalories)

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
    }
}
