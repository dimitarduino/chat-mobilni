package com.dimitarduino.chatmobilni.ModelClasses

class Users {
    private var uid : String = ""
    private var username : String = ""
    private var fullname : String = ""
    private var profile : String = ""
    private var cover : String = ""
    private var status : String = ""
    private var search : String = ""
    private var facebook : String = ""
    private var instagram : String = ""
    private var website : String = ""
    private var gender : String = ""

    constructor() {

    }

    constructor(
        uid: String,
        username: String,
        fullname: String,
        profile: String,
        cover: String,
        status: String,
        search: String,
        facebook: String,
        instagram: String,
        website: String,
        gender: String,
    ) {
        this.uid = uid
        this.username = username
        this.fullname = fullname
        this.profile = profile
        this.cover = cover
        this.status = status
        this.search = search
        this.facebook = facebook
        this.instagram = instagram
        this.website = website
        this.gender = gender
    }

    fun getUID() : String? {
        return uid
    }

    fun setUID(uid : String) {
        this.uid = uid
    }

    fun getUsername() : String? {
        return username
    }

    fun setUsername(username : String) {
        this.username = username
    }

    fun getFullname() : String {
        return fullname
    }

    fun setFullname(fullname: String) {
        this.fullname = fullname
    }

    fun getProfile() : String? {
        return profile
    }

    fun setProfile(profile : String) {
        this.profile = profile
    }

    fun getCover() : String? {
        return cover
    }

    fun setCover(cover : String) {
        this.cover = cover
    }

    fun getStatus() : String? {
        return status
    }

    fun setStatus(status : String) {
        this.status = status
    }

    fun getSearch() : String? {
        return search
    }

    fun setSearch(search : String) {
        this.search = search
    }

    fun getFacebook() : String? {
        return facebook
    }

    fun setFacebook(facebook: String) {
        this.facebook = facebook
    }

    fun getInstagram() : String? {
        return instagram
    }

    fun setInstagram(instagram: String) {
        this.instagram = instagram
    }

    fun getWebsite() : String? {
        return website
    }

    fun setWebsite(website: String) {
        this.website = website
    }



    fun getGender() : String {
        return gender
    }

    fun setGender(gender: String) {
        this.gender = gender
    }



}