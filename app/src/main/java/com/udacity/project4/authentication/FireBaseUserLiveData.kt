package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseUserLiveData : LiveData<FirebaseUser?>() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authListener = FirebaseAuth.AuthStateListener {
        value = it.currentUser
    }

    override fun onInactive(){
        firebaseAuth.addAuthStateListener(authListener)
    }
    override fun onActive(){
        firebaseAuth.removeAuthStateListener(authListener)
    }
}