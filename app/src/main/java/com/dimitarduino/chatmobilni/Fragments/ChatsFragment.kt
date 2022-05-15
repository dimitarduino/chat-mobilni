package com.dimitarduino.chatmobilni.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dimitarduino.chatmobilni.AdapterClasses.UserAdapter
import com.dimitarduino.chatmobilni.Izvestuvanja.Token
import com.dimitarduino.chatmobilni.Izvestuvanja.firebaseInstanceId
import com.dimitarduino.chatmobilni.ModelClasses.Chatlist
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.dimitarduino.chatmobilni.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase

class ChatsFragment : Fragment() {
    private var userAdapter : UserAdapter? = null
    private var korisnici : List<Users>? = null
    private var korisniciChatList : List<Chatlist>? = null
    lateinit var chatListRecycler : RecyclerView
    private var firebaseKorisnik : FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        chatListRecycler = view.findViewById(R.id.chatLista_recycler)
        chatListRecycler.setHasFixedSize(true)
        chatListRecycler.layoutManager = LinearLayoutManager(context)

        firebaseKorisnik = FirebaseAuth.getInstance().currentUser
//        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        korisniciChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("chatList").child(firebaseKorisnik!!.uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (korisniciChatList as ArrayList).clear()

                for (dataSnapshot in p0.children)
                {
                    val korisnik = dataSnapshot.getValue(Chatlist::class.java)


                    Log.i("korisnikChatList", korisnik.toString())
                    (korisniciChatList as ArrayList).add(korisnik!!)
                    popolniChatlist()
                }

                popolniChatlist()
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        updateToken(FirebaseInstanceId.getInstance().token)

        return view
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("tokens")
        val token1 = Token(token!!)
        ref.child(firebaseKorisnik!!.uid).setValue(token1)
    }

    private fun popolniChatlist() {
        korisnici = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users")

        ref!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (korisnici as ArrayList).clear()

                for (dataSnapshot in p0.children) {
                    val korisnik = dataSnapshot.getValue(Users::class.java)

                    for (sekojChatList in korisniciChatList!!) {
                        if (korisnik!!.getUID().equals(sekojChatList.getId())) {
                            (korisnici as ArrayList).add(korisnik!!)
                        }
                    }
                }

                Log.i("korisnici", korisniciChatList.toString())

                if (context != null) {
                userAdapter = UserAdapter(context!!, (korisnici as ArrayList<Users>), true)

                chatListRecycler.adapter = userAdapter

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

    }
}