package com.cs407.caloriecam

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

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

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calorie_tracking)

        etFoodName = findViewById(R.id.etFoodName)
        etCalories = findViewById(R.id.etCalories)
        btnAddFood = findViewById(R.id.btnAddFood)
        listViewFoods = findViewById(R.id.listViewFoods)
        tvTotalCalories = findViewById(R.id.tvTotalCalories)
        doneLogging = findViewById(R.id.doneLoggingCheck)

        databaseReference = FirebaseDatabase.getInstance().reference.child("meals")

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

        listViewFoods.setOnItemClickListener { _, _, position, _ ->
            val selectedFood = foodList[position]
            val foodName = selectedFood.split(" - ")[0]

            databaseReference.orderByChild("food").equalTo(foodName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (mealSnapshot in snapshot.children) {
                            val photoUrl = mealSnapshot.child("photoUrl").value.toString()
                            showImageInDialog(foodName, photoUrl)
                            break
                        }
                    } else {
                        Toast.makeText(this@CalorieTracking, "No image found for $foodName", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CalorieTracking, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        doneLogging.setOnClickListener {
            val prefs = getSharedPreferences("MY_PREFS", MODE_PRIVATE)
            val cachedCals = prefs.getInt("total_calories", 0)
            prefs.edit().putInt("total_calories", totalCalories + cachedCals).apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showImageInDialog(foodName: String, photoUrl: String) {
        val dialogImageView = ImageView(this)

        Thread {
            try {
                val url = URL(photoUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream: InputStream = connection.inputStream
                val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

                runOnUiThread {
                    val alertDialog = android.app.AlertDialog.Builder(this)
                        .setTitle(foodName)
                        .setView(dialogImageView)
                        .setPositiveButton("OK", null)
                        .create()

                    dialogImageView.setImageBitmap(bitmap)
                    alertDialog.show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // Getter method to access foodList
    fun getFoodList(): List<String> = foodList
}
