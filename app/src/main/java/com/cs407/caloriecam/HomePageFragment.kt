package com.cs407.caloriecam

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class HomePageFragment : Fragment(R.layout.calorie_progress) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize views here
        val caloriesTextView: TextView = view.findViewById(R.id.CalorieAmount)
        val caloriesProgressView: ProgressBar = view.findViewById(R.id.progressBar)
        caloriesTextView.text = "VAR/2000"//TODO FIX THIS
        caloriesProgressView.progress = 22 //TODO ALSO THIS, NEED GLOBAL VAR FOR CAL COUN
        caloriesTextView.setOnClickListener{
            findNavController().navigate(R.id.action_HomePageFragment_to_MealLoggingFragment)
        }

    }
}
