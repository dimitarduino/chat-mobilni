package com.dimitarduino.chatmobilni

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {
    //varijabli firebase
    private lateinit var mAuth : FirebaseAuth
    private lateinit var refUsers : DatabaseReference
    private var firebaseUserId : String = ""

    //varijabli ui komponenti
    private lateinit var registerBtn : Button
    private lateinit var registerEmailEdit : EditText
    private lateinit var registerUsernameEdit : EditText
    private lateinit var registerPasswordEdit : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //deklariraj elementi del
        registerBtn = findViewById<Button>(R.id.register_btn)
        registerEmailEdit = findViewById<EditText>(R.id.email_register)
        registerUsernameEdit = findViewById<EditText>(R.id.username_register)
        registerPasswordEdit = findViewById<EditText>(R.id.password_register)

        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)

        //title na top menu
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //back da vrakja na welcome
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)

            startActivity(intent)
            finish()
        }

        //vrzvanje so firebase
        mAuth = FirebaseAuth.getInstance()

        registerBtn.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username : String = registerUsernameEdit.text.toString()
        val password : String = registerPasswordEdit.text.toString()
        val email : String = registerEmailEdit.text.toString()

        val daliEValidno : Boolean = proveriValidnost(username, password, email)

        if (daliEValidno) {
            // validen korisnik i dodaj vo baza i vo firebase authentication

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                task ->
                if (task.isSuccessful) {
                    firebaseUserId = mAuth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseUserId)

                    val userHashMap = HashMap<String, Any>()
                    userHashMap["uid"] = firebaseUserId
                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chatmobilni.appspot.com/o/profile.png?alt=media&token=976fdb7d-0ed6-4720-bd5e-87cd8a974374"
                    userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/chatmobilni.appspot.com/o/cover.jpg?alt=media&token=495a2174-5d51-447f-9364-866ce6def7ef"
                    userHashMap["status"] = "offline"
                    userHashMap["username"] = username
                    userHashMap["search"] = username.lowercase(Locale.getDefault())
                    userHashMap["facebook"] = "https://facebook.com"
                    userHashMap["instagram"] = "https://instagram.com"
                    userHashMap["website"] = "https://google.com"

                    refUsers.updateChildren(userHashMap)
                        .addOnCompleteListener {task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                } else {
                    Toast.makeText(this, "Error: " + task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            //prikazi poraka deka se zadolzitelni polinjata
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_LONG).show()
        }
    }

    private fun proveriValidnost(username: String, password: String, email: String): Boolean {
        return !(username == "" || password == "" || email == "")
    }
}