package com.benjaminearley.chat.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import arrow.core.Either
import com.benjaminearley.chat.ChatApplication
import com.benjaminearley.chat.R
import com.benjaminearley.chat.databinding.MainBinding
import com.benjaminearley.chat.util.applySystemWindowInsetsMargin
import com.benjaminearley.chat.util.applySystemWindowInsetsPadding
import com.benjaminearley.chat.util.setLayoutFullscreen
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(ChatApplication.INSTANCE.getChatModel())
    }
    private lateinit var binding: MainBinding
    private lateinit var navController: NavController

    init {
        lifecycleScope.launchWhenStarted {
            viewModel.getSnackBars().collect { message ->
                Snackbar.make(binding.root, getString(message), Snackbar.LENGTH_SHORT).show()
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.getNewChatTitle().collect { title ->
                supportActionBar?.title = title
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.getLoginState().collect { isLoggedIn ->
                if (!isLoggedIn) {
                    startActivity(Intent(this@MainActivity, SplashActivity::class.java))
                    finish()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.getNavDirections().collect {
                when (it) {
                    is Either.Left -> navController.navigate(it.a)
                    is Either.Right -> navController.popBackStack()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Chat_Teal)
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.main.setLayoutFullscreen(true)
        binding.main.applySystemWindowInsetsPadding(
            applyLeft = true,
            applyRight = true,
            applyTop = true
        )
        binding.navHostFragment.applySystemWindowInsetsPadding(
            applyLeft = true,
            applyRight = true,
            applyBottom = true
        )
        binding.add.applySystemWindowInsetsMargin(
            applyLeft = true,
            applyRight = true,
            applyBottom = true
        )

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.add.setOnClickListener {
            viewModel.addAddClick()
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.chats_fragment -> binding.add.show()
                else -> binding.add.hide()
            }
        }
    }
}
