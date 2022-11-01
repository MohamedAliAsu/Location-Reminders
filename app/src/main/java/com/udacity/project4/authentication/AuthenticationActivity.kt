package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
const val SIGN_IN_REQUEST_CODE = 10


class AuthenticationActivity : AppCompatActivity() {
    private lateinit var bind: ActivityAuthenticationBinding
    val vm by viewModels<AuthVM>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_authentication)





        bind.login.setOnClickListener {

            Log.i("authActivity",(FirebaseAuth.getInstance().currentUser==null).toString())
            launchSignInFlow()
        }

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private fun launchSignInFlow() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(arrayListOf(EmailBuilder().build(), GoogleBuilder().build()))
            .build(),
            SIGN_IN_REQUEST_CODE)
    }

    override fun onStart() {
        super.onStart()
        vm.authState.observe(this){
            if(it == AuthVM.Authentication.AUTHENTICATED) startActivity(Intent(this,RemindersActivity::class.java))
            else {Log.i("authActivity ","unauthenticated")}
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== SIGN_IN_REQUEST_CODE){
            val response = IdpResponse.fromResultIntent(data)
            if(resultCode == RESULT_OK){
                Log.i("AuthActivity","user ${FirebaseAuth.getInstance().currentUser?.displayName} signed in")
                startActivity(Intent(this,RemindersActivity::class.java))
            }
            else {
                Log.i("AuthActivity","unsuccessful login , with error ${response?.error?.errorCode}")
            }

        }}
}

class AuthVM : ViewModel() {
    enum class Authentication {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authState = FirebaseUserLiveData().map {
        if (it == null) Authentication.UNAUTHENTICATED else Authentication.AUTHENTICATED

    }

}
