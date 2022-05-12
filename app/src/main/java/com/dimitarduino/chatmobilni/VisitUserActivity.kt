package com.dimitarduino.chatmobilni

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class VisitUserActivity : AppCompatActivity() {
    private var profilOtvori: String = ""
    var korisnik: Users? = null

    //deklariraj ui komponenti
    private lateinit var korisnickoPrikaz : TextView
    private lateinit var profilnaPrikaz : CircleImageView
    private lateinit var coverPrikaz : ImageView
    private lateinit var fbPrikaz : ImageView
    private lateinit var instaPrikaz : ImageView
    private lateinit var webPrikaz : ImageView
    private lateinit var ispratiPorakaProfil : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user)
        //definiraj ui komponenti
        korisnickoPrikaz = findViewById<TextView>(R.id.profil_korisnicko)
        profilnaPrikaz = findViewById<CircleImageView>(R.id.profil_profilna)
        coverPrikaz = findViewById<ImageView>(R.id.profil_naslovna)
        fbPrikaz = findViewById<ImageView>(R.id.profil_fb)
        instaPrikaz = findViewById<ImageView>(R.id.profil_insta)
        webPrikaz = findViewById<ImageView>(R.id.profil_web)
        ispratiPorakaProfil = findViewById<Button>(R.id.ispratiPoraka_profil)

        profilOtvori = intent.getStringExtra("profilZaOtvoranje").toString()

        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").child(profilOtvori)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    korisnik = p0.getValue(Users::class.java)

//                    korisnikname_display.text = korisnik!!.getUserName()
//                    Picasso.get().load(korisnik!!.getProfile()).into(profile_display)
//                    Picasso.get().load(korisnik!!.getCover()).into(cover_display)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


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