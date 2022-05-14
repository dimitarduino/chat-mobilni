package com.dimitarduino.chatmobilni.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dimitarduino.chatmobilni.MessageChatActivity
import com.dimitarduino.chatmobilni.ModelClasses.Chat
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.dimitarduino.chatmobilni.R
import com.dimitarduino.chatmobilni.VisitUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter (
    mContext : Context,
    mUsers : List<Users>,
    isChatCheck : Boolean
) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    private val mContext : Context = mContext
    private val mUsers : List<Users> = mUsers
    private val isChatCheck : Boolean = isChatCheck
    var poslednaPorakaVar : String = ""
    var procitanoPosledna : Boolean = true

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        //implementacija i mestenje na viewholder za adapterot
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, viewGroup, false)

        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // zemi go potrebniot korisnik i-tiot korisnik i napraj mu element so slika i tekst
        val user : Users = mUsers[position]
        holder.usernameText.text = user.getUsername()
        Log.i("COVER", user.getProfile().toString())
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(holder.profileImageView)

        //online i offline status
        if (isChatCheck)
        {
            zemiPoslednaPoraka(user.getUID(), holder.poslednaPorakaTekst, holder.procitanoPosledna)
        }
        else
        {
            holder.poslednaPorakaTekst.visibility = View.GONE
        }

        if (isChatCheck)
        {
            if (user.getStatus() == "online")
            {
                holder.onlineImageView.visibility = View.VISIBLE
                holder.offlineImageView.visibility = View.GONE
            }
            else
            {
                holder.onlineImageView.visibility = View.GONE
                holder.offlineImageView.visibility = View.VISIBLE
            }
        }
        else
        {
            holder.onlineImageView.visibility = View.GONE
            holder.offlineImageView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener{
            val options = arrayOf<CharSequence>(
                "Send message",
                "Visit Profile"
            )

            val builder : AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("")
            builder.setItems(options, DialogInterface.OnClickListener {dialog, which ->
                if (which == 0) {
                    // open chats
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("idNaDrugiot", user.getUID())
                    intent.putExtra("username", user.getUsername())
                    intent.putExtra("profile", user.getProfile())
                    mContext.startActivity(intent)
                } else if (which == 1) {
                    //open profile
                    val intent = Intent(mContext, VisitUserActivity::class.java)
                    intent.putExtra("profilZaOtvoranje", user.getUID())
                    mContext.startActivity(intent)
                }
            })

            builder.show()


        }
    }

    private fun zemiPoslednaPoraka(uid: String?, poslednaPorakaTekst: TextView, procitanoPoslednoImage : CircleImageView) {
        poslednaPorakaVar = "No Message"
        procitanoPosledna = true

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refrence = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("chats")

        refrence.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val chat: Chat? = dataSnapshot.getValue(Chat::class.java)

                    if (firebaseUser!=null && chat!=null)
                    {
                        if (chat.getPrimac() == firebaseUser.uid  &&
                            chat.getIsprakjac() == uid  ||
                            chat.getPrimac() == uid  &&
                            chat.getIsprakjac() == firebaseUser.uid)
                        {
                            poslednaPorakaVar = chat.getPoraka()!!

                            if (!chat.getSeen() &&  chat.getPrimac() == firebaseUser.uid) {
                                procitanoPosledna = false
                            }
                        }
                    }
                }
                when(poslednaPorakaVar)
                {
                    "sent you an image." -> poslednaPorakaTekst.text = "image sent."
                    else -> poslednaPorakaTekst.text = poslednaPorakaVar
                }
                if (procitanoPosledna == false) {
                    procitanoPoslednoImage.visibility = View.VISIBLE
                } else {
                    procitanoPoslednoImage.visibility = View.GONE
                }
                poslednaPorakaVar = "No Message"
                procitanoPosledna = true
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var usernameText : TextView = itemView.findViewById(R.id.username_text_search)
        var profileImageView : CircleImageView = itemView.findViewById(R.id.profile_image_search)
        var onlineImageView : CircleImageView = itemView.findViewById(R.id.image_online)
        var offlineImageView : CircleImageView = itemView.findViewById(R.id.image_offline)
        var poslednaPorakaTekst : TextView = itemView.findViewById(R.id.poslednaPorakaTekst)
        var poslednaPorakaVreme : TextView = itemView.findViewById(R.id.poslednaPorakaVreme)
        var procitanoPosledna : CircleImageView = itemView.findViewById(R.id.slika_neprocitano)
    }
}