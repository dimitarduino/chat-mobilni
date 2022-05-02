package com.dimitarduino.chatmobilni.AdapterClasses

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.dimitarduino.chatmobilni.ModelClasses.Chat
import com.dimitarduino.chatmobilni.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
@Keep
public class ChatsAdapter(
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder?>()
{
    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String

    var firebaseKorisnik: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mChatList = mChatList
        this.mContext = mContext
        this.imageUrl = imageUrl
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 1)
        {
            val view: View = LayoutInflater.from(mContext).inflate(com.dimitarduino.chatmobilni.R.layout.sent_message, parent, false)
            ViewHolder(view)
        }
        else
        {
            Log.i("primena poraka", "primeno")
            val view: View = LayoutInflater.from(mContext).inflate(com.dimitarduino.chatmobilni.R.layout.received_message, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val poraka = mChatList[position]

        if (this.imageUrl.toString() != "") {
            Picasso.get().load(this.imageUrl).into(holder.profilnaSlika)
        }

        //slika vo poraka
        if (poraka.getPoraka().equals("sent you an image.") && !poraka.getUrl().equals(""))
        {
            //pratena slika
            if (poraka.getIsprakjac().equals(firebaseKorisnik!!.uid))
            {
                holder.tekstPoraka!!.visibility = View.GONE
                holder.pratenaSlika!!.visibility = View.VISIBLE
                Picasso.get().load(poraka.getUrl()).into(holder.pratenaSlika)
            }
            //primena slika
            else if (!poraka.getIsprakjac().equals(firebaseKorisnik!!.uid))
            {
                holder.tekstPoraka!!.visibility = View.GONE
                holder.primenaSlika!!.visibility = View.VISIBLE
                Picasso.get().load(poraka.getUrl()).into(holder.primenaSlika)
            }
        } else {
            //tekst vo poraka‘
                holder.tekstPoraka!!.visibility = View.VISIBLE
            holder.tekstPoraka!!.text = poraka.getPoraka()
        }


    }

    override fun getItemCount(): Int {
        return mChatList.size
    }


    override fun getItemViewType(position: Int): Int
    {
        return if (mChatList[position].getIsprakjac().equals(firebaseKorisnik!!.uid))
        {
            1
        }
        else
        {
            0
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var profilnaSlika: CircleImageView? = null
        var tekstPoraka: TextView? = null
        var primenaSlika: ImageView? = null
        var porakaSeen: TextView? = null
        var pratenaSlika: ImageView? = null

        init {
            profilnaSlika = itemView.findViewById(R.id.profilnaKorisnik)
            tekstPoraka = itemView.findViewById(R.id.tekstPoraka)
            primenaSlika = itemView.findViewById(R.id.primenaSlika)
            porakaSeen = itemView.findViewById(R.id.seen)
            pratenaSlika = itemView.findViewById(R.id.pratenaSlika)
        }
    }
}