package com.dimitarduino.chatmobilni

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class VisitUserActivity : AppCompatActivity() {
    private var profilOtvori: String = ""
    var korisnik: Users? = null
    private lateinit var firebaseAnalytics : FirebaseAnalytics

    //deklariraj ui komponenti
    private lateinit var korisnickoPrikaz : TextView
    private lateinit var fullnamePrikaz : TextView
    private lateinit var profilnaPrikaz : CircleImageView
    private lateinit var coverPrikaz : ImageView
    private lateinit var fbPrikaz : LinearLayout
    private lateinit var instaPrikaz : LinearLayout
    private lateinit var webPrikaz : LinearLayout
    private lateinit var ispratiPorakaProfil : Button
    private lateinit var vratiNazadBtn : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user)
        firebaseAnalytics = Firebase.analytics
        //definiraj ui komponenti
        korisnickoPrikaz = findViewById<TextView>(R.id.profil_korisnicko)
        fullnamePrikaz = findViewById<TextView>(R.id.fullnameVisit)
        profilnaPrikaz = findViewById<CircleImageView>(R.id.profil_profilna)
        coverPrikaz = findViewById<ImageView>(R.id.profil_naslovna)
        fbPrikaz = findViewById<LinearLayout>(R.id.profil_fb)
        instaPrikaz = findViewById<LinearLayout>(R.id.profil_insta)
        webPrikaz = findViewById<LinearLayout>(R.id.profil_web)
        ispratiPorakaProfil = findViewById<Button>(R.id.ispratiPoraka_profil)
        vratiNazadBtn = findViewById(R.id.vratiNazad)

        profilOtvori = intent.getStringExtra("profilZaOtvoranje").toString()

        firebaseAnalytics.logEvent("otvoril_profil") {
            param("otvoril", FirebaseAuth.getInstance().currentUser!!.uid)
            param("otvoreno_so", profilOtvori)
        }

        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").child(profilOtvori)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    korisnik = p0.getValue(Users::class.java)

                    korisnickoPrikaz.text = "@" + korisnik!!.getUsername()
                    fullnamePrikaz.text = korisnik!!.getFullname()
                    Picasso.get().load(korisnik!!.getProfile()).into(profilnaPrikaz)
                    Picasso.get().load(korisnik!!.getCover()).into(coverPrikaz)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        vratiNazadBtn.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        fbPrikaz.setOnClickListener {
            val uri = Uri.parse(korisnik!!.getFacebook())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        instaPrikaz.setOnClickListener {
            val uri = Uri.parse(korisnik!!.getInstagram())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        webPrikaz.setOnClickListener {
            val uri = Uri.parse(korisnik!!.getWebsite())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        ispratiPorakaProfil.setOnClickListener {
            val intent = Intent(this, MessageChatActivity::class.java)
            intent.putExtra("idNaDrugiot", korisnik!!.getUID())
            startActivity(intent)
        }
    }

}