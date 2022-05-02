package com.dimitarduino.chatmobilni.ModelClasses

import android.util.Log
import androidx.annotation.Keep

@Keep
public class Chat {
    private var isprakjac: String = ""
    private var poraka: String = ""
    private var primac: String = ""
    private var seen : Boolean = false
    private var url: String = ""
    private var porakaId: String = ""

    constructor()


    constructor(
        isprakjac: String,
        poraka: String,
        primac: String,
        seen: Boolean,
        url: String,
        porakaId: String
    ) {
        Log.i("isprakjac", isprakjac)
        Log.i("poraka", poraka)
        Log.i("primac", primac)
        Log.i("seen", seen.toString())
        Log.i("url", url)
        Log.i("porakaId", porakaId)
        this.isprakjac = isprakjac
        this.poraka = poraka
        this.primac = primac
        this.seen = seen
        this.url = url
        this.porakaId = porakaId
    }



    fun getIsprakjac(): String? {
        return isprakjac
    }

    fun setIsprakjac(isprakjac: String?) {
        this.isprakjac = isprakjac!!
    }

    fun getPoraka(): String? {
        return poraka
    }

    fun setPoraka(poraka: String?) {
        this.poraka = poraka!!
    }

    fun getPrimac(): String? {
        return primac
    }

    fun setPrimac(primac: String?) {
        this.primac = primac!!
    }

    fun getSeen(): Boolean {
        return seen
    }

    fun setSeen(seen: Boolean?) {
        this.seen = seen!!
    }

    fun getUrl(): String? {
        return url
    }

    fun setUrl(url: String?) {
        this.url = url!!
    }

    fun getPorakaId(): String {
        return porakaId
    }

    fun setPorakaId(porakaId: String?) {
        this.porakaId = porakaId!!
    }
}