package com.cs407.caloriecam

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val auth = FirebaseAuth.getInstance()
//        if (auth.currentUser == null) {
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//            return
//        }

        setContentView(R.layout.activity_main)
    }
}
