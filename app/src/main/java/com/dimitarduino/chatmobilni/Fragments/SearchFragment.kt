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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dimitarduino.chatmobilni.AdapterClasses.UserAdapter
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.dimitarduino.chatmobilni.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {
    private var userAdapter : UserAdapter? = null
    private var mUsers : List<Users>? = null
    private var recyclerView : RecyclerView? = null

    //deklariraj ui komponenti
    private lateinit var searchUsersEdit : EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //definiranje koj fragment da go koristi
        val view : View = inflater.inflate(R.layout.fragment_search, container, false)

        //definiraj ui komponenti
        searchUsersEdit = view.findViewById(R.id.searchUserEdit)
        recyclerView = view.findViewById(R.id.searchList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList()
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

        return view
    }

    private fun retrieveAllUsers() {
        //zemi momentalen korisnik id
        var firebaseUserId  = FirebaseAuth.getInstance().currentUser!!.uid

        // zemi ja tabelata/kolekcijata ko varijabla za ponatamoshen listener na istata
        val refUsers = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users")

        //listener za podatoci vo kolekcijata users
        refUsers.addValueEventListener(object : ValueEventListener {
            //pri dobivanje na sekoj nov podatok od firebase azuriraj lista na korisnici sho se prikazhva pri otvoranje na search tabot
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                if (searchUsersEdit.text.toString() == "") {
                    for (snapshot in p0.children) {
                        Log.i("TEST", snapshot.getValue(Users::class.java).toString())
                        val user = snapshot.getValue(Users::class.java)

                        if (!(user!!.getUID()).equals(firebaseUserId)) {
                            // ako ne sum jas dodaj vo userLista za search
                            (mUsers as ArrayList<Users>).add(user)
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

    private fun searchForUsers(str : String) {
        //zemi momentalen korisnik id
        var firebaseUserId  = FirebaseAuth.getInstance().currentUser!!.uid

        // zemi ja tabelata/kolekcijata ko varijabla za ponatamoshen listener na istata zaedno so query za search po poleto 'search'
        val queryUsers = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").orderByChild("search").startAt(str).endAt(str + "\uf8ff")

        //pri dobivanje na nov podatok od firebase azuriraj najdeni rezultati od searchot
        queryUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
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