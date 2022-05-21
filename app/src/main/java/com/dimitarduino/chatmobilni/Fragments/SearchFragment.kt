package com.dimitarduino.chatmobilni.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dimitarduino.chatmobilni.AdapterClasses.UserAdapter
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.dimitarduino.chatmobilni.ModelClasses.Visits
import com.dimitarduino.chatmobilni.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.protobuf.Value
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {
    private var userAdapter : UserAdapter? = null
    private var mUsers : List<Users>? = null
    private var najdeniBaranja : List<String>? = ArrayList()
    private var recyclerView : RecyclerView? = null
    private var firestoreDb : FirebaseFirestore? = null

    //deklariraj ui komponenti
    private lateinit var searchUsersEdit : EditText
    private lateinit var tipRezultati : TextView
    private lateinit var izbrisiBaranja : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //definiranje koj fragment da go koristi
        val view : View = inflater.inflate(R.layout.fragment_search, container, false)

        //definiraj ui komponenti
        searchUsersEdit = view.findViewById(R.id.searchUserEdit)
        tipRezultati = view.findViewById(R.id.tipRezultati)
        izbrisiBaranja = view.findViewById(R.id.izbrisiBaranja)
        recyclerView = view.findViewById(R.id.searchList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList()
        najdeniBaranja = ArrayList()
        retrieveAllUsers()

        //onchange listener na search edit text
        searchUsersEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchForUsers(p0.toString().lowercase(Locale.getDefault()))
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })


        //izbrisi baranja
        izbrisiBaranja.setOnClickListener {
            izbrisiIstorijaBaranja()
        }

        return view
    }

    private fun izbrisiIstorijaBaranja() {
        val firebaseKorisnik = FirebaseAuth.getInstance().currentUser!!.uid

        val firestoreVisits = firestoreDb!!.collection("visits")
        firestoreVisits.whereEqualTo("otvoreno_od", firebaseKorisnik).get()
            .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestoreDb!!.collection("visits").document(document.id).delete()
                    }

                retrieveAllUsers()
            }
    }

    fun retrieveAllUsers() {

        //zemi momentalen korisnik id
        var firebaseUserId  = FirebaseAuth.getInstance().currentUser!!.uid
        firestoreDb = Firebase.firestore

        // zemi ja tabelata/kolekcijata ko varijabla za ponatamoshen listener na istata
        val refUsers = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users")
        val momentalenKorisnikRef = refUsers.child(firebaseUserId)

        val firestoreVisits = firestoreDb!!.collection("visits")
        firestoreVisits.whereEqualTo("otvoreno_od", firebaseUserId).get()
            .addOnSuccessListener { documents ->
                    (najdeniBaranja as ArrayList<String>).clear()
                    (mUsers as ArrayList<String>).clear()

                if (documents.size() > 0) {
                    (mUsers as ArrayList<String>).clear()

                    tipRezultati.setText(getString(R.string.prabarani))
                    izbrisiBaranja.visibility = View.VISIBLE
                    for (document in documents) {
                        Log.d("FIRESTORE_GET", "${document.id} => ${document.data.get("otvoreno_od")}")
                        (najdeniBaranja as ArrayList<String>).add(document.data["otvoreno_na"] as String)

                        val firebaseOtvorenProfilRef = refUsers.child(document.data["otvoreno_na"].toString())

                        firebaseOtvorenProfilRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.exists()) {
                                    val otvorenProfilDb : Users? = p0.getValue(Users::class.java)
                                    if (otvorenProfilDb != null) {
                                        Log.i("SEARCH", otvorenProfilDb.getUsername().toString())
                                        (mUsers as ArrayList<Users>).add(otvorenProfilDb)

                                        if (context != null) {
                                            Log.i("SEARCH", "context ne e null")

                                            userAdapter = UserAdapter(requireContext(), mUsers!!, false)

                                            //vrzvanje na recyclerview vo ui so userAdapter
                                            recyclerView!!.adapter = userAdapter
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(p0: DatabaseError) {
                            }

                        })
                    }

                    Log.i("SEARCH", mUsers.toString())


                } else {
                    tipRezultati.setText(getString(R.string.preporacani))
                    izbrisiBaranja.visibility = View.GONE
                    momentalenKorisnikRef.addValueEventListener(object : ValueEventListener {

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                val momentalnoNajaven : Users? = p0.getValue(Users::class.java)
                                //listener za podatoci vo kolekcijata users
                                refUsers.addValueEventListener(object : ValueEventListener {
                                    //pri dobivanje na sekoj nov podatok od firebase azuriraj lista na korisnici sho se prikazhva pri otvoranje na search tabot
                                    override fun onDataChange(p0: DataSnapshot) {
                                        (mUsers as ArrayList<Users>).clear()
                                        if (searchUsersEdit.text.toString() == "") {
                                            for (snapshot in p0.children) {
                                                val user = snapshot.getValue(Users::class.java)

                                                // ako ne sum jas dodaj vo userLista za search
                                                if (!(user!!.getUID()).equals(firebaseUserId)) {
                                                    //ako e privaten profil ne prikazvaj
                                                    if (user!!.getDostapnost() != 2) {
                                                        //ako najaveniot e gostin da ne gi gleda i only verifed view userite
                                                        if (momentalnoNajaven!!.getGostin() == 1) {
                                                            if (user!!.getDostapnost() != 1) {
                                                                (mUsers as ArrayList<Users>).add(user)
                                                            }
                                                        } else {
                                                            (mUsers as ArrayList<Users>).add(user)
                                                        }
                                                    }
                                                }
                                            }
                                            //daj mu vrednost na listata na korisnici vo userAdapter

                                            if (context != null) {

                                                userAdapter = UserAdapter(context!!, mUsers!!, false)

                                                //vrzvanje na recyclerview vo ui so userAdapter
                                                recyclerView!!.adapter = userAdapter
                                            }
                                        }

                                    }

                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                })
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                        }

                    })

                }
            }
            .addOnFailureListener {e ->
            }
    }

    private fun searchForUsers(str : String) {
        //zemi momentalen korisnik id
        var firebaseUserId  = FirebaseAuth.getInstance().currentUser!!.uid

        // zemi ja tabelata/kolekcijata ko varijabla za ponatamoshen listener na istata zaedno so query za search po poleto 'search'
        val queryUsers = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").orderByChild("search").startAt(str).endAt(str + "\uf8ff")

        if (str.trim() == "") {
            Log.i("SEARCH", "kje vlecam nanovo")
            retrieveAllUsers()
        } else {
            Log.i("SEARCH", "KJE BARAM")
        //pri dobivanje na nov podatok od firebase azuriraj najdeni rezultati od searchot
        queryUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                if (p0.exists()) {
                    tipRezultati.setText(getString(R.string.rezultati))
                    izbrisiBaranja.visibility = View.GONE

                }else {
                    tipRezultati.setText(getString(R.string.nemarezultat))
                    izbrisiBaranja.visibility = View.GONE

                }
                for (snapshot in p0.children) {
                    val user : Users? = snapshot.getValue(Users::class.java)

                    if (!(user!!.getUID()).equals(firebaseUserId)) {
                        // ako ne sum jas dodaj vo userLista kaj najdeni rezultati
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                //daj mu vrednost na najdeni korisnici vo userAdapter
                if (context != null) {
                    userAdapter = UserAdapter(context!!, mUsers!!, false)
                    //vrzvanje na recyclerview vo ui so userAdapter
                    recyclerView!!.adapter = userAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
        }

    }
}