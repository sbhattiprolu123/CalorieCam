package com.cs407.caloriecam

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is authenticated
//        val auth = FirebaseAuth.getInstance()
//        if (auth.currentUser == null) {
//            // Redirect to LoginActivity if the user is not logged in
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//            return
//        }

        setContentView(R.layout.activity_main)

        // Load the MealLoggingFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MealLoggingFragment())
                .commit()
        }
    }
}
