package com.dimitarduino.chatmobilni

import com.dimitarduino.chatmobilni.ModelClasses.Users

interface IListener {
    fun onUserClickListener(user: Users?)
}