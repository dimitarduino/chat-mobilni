package com.dimitarduino.chatmobilni

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class LoginActivity : AppCompatActivity() {
    //varijabli firebase
    private lateinit var mAuth : FirebaseAuth
    private lateinit var refUsers : DatabaseReference
    private var firebaseUserId : String = ""

    //varijabli ui komponenti
    private lateinit var loginBtn : Button
    private lateinit var loginEmailEdit : EditText
    private lateinit var loginPasswordEdit : EditText
    private lateinit var vratiNazad : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //deklariraj lateinit varijabli
        loginBtn = findViewById<Button>(R.id.login_btn)
        loginEmailEdit = findViewById<EditText>(R.id.email_login)
        loginPasswordEdit = findViewById<EditText>(R.id.password_login)
        vratiNazad = findViewById(R.id.vratiNazad)

//        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_login)
//        setSupportActionBar(toolbar)
//
//        supportActionBar!!.title = "Login"
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //toolbar
        vratiNazad.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)

            startActivity(intent)
            finish()
        }

        //povrzvanje so firebase
        mAuth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val password : String = loginPasswordEdit.text.toString()
        val email : String = loginEmailEdit.text.toString()

        val daliEValidno : Boolean = proveriValidnost(email, password)

        if (daliEValidno) {
            //validen korisnik i probaj da se logirash
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error: " + task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            //prikazi poraka deka se zadolzitelni polinjata
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_LONG).show()
        }
    }

    private fun proveriValidnost(email: String, password: String): Boolean {
        return !(password == "" || email == "")
    }
}