package com.dimitarduino.chatmobilni

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dimitarduino.chatmobilni.AdapterClasses.ChatsAdapter
import com.dimitarduino.chatmobilni.ModelClasses.Chat
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.StringBuilder

class MessageChatActivity : AppCompatActivity() {
    var idNaDrugiot : String = ""
    var firebaseKorisnik : FirebaseUser? = null
    private var slikaUri : Uri? = null
    var chatsAdapter: ChatsAdapter? = null
    var porakiLista: List<Chat>? = null
    private var dbReference : DatabaseReference? = null
    private var storageReference : StorageReference? = null

    //deklariraj ui komponenti
    private lateinit var barLayout : AppBarLayout
    private lateinit var toolbarConv : Toolbar
    private lateinit var profilnaConv : CircleImageView
    private lateinit var korisnickoConv : TextView
    private lateinit var porakiListaRecycler : RecyclerView
    private lateinit var novaPorakaCelina : RelativeLayout
    private lateinit var prikaciFajlBtn : ImageView
    private lateinit var ispratiPorakaBtn : ImageView
    private lateinit var novaPorakaEdit : EditText
    private lateinit var progressBar : ProgressBar
    private lateinit var recyclerPoraki : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)
        intent = intent
        idNaDrugiot = intent.getStringExtra("idNaDrugiot").toString()
        firebaseKorisnik = FirebaseAuth.getInstance().currentUser

        recyclerPoraki = findViewById(R.id.poraki_lista_recycler)
        recyclerPoraki.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerPoraki.layoutManager = linearLayoutManager

        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference
            .child("users").child(idNaDrugiot)

        //
        storageReference = FirebaseStorage.getInstance().reference.child("chat_images")
        //definiraj ui komponenti
        ispratiPorakaBtn = findViewById(R.id.isprati_poraka_button)
        novaPorakaEdit = findViewById(R.id.novaporaka_edit)
        korisnickoConv = findViewById(R.id.korisnicko_conv)
        profilnaConv = findViewById(R.id.profilna_conv)
        prikaciFajlBtn = findViewById(R.id.prikaci_fajl_conv)
        progressBar = findViewById(R.id.progressbar)

        progressBar.visibility = View.INVISIBLE

        //listeners
//        --listener baza vlecenje na informacii za drugiot korisnik
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val korisnik : Users? = p0.getValue(Users::class.java)

                korisnickoConv.text = korisnik!!.getUsername().toString()
                Picasso.get().load(korisnik.getProfile()).into(profilnaConv)

                popolniPoraki(firebaseKorisnik!!.uid, idNaDrugiot, korisnik.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        //--attach na file
        prikaciFajlBtn.setOnClickListener {
            pickImage()
        }

        //--prakjanje na poraka
        ispratiPorakaBtn.setOnClickListener {
            val poraka = novaPorakaEdit.text.toString()
            Log.i("PORAKA", poraka)

            if (poraka != "") {
                ispratiPorakaDoKorisnik(firebaseKorisnik!!.uid, idNaDrugiot, poraka)
            }
        }
    }

    private fun popolniPoraki(isprakjacId: String, primacId: String?, primacSlikaUrl: String?) {
        porakiLista = ArrayList()
        val reference = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                (porakiLista as ArrayList<Chat>).clear()
                for (snapshot in p0.children)
                {
                    Log.i("DATA", snapshot.value.toString())
                    val poraka = snapshot.getValue(Chat::class.java)
                    Log.i("DATA", poraka.toString())

                    if (poraka!!.getIsprakjac().equals(isprakjacId) && poraka.getIsprakjac().equals(primacId)
                        || poraka.getPrimac().equals(primacId) && poraka.getIsprakjac().equals(isprakjacId))
                    {
                        (porakiLista as ArrayList<Chat>).add(poraka)
                    }
                    chatsAdapter = ChatsAdapter(this@MessageChatActivity, (porakiLista as ArrayList<Chat>), primacSlikaUrl!!)
                    recyclerPoraki.adapter = chatsAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            slikaUri = data!!.data

            Log.i("slika", slikaUri.toString())
//            Toast.makeText(this@MessageChatActivity, "Uploading...", Toast.LENGTH_LONG).show()

            prikaciSlikaBaza()
        }
    }

    private fun prikaciSlikaBaza() {
        progressBar.visibility = View.VISIBLE

        if (slikaUri != null) {
            val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference
            val porakaId = ref.push().key
            val fileRef = storageReference!!.child(porakaId.toString() + ".jpg")

            val uploadTask = fileRef.putFile(slikaUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val porakaSlikaHash = HashMap<String, Any?>()
                    porakaSlikaHash["isprakjac"] = firebaseKorisnik!!.uid
                    porakaSlikaHash["primac"] = idNaDrugiot
                    porakaSlikaHash["poraka"] = "sent you an image."
                    porakaSlikaHash["seen"] = false
                    porakaSlikaHash["url"] = url
                    porakaSlikaHash["porakaId"] = porakaId

                    Log.i("slika", url)

                    ref.child("chats").child(porakaId!!).setValue(porakaSlikaHash).addOnCompleteListener {task ->

                    }

                    progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun ispratiPorakaDoKorisnik(najavenKorisnik: String, idNaDrugiot: String, poraka: String) {
        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference
        val porakaKey = ref.push().key

        val porakaHashMap = HashMap<String, Any?>()

        porakaHashMap["isprakjac"] = najavenKorisnik
        porakaHashMap["primac"] = idNaDrugiot
        porakaHashMap["poraka"] = poraka
        porakaHashMap["seen"] = false
        porakaHashMap["url"] = ""
        porakaHashMap["porakaId"] = porakaKey

        ref.child("chats").child(porakaKey!!).setValue(porakaHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    popolniPoraki(firebaseKorisnik!!.uid, idNaDrugiot, "")
                }
        }
    }
}