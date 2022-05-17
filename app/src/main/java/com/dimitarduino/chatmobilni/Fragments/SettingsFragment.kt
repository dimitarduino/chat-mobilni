package com.dimitarduino.chatmobilni.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.dimitarduino.chatmobilni.ModelClasses.Users
import com.dimitarduino.chatmobilni.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class SettingsFragment : Fragment() {
    var korisniciReference : DatabaseReference? = null
    var najavenKorisnik : FirebaseUser? = null
    private var slikaUri : Uri? = null
    private var storageReference : StorageReference? = null
    private var daliECover : String = ""
    private var mrezaZaIzmenvanje : String = ""
    private var smenatoDostapnost = false
    private var nacrtanoDostapnost = false

    //ui komponenti
    private lateinit var profilnaSlikaProfil : CircleImageView
    private lateinit var korisnickoImeProfil : TextView
    private lateinit var fullnameProfil : TextView
    private lateinit var naslovnaSlikaProfil : ImageView
    private lateinit var facebookProfil : LinearLayout
    private lateinit var instaProfil : LinearLayout
    private lateinit var websiteProfil : LinearLayout
    private lateinit var progressBar : ProgressBar
    private lateinit var zacuvajDostapnostBtn : Button
    private lateinit var dostapnostSpinner : Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        profilnaSlikaProfil = view.findViewById(R.id.profilna_slika)
        korisnickoImeProfil = view.findViewById(R.id.korisnickoImeProfil)
        fullnameProfil = view.findViewById(R.id.fullnameProfil)
        naslovnaSlikaProfil = view.findViewById(R.id.naslovna_slika)
        facebookProfil = view.findViewById(R.id.facebookLinear)
        instaProfil = view.findViewById(R.id.instagramLinear)
        websiteProfil = view.findViewById(R.id.webLinear)
        progressBar = view.findViewById(R.id.progressbar)
        dostapnostSpinner = view.findViewById(R.id.dostapnost_spinner)
        zacuvajDostapnostBtn = view.findViewById(R.id.zacuvajDostapnost)

        progressBar.visibility = View.INVISIBLE


        najavenKorisnik = FirebaseAuth.getInstance().currentUser
        korisniciReference = FirebaseDatabase.getInstance("https://chatmobilni-default-rtdb.firebaseio.com/").reference.child("users").child(najavenKorisnik!!.uid)
        storageReference = FirebaseStorage.getInstance().reference.child("user_images")

        // init na dostapnost select
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.dostapnost_array,
                android.R.layout.simple_spinner_dropdown_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                dostapnostSpinner.adapter = adapter
            }
        }

        zacuvajDostapnostBtn.setOnClickListener {
            zacuvajDostapnost(dostapnostSpinner.selectedItemPosition)
        }

        korisniciReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user : Users? = p0.getValue(Users::class.java)
                    if (context != null) {
                        korisnickoImeProfil.text = "@" + user!!.getUsername()
                        fullnameProfil.text = user!!.getFullname()
                        Picasso.get().load(user.getProfile()).into(profilnaSlikaProfil)
                        Picasso.get().load(user.getCover()).into(naslovnaSlikaProfil)
                        Log.i("proverkaDostapnost", dostapnostSpinner.selectedItemPosition.toString())
                        Log.i("proverkaDostapnost", user.getDostapnost().toString())
                        if (dostapnostSpinner.selectedItemPosition != user.getDostapnost()) {
                            dostapnostSpinner.setSelection(user.getDostapnost())
                        }

                        nacrtanoDostapnost = true
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        profilnaSlikaProfil.setOnClickListener{
            pickImage()
        }

        naslovnaSlikaProfil.setOnClickListener {
            daliECover = "cover"
            pickImage()
        }

        facebookProfil.setOnClickListener {
            mrezaZaIzmenvanje = "facebook"

            namestiSocijalniMrezhi()
        }

        instaProfil.setOnClickListener {
            mrezaZaIzmenvanje = "instagram"

            namestiSocijalniMrezhi()
        }

        websiteProfil.setOnClickListener {
            mrezaZaIzmenvanje = "website"

            namestiSocijalniMrezhi()
        }

        korisnickoImeProfil.setOnClickListener {
            mrezaZaIzmenvanje = "username"

            namestiSocijalniMrezhi()
        }

        fullnameProfil.setOnClickListener {
            mrezaZaIzmenvanje = "fullname"

            namestiSocijalniMrezhi()
        }

        return view
    }

    @SuppressLint("ResourceAsColor", "RestrictedApi")
    private fun namestiSocijalniMrezhi() {
        val builder : androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(requireContext(), androidx.appcompat.R.style.Base_ThemeOverlay_AppCompat_Dialog_Alert)

        if (mrezaZaIzmenvanje == "website") {
            builder.setTitle("Enter your link")
        } else {
            if (mrezaZaIzmenvanje == "fullname") {
                builder.setTitle("Enter your name")
            } else {
                builder.setTitle("Enter your username")
            }
        }

        val editMreza = EditText(context)
        editMreza.setSingleLine()
        editMreza.setBackgroundResource(R.drawable.account_input_bg)
        editMreza.setPadding(10, 30, 10, 30)


        if (mrezaZaIzmenvanje == "website") {
            editMreza.hint = "ex. www.google.com"
        } else {
            if (mrezaZaIzmenvanje == "fullname") {
                editMreza.hint = "ex. John Doe"
            } else {
                editMreza.hint = "ex. johndoe"
            }
        }
        builder.setView(editMreza, 20, 20, 20, 20)

        builder.setPositiveButton("Save", DialogInterface.OnClickListener {
                dialog, which ->
            val vrednostMreza = editMreza.text.toString()

            if (vrednostMreza == "") {
                Toast.makeText(context, "Username/URL is required!", Toast.LENGTH_SHORT).show()
            } else {
                zacuvajSocijalnaMrezha(vrednostMreza)
            }
        })

        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener {
                dialog, which -> dialog.cancel() })

        builder.show()
    }

    private fun zacuvajDostapnost(dostapnost: Int) {
        val dostapnostHash = HashMap<String, Any>()
        dostapnostHash["dostapnost"] = dostapnost
        korisniciReference!!.updateChildren(dostapnostHash).addOnCompleteListener {
                task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun zacuvajSocijalnaMrezha(vrednost: String) {
        val socialMap = HashMap<String, Any>()

        when (mrezaZaIzmenvanje) {
            "facebook" -> {
                socialMap["facebook"] = "https://m.facebook.com/$vrednost"
            }
            "instagram" -> {
                socialMap["instagram"] = "https://instagram.com/$vrednost"
            }
            "website" -> {
                socialMap["website"] = "https://$vrednost"
            }
            "username" -> {
                socialMap["username"] = vrednost

            }
            "fullname" -> {
                socialMap["fullname"] = vrednost

            }
        }

        korisniciReference!!.updateChildren(socialMap).addOnCompleteListener {
                task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
            }
        }
        mrezaZaIzmenvanje = ""
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            slikaUri = data!!.data

            Log.i("slika", slikaUri.toString())
            Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show()

            prikaciSlikaBaza()
        }
    }

    private fun prikaciSlikaBaza() {
        progressBar.visibility = View.VISIBLE

        if (slikaUri != null) {
            val fileRef = storageReference!!.child(System.currentTimeMillis().toString() + ".jpg")

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

                    if (daliECover == "cover") {
                        val coverMap = HashMap<String, Any>()
                        coverMap["cover"] = url
                        korisniciReference!!.updateChildren(coverMap)
                        daliECover = ""
                    } else {
                        val profileMap = HashMap<String, Any>()
                        profileMap["profile"] = url
                        korisniciReference!!.updateChildren(profileMap)
                    }

                    progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }


}