package com.dimitarduino.chatmobilni

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.dimitarduino.chatmobilni.Fragments.ChatsFragment
import com.dimitarduino.chatmobilni.Fragments.SearchFragment
import com.dimitarduino.chatmobilni.Fragments.SettingsFragment
import com.dimitarduino.chatmobilni.ModelClasses.Chat
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.dimitarduino.chatmobilni.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    //varijabli firebase
    var refUsers : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null

    //varijabli ui komponenti
    private lateinit var usernameText : TextView
    private lateinit var profileImage : de.hdodenhof.circleimageview.CircleImageView

    //varijabli funkcionalnosti
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //deklariraj ui komponenti
        usernameText = findViewById(R.id.username_text)
        profileImage = findViewById(R.id.profile_image)


        //zemi korisnik firebase
        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseUser!!.uid)


        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        supportActionBar!!.title = ""

        val tabLayout : TabLayout = findViewById(R.id.tabLayout)
        val viewPager : androidx.viewpager.widget.ViewPager = findViewById(R.id.viewPager)

        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("chats")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var brojNeProcitaniPoraki = 0

                for (dataSnapshot in  p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    Log.i("poraka", dataSnapshot.toString())

                    if (chat!!.getPrimac().equals(firebaseUser!!.uid) && !chat.getSeen()) {
                        Log.i("poraka", "vlegov brat");
                        brojNeProcitaniPoraki += 1
                    }
                }

                if (brojNeProcitaniPoraki == 0) {
                    viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
                } else {
                    viewPagerAdapter.addFragment(ChatsFragment(), "Chats ($brojNeProcitaniPoraki)")
                }

                viewPagerAdapter.addFragment(SearchFragment(), "Search")
                viewPagerAdapter.addFragment(SettingsFragment(), "Settings")
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        //prikazi informacii od najaven korisnik
        refUsers!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user : Users? = p0.getValue(Users::class.java)
                    usernameText.text = user!!.getUsername()
                    Log.i("PROFIL", user.getProfile().toString())
                    Picasso.get().load(user.getProfile()).into(profileImage)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

                return true
            }
        }

        return false
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fragmentManager) {
        private val fragments : ArrayList<androidx.fragment.app.Fragment>
        private val titles : ArrayList<String>

        init {
            fragments = ArrayList<androidx.fragment.app.Fragment>()
            titles = ArrayList<String>()
        }

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: androidx.fragment.app.Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    private fun namestiStatus(status: String)
    {
        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseUser!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        ref!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()

        namestiStatus("online")
    }

    override fun onPause() {
        super.onPause()

        namestiStatus("offline")
    }
}