package com.cs407.caloriecam

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlin.math.floor

class HomePageFragment : Fragment(R.layout.calorie_progress) {
    val totalCalories = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val caloriesTextView: TextView = view.findViewById(R.id.CalorieAmount)
        val caloriesProgressView: ProgressBar = view.findViewById(R.id.progressBar)
        val logButton: Button = view.findViewById(R.id.logButton)
        val resetButton: Button = view.findViewById(R.id.resetButton)

        val prefs = requireContext().getSharedPreferences("MY_PREFS", 0)
        val totalCalories = totalCalories + prefs.getInt("total_calories", 0)

        caloriesTextView.text = "$totalCalories/2000"
        caloriesProgressView.progress = (totalCalories*100)/2000
        logButton.setOnClickListener{
            findNavController().navigate(R.id.action_HomePageFragment_to_MealLoggingFragment)
        }
        resetButton.setOnClickListener{
            prefs.edit().putInt("total_calories",0)
                .apply()
            caloriesTextView.text = "0/2000"
            caloriesProgressView.progress=0
        }

    }
}
