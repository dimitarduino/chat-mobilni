package com.dimitarduino.chatmobilni

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class WelcomeActivity : AppCompatActivity() {
    var firebaseUser : FirebaseUser? = null
    private lateinit var registerWelcomeBtn : Button
    private lateinit var loginWelcomeBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        registerWelcomeBtn = findViewById<Button>(R.id.register_welcome_btn)
        loginWelcomeBtn = findViewById<Button>(R.id.login_welcome_btn)

        registerWelcomeBtn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)

            startActivity(intent)
            finish()
        }
        loginWelcomeBtn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)
            finish()
        }
    }
}