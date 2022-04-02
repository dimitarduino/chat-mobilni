package com.dimitarduino.chatmobilni

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatContent : AppCompatActivity() {
    private lateinit var imeTextView : TextView
    private lateinit var profilnaElement : CircleImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_content)
        imeTextView = findViewById(R.id.username_text_conv)
        profilnaElement = findViewById(R.id.profile_image_conv)

        val imeNaDrugiot = intent.getStringExtra("username")
        val slikaNaDrugiot = intent.getStringExtra("profile")

        imeTextView.text = imeNaDrugiot
        Picasso.get().load(slikaNaDrugiot).into(profilnaElement)

    }
}