package com.ach.testapp

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.ach.ramppaysdk.RampPayEnvironment
import com.ach.ramppaysdk.RampPayRenderingOption
import com.ach.ramppaysdk.RampPaySdk
import com.ach.ramppaysdk.RampPaySdkConfig
import com.ach.testapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var rampPaySdk: RampPaySdk
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


        val config = RampPaySdkConfig(
            false,
            RampPayEnvironment.Sandbox
        )
        rampPaySdk = RampPaySdk(this, config)

        binding.fab.setOnClickListener { view ->
            rampPaySdk.show(RampPayRenderingOption.WebViewOverlay)
        }
    }


}