package com.cs407.caloriecam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MealLoggingHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_logging_host)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.meal_logging_fragment_container, MealLoggingFragment())
                .commitNow()
        }
    }
}
