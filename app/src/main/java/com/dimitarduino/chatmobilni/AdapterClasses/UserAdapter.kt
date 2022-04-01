package com.dimitarduino.chatmobilni.AdapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.dimitarduino.chatmobilni.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

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