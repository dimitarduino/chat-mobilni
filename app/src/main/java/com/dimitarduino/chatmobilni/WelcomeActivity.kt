package com.dimitarduino.chatmobilni

import android.content.ContentProviderClient
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.HashMap

class WelcomeActivity : AppCompatActivity() {
    var firebaseUser : FirebaseUser? = null
    private lateinit var registerWelcomeBtn : Button
    private lateinit var loginWelcomeBtn : Button
    private lateinit var googleSignInBtn : Button

    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers : DatabaseReference
    companion object {
        const val RC_SIGN_IN = 1001
        const val EXTRA_NAME = "EXTRA_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        mAuth = FirebaseAuth.getInstance()

        //definiraj ui komponenti
        registerWelcomeBtn = findViewById<Button>(R.id.register_welcome_btn)
        loginWelcomeBtn = findViewById<Button>(R.id.login_welcome_btn)
        googleSignInBtn = findViewById<Button>(R.id.googleSignIn)


        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope(Scopes.PLUS_ME))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInBtn.setOnClickListener {
            najaviSeSoGoogle()
        }


        registerWelcomeBtn.setOnClickListener{
            //smeni view vo registracija
            val intent = Intent(this, RegisterActivity::class.java)

            startActivity(intent)
            finish()
        }
        loginWelcomeBtn.setOnClickListener{
            //smeni view vo registracija
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)
            finish()
        }
    }

    private fun najaviSeSoGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("GOOGLE_SIGNIN", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("GOOGLE_SIGNIN", "signInWithCredential:success")
                    val user = mAuth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("GOOGLE_SIGNIN", "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val momentalenKorisnik = mAuth.currentUser!!.uid

            refUsers = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference
                .child("users")
                .child(momentalenKorisnik)

            val userHashMap = HashMap<String, Any>()
            userHashMap["uid"] = momentalenKorisnik

            userHashMap["status"] = "online"
            userHashMap["username"] = user.email.toString()
            userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/chatmobilni.appspot.com/o/cover.jpg?alt=media&token=590ea2bd-1f43-40af-9f85-db1d44f3623f"
            userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chatmobilni.appspot.com/o/m1.jpg?alt=media&token=14b1ea15-8f83-46ff-8edb-9e25b3060a38"
            userHashMap["search"] = user.email.toString().lowercase(Locale.getDefault())
            userHashMap["facebook"] = "https://facebook.com"
            userHashMap["instagram"] = "https://instagram.com"
            userHashMap["website"] = "https://google.com"
            userHashMap["fullname"] = user.displayName.toString()
            userHashMap["gender"] = "Male"

            //proveri prvo dali postoi
            refUsers.updateChildren(userHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            // ako vekje e najaven odi vo MainActivity direktno
            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)
            finish()
        }
    }
}