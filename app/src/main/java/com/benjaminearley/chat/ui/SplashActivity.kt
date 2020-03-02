package com.benjaminearley.chat.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.benjaminearley.chat.ChatApplication
import com.benjaminearley.chat.R
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class SplashActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> {
        ChatApplication.INSTANCE.run { MainViewModelFactory(getUserModel()) }
    }

    init {
        lifecycleScope.launchWhenStarted {
            if (viewModel.isAuthenticated()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            } else {
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(authProviders)
                        .setTheme(R.style.Chat_Teal_LoginTheme)
                        .setLogo(R.mipmap.ic_launcher)
                        .build(),
                    RC_SIGN_IN
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                viewModel.createUser(user)
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            } else {
                finish()
            }
        }
    }

    companion object {
        const val RC_SIGN_IN = 123
        val authProviders = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )
    }
}