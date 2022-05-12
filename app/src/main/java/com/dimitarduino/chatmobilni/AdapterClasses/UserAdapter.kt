package com.dimitarduino.chatmobilni.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dimitarduino.chatmobilni.MessageChatActivity
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.dimitarduino.chatmobilni.R
import com.dimitarduino.chatmobilni.VisitUserActivity
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

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        //implementacija i mestenje na viewholder za adapterot
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, viewGroup, false)

        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // zemi go potrebniot korisnik i-tiot korisnik i napraj mu element so slika i tekst
        val user : Users = mUsers[position]
        holder.usernameText.text = user!!.getUsername()
        Picasso.get().load(user.getProfile()).into(holder.profileImageView)

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

    override fun getItemCount(): Int {
        return mUsers.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var usernameText : TextView = itemView.findViewById(R.id.username_text_search)
        var profileImageView : CircleImageView = itemView.findViewById(R.id.profile_image_search)
        var onlineImageView : CircleImageView = itemView.findViewById(R.id.image_online)
        var offlineImageView : CircleImageView = itemView.findViewById(R.id.image_offline)
        var lastMessageText : TextView = itemView.findViewById(R.id.message_last)

    }
}