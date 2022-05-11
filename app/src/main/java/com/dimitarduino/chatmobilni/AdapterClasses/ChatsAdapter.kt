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
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.dimitarduino.chatmobilni.ModelClasses.Chat
import com.dimitarduino.chatmobilni.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
class ChatsAdapter(
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

                if (firebaseKorisnik!!.uid == poraka.getIsprakjac())
                {
                    holder.tekstPoraka!!.setOnClickListener {
                        val options = arrayOf<CharSequence>(
                            "Delete Message",
                            "Cancel"
                        )

                        var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                        builder.setTitle("What do you want?")

                        builder.setItems(options, DialogInterface.OnClickListener{
                                dialog, which ->
                            if (which == 0)
                            {
                                izbrisiPratenaPoraka(position, holder)
                            }
                        })
                        builder.show()
                    }
                }
        }

        if (position == mChatList.size-1)
        {
            if (poraka.getSeen())
            {
                holder.porakaSeen!!.text = "Seen"

                if (poraka.getPoraka().equals("sent you an image.") && !poraka.getUrl().equals(""))
                {
                    val porakaSeenElement: RelativeLayout.LayoutParams? = holder.porakaSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    porakaSeenElement!!.setMargins(0, 245, 10, 0)
                    holder.porakaSeen!!.layoutParams = porakaSeenElement
                }
            }
            else
            {
                holder.porakaSeen!!.text = "Sent"

                if (poraka.getPoraka().equals("sent you an image.") && !poraka.getUrl().equals(""))
                {
                    val porakaSeenElement: RelativeLayout.LayoutParams? = holder.porakaSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    porakaSeenElement!!.setMargins(0, 245, 10, 0)
                    holder.porakaSeen!!.layoutParams = porakaSeenElement
                }
            }
        }
        else
        {
            holder.porakaSeen!!.visibility = View.GONE
        }
    }

    private fun izbrisiPratenaPoraka(position: Int, holder: ChatsAdapter.ViewHolder) {
        val ref = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("chats")
            .child(mChatList.get(position).getPorakaId()!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    Toast.makeText(holder.itemView.context, "Deleted.", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(holder.itemView.context, "Failed, please try again.", Toast.LENGTH_SHORT).show()
                }
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
        var porakaRelativeLayout: RelativeLayout? = null

        init {
            profilnaSlika = itemView.findViewById(R.id.profilnaKorisnik)
            tekstPoraka = itemView.findViewById(R.id.tekstPoraka)
            primenaSlika = itemView.findViewById(R.id.primenaSlika)
            porakaSeen = itemView.findViewById(R.id.seen)
            pratenaSlika = itemView.findViewById(R.id.pratenaSlika)
            porakaRelativeLayout = itemView.findViewById(R.id.porakaRelative)
        }
    }
}