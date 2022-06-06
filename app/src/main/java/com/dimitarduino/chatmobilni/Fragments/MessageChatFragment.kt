
package com.dimitarduino.chatmobilni.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.dimitarduino.chatmobilni.AdapterClasses.ChatsAdapter
import com.dimitarduino.chatmobilni.Enkripcija
import com.dimitarduino.chatmobilni.Izvestuvanja.*
import com.dimitarduino.chatmobilni.ModelClasses.Chat
import com.dimitarduino.chatmobilni.ModelClasses.Chatlist
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.dimitarduino.chatmobilni.R
import com.dimitarduino.chatmobilni.VisitUserActivity
import com.dimitarduino.chatmobilni.database.AppDatabase
import com.dimitarduino.chatmobilni.database.PorakaDb
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Response
import java.sql.Timestamp


class MessageChatFragment : Fragment() {
    var idNaDrugiot : String = ""
    var firebaseKorisnik : FirebaseUser? = null
    private var slikaUri : Uri? = null
    var chatsAdapter: ChatsAdapter? = null
    var porakiLista: List<Chat>? = null
    private var dbReference : DatabaseReference? = null
    private var storageReference : StorageReference? = null
    var notify = false
    var apiService : APIService? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    //deklariraj ui komponenti
    private lateinit var barLayout : AppBarLayout
    private lateinit var toolbarConv : Toolbar
    private lateinit var profilnaConv : CircleImageView
    private lateinit var korisnickoConv : TextView
    private lateinit var porakiListaRecycler : RecyclerView
    private lateinit var novaPorakaCelina : RelativeLayout
    private lateinit var prikaciFajlBtn : ImageView
    private lateinit var ispratiPorakaBtn : CircleImageView
    private lateinit var novaPorakaEdit : EditText
    private lateinit var progressBar : ProgressBar
    private lateinit var recyclerPoraki : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message_chat, container, false)
        // Inflate the layout for this fragment

        //definiraj ui komponenti
        ispratiPorakaBtn = view.findViewById(R.id.isprati_poraka_button)
        novaPorakaEdit = view.findViewById(R.id.novaporaka_edit)
        korisnickoConv = view.findViewById(R.id.korisnicko_conv)
        profilnaConv = view.findViewById(R.id.profilna_conv)
        prikaciFajlBtn = view.findViewById(R.id.prikaci_fajl_conv)
        progressBar = view.findViewById(R.id.progressbar)
        recyclerPoraki = view.findViewById(R.id.poraki_lista_recycler)

        kreirajViewPorakiChat("BfPeDdunoeV3hsZSz3EVvf2faLf1")

        return view
    }

    fun kreirajViewPorakiChat(idNaDrugiot: String) {
        Log.i("kreiram", "kreiram novo so id na drugiot $idNaDrugiot")
//        val view = layoutInflater.inflate(R.layout.fragment_message_chat, ViewGroup!!, false)

        firebaseAnalytics = Firebase.analytics

        apiService =
            Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

//        intent = intent
//        idNaDrugiot = intent.getStringExtra("idNaDrugiot").toString()

        firebaseKorisnik = FirebaseAuth.getInstance().currentUser

        firebaseAnalytics.logEvent("otvoril_chat") {
            param("otvoril", firebaseKorisnik!!.uid)
            param("otvoreno_so", idNaDrugiot)
        }

        recyclerPoraki.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        recyclerPoraki.layoutManager = linearLayoutManager

        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference
            .child("users").child(idNaDrugiot)
        //
        storageReference = FirebaseStorage.getInstance().reference.child("chat_images")

        progressBar.visibility = View.INVISIBLE

        //listeners
        //        --listener baza vlecenje na informacii za drugiot korisnik
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                Log.i("KONEKCIJA", "available")

                super.onAvailable(network)

                ispratiNeisprateniPoraki()
            }

            // Network capabilities have changed for the network
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            }

            // lost network connection
            override fun onLost(network: Network) {
                Log.i("KONEKCIJA", "lost")
                super.onLost(network)
            }
        }

//        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
//        connectivityManager.requestNetwork(networkRequest, networkCallback)


        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val korisnik : Users? = p0.getValue(Users::class.java)

                korisnickoConv.text = korisnik!!.getUsername().toString()
                Picasso.get().load(korisnik.getProfile()).into(profilnaConv)

                korisnickoConv.setOnClickListener {
                    otvoriProfilActivity(idNaDrugiot)
                }
                profilnaConv.setOnClickListener {
                    otvoriProfilActivity(idNaDrugiot)
                }

                popolniPoraki(firebaseKorisnik!!.uid, idNaDrugiot, korisnik.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        //--attach na file
        prikaciFajlBtn.setOnClickListener {
            notify = true
            pickImage()
        }

        //--prakjanje na poraka
        ispratiPorakaBtn.setOnClickListener {
            notify = true
            val poraka = novaPorakaEdit.text.toString()
            Log.i("PORAKA", poraka)

            if (poraka != "") {
                ispratiPorakaDoKorisnik(firebaseKorisnik!!.uid, idNaDrugiot, poraka)
            }

            novaPorakaEdit.setText("")
        }

        seenPoraka(idNaDrugiot)
    }

    private fun getSystemService(java: Class<ConnectivityManager>) {

    }


    private fun ispratiNeisprateniPoraki() {
        val roomDb = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "chatx"
        ).allowMainThreadQueries().build()

        var k = roomDb.porakaDao().najdiPorakiChat(idNaDrugiot, FirebaseAuth.getInstance().currentUser!!.uid)

        for (porakaLokalno in k) {
            Log.i("PORAKA_LOKALNO", porakaLokalno.isprateno.toString())
            if (porakaLokalno.isprateno == false) {
                //hashmap prateno = true
                Log.i("PORAKA_LOKALNO", porakaLokalno.poraka.toString())

                val neispratenaPorakaRef = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("chats").child(porakaLokalno.porakaId)
                val porakaIsprateno = HashMap<String, Boolean>()
                porakaIsprateno["prateno"] = true
                neispratenaPorakaRef.setValue(porakaIsprateno)
            }
        }
    }

    private fun dodajVoLokalna(poraka : Chat, isprateno: Boolean) {
        val roomDb = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "chatx"
        ).allowMainThreadQueries().build()

        Log.i("PORAKADB_NAJDEN1", poraka.getPoraka().toString())
        Log.i("PORAKADB_NAJDEN1", poraka.getPorakaId().toString())

        if (poraka.getPorakaId() != null) {
            var k = roomDb.porakaDao().getAll()
            Log.i("PORAKADB_NAJDEN22", k.toString())
//            for (porakaLokalno in k) {
            Log.i("PORAKADB_NAJDEN", "vrtam")
            var najden = roomDb.porakaDao().searchFor(poraka.getPorakaId().toString())
            Log.i("PORAKADB_NAJDEN", najden.toString())
            Log.i("PORAKADB_NAJDEN", isprateno.toString())
            if (najden.size == 0) {
                poraka.getPorakaId()?.let {
                    roomDb.porakaDao().insertAll(PorakaDb(it, poraka.getIsprakjac(), poraka.getPoraka(), poraka.getPrimac(), poraka.getSeen(), poraka.getUrl(), isprateno))
                }
            } else {
                Log.i("PORAKADB", "vekje postoi")
                var d = roomDb.porakaDao().namestiIsprateno(PorakaDb(poraka.getPorakaId()!!, poraka.getIsprakjac(), poraka.getPoraka(), poraka.getPrimac(), poraka.getSeen(), poraka.getUrl(), isprateno))
                Log.i("PORAKADB", "smenav")
                Log.i("SMENATO_S", d.toString())
            }
//            }

            if (k.size == 0) {
                poraka.getPorakaId()?.let {
                    roomDb.porakaDao().insertAll(PorakaDb(it, poraka.getIsprakjac(), poraka.getPoraka(), poraka.getPrimac(), poraka.getSeen(), poraka.getUrl(), false))
                }
            }
        }

    }

    private fun otvoriProfilActivity(idNaDrugiot: String) {
        val intent = Intent(context, VisitUserActivity::class.java)
        intent.putExtra("profilZaOtvoranje", idNaDrugiot)
        startActivity(intent)
    }


    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    private fun popolniPoraki(isprakjacId: String, primacId: String?, primacSlikaUrl: String?) {
        Log.i("PORAKA_LOCAL", "polnam")
        porakiLista = ArrayList()
        val reference = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                (porakiLista as ArrayList<Chat>).clear()
                for (snapshot in p0.children)
                {
                    val poraka = snapshot.getValue(Chat::class.java)

                    Log.i("DATA", poraka.toString())

                    if ((poraka!!.getPrimac().equals(isprakjacId) && poraka.getIsprakjac().equals(primacId))
                        || poraka.getPrimac().equals(primacId) && poraka.getIsprakjac().equals(isprakjacId))
                    {

                        if (poraka.getPrateno() == false) {
                            val porakaIsprateno = HashMap<String, Boolean>()
                            porakaIsprateno["prateno"] = true

                            val neispratenaPorakaRef = poraka!!.getPorakaId()?.let {
                                FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("chats").child(it).setValue(porakaIsprateno)

                            }
                        }
                        (porakiLista as ArrayList<Chat>).add(poraka)

                        dodajVoLokalna(poraka, poraka.getPrateno())

                    }
                    chatsAdapter = ChatsAdapter(context!!, (porakiLista as ArrayList<Chat>), primacSlikaUrl!!, isOnline(requireContext()))
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

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            slikaUri = data!!.data

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

                    dodajVoLokalna(Chat(porakaSlikaHash["isprakjac"].toString(), porakaSlikaHash["poraka"].toString(), porakaSlikaHash["primac"].toString(), false, porakaSlikaHash["url"].toString(), porakaSlikaHash["porakaId"].toString()), false)

                    Log.i("slika", url)

                    ref.child("chats").child(porakaId!!).setValue(porakaSlikaHash).addOnCompleteListener {task ->
                        if (task.isSuccessful) {
                            dodajVoLokalna(Chat(porakaSlikaHash["isprakjac"].toString(), porakaSlikaHash["poraka"].toString(), porakaSlikaHash["primac"].toString(), false, porakaSlikaHash["url"].toString(), porakaSlikaHash["porakaId"].toString()), true)

                            progressBar.visibility = View.INVISIBLE

                            firebaseAnalytics.logEvent("pratil_slika") {
                                param("pratil", FirebaseAuth.getInstance().currentUser!!.uid)
                                param("pratil_na", idNaDrugiot)
                            }

                            //push notifikacii
                            val reference = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseKorisnik!!.uid)

                            reference.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(p0: DataSnapshot) {
                                    val korisnik = p0.getValue(Users::class.java)

                                    if (notify) {
                                        ispratiNotifikacija(idNaDrugiot, korisnik!!.getUsername(), "sent you an image.")
                                    }
                                    notify = false
                                }

                                override fun onCancelled(p0: DatabaseError) {

                                }

                            })
                        }
                    }

                }
            }
        }
    }

    private fun ispratiPorakaDoKorisnik(najavenKorisnik: String, idNaDrugiot: String, poraka: String) {
        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference
        val porakaKey = ref.push().key

        val porakaHashMap = HashMap<String, Any?>()
        var enkripcija : Enkripcija
        enkripcija = Enkripcija()
        val enkriptiranaPoraka = enkripcija.encrypt(poraka).toString()

        porakaHashMap["isprakjac"] = najavenKorisnik
        porakaHashMap["primac"] = idNaDrugiot
        porakaHashMap["poraka"] = enkriptiranaPoraka
        porakaHashMap["seen"] = false
        porakaHashMap["url"] = ""
        porakaHashMap["porakaId"] = porakaKey
        porakaHashMap["prateno"] = true

        if (isOnline(requireContext())) {
            porakaHashMap["prateno"] = true
        } else {
            porakaHashMap["prateno"] = false
        }

        dodajVoLokalna(Chat(porakaHashMap["isprakjac"].toString(), porakaHashMap["poraka"].toString(), porakaHashMap["primac"].toString(), false, porakaHashMap["url"].toString(), porakaHashMap["porakaId"].toString()), false)


        ref.child("chats").child(porakaKey!!).setValue(porakaHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    dodajVoLokalna(Chat(porakaHashMap["isprakjac"].toString(), porakaHashMap["poraka"].toString(), porakaHashMap["primac"].toString(), false, porakaHashMap["url"].toString(), porakaHashMap["porakaId"].toString()), true)

                    firebaseAnalytics.logEvent("pratil_tekst") {
                        param("pratil", FirebaseAuth.getInstance().currentUser!!.uid)
                        param("pratil_na", idNaDrugiot)
                    }

                    val chatsListReference = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/")
                        .reference
                        .child("chatList")
                        .child(firebaseKorisnik!!.uid)
                        .child(idNaDrugiot)

                    val momTimestamp = Timestamp(System.currentTimeMillis()).time

                    val chatListTvoj = Chatlist(idNaDrugiot, momTimestamp)
                    val chatListNadrug = Chatlist(firebaseKorisnik!!.uid, momTimestamp)

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists())
                            {
                                chatsListReference.setValue(chatListTvoj)
                            } else {
                                chatsListReference.child("timestamp").setValue(momTimestamp)
                            }

                            val chatlistaNaDrugiotRef = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/")
                                .reference
                                .child("chatList")
                                .child(idNaDrugiot)
                                .child(firebaseKorisnik!!.uid)

                            chatlistaNaDrugiotRef.setValue(chatListNadrug)
                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })
                }
            }

        //push notifikacii
        val reference = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseKorisnik!!.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val korisnik = p0.getValue(Users::class.java)

                if (notify) {
                    Log.i("notifikacii", "kje prakjam 1")
                    ispratiNotifikacija(idNaDrugiot, korisnik!!.getUsername(), poraka)
                }
                notify = false
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun ispratiNotifikacija(idNaDrugiot: String, username: String?, poraka: String) {
        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("tokens")
        Log.i("notifikacii", "kje prakam 2")

        val query = ref.orderByKey().equalTo(idNaDrugiot)

        query.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    val data = Data(
                        firebaseKorisnik!!.uid,
                        R.mipmap.ic_launcher,
                        "$username: $poraka",
                        "New Message",
                        idNaDrugiot
                    )

                    val isprakjac = Isprakjac(data!!, token!!.getToken().toString())

                    apiService!!.sendNotification(isprakjac)
                        .enqueue(object : retrofit2.Callback<IResponse>
                        {
                            override fun onResponse(
                                call: Call<IResponse>,
                                response: Response<IResponse>
                            )
                            {
                                if (response.code() == 200)
                                {
                                    Log.i("notifikacii", response.body().toString())
                                    if (response.body()!!.success !== 1)
                                    {
                                        Toast.makeText(requireContext(), getString(R.string.greskaProbaj), Toast.LENGTH_LONG).show()
                                    } else {
//                                        Toast.makeText(this@MessageChatActivity, "Sent.", Toast.LENGTH_LONG).show()

                                    }
                                }
                            }

                            override fun onFailure(call: Call<IResponse>, t: Throwable) {

                            }
                        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private var seenListener: ValueEventListener? = null

    private fun seenPoraka(korisnikId: String)
    {
        Log.i("seen ke napram", "seen")
        val reference = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("chats")

        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val poraka = dataSnapshot.getValue(Chat::class.java)

                    if (poraka!!.getPrimac().equals(firebaseKorisnik!!.uid) && poraka.getIsprakjac().equals(korisnikId))
                    {
                        val hashMap = HashMap<String, Any>()
                        hashMap["seen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}
