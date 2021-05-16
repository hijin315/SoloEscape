package com.jinny.soloescape

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            // 로그인 안되어 있다면
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // 로그인 되어 있다면
            startActivity(Intent(this, LikeActivity::class.java))
            finish()
        }
    }
}