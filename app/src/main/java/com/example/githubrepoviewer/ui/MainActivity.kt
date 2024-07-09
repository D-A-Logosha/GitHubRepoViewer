package com.example.githubrepoviewer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.githubrepoviewer.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding by lifecycleLazy()

    override fun onCreate(savedInstanceState: Bundle?) {
        var keepSplashScreenOn = true
        lifecycleScope.launch {
            delay(SPLASH_SCREEN_DURATION_MILLIS)
            keepSplashScreenOn = false
        }
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashScreenOn
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private companion object {
        const val SPLASH_SCREEN_DURATION_MILLIS = 999L
    }
}
