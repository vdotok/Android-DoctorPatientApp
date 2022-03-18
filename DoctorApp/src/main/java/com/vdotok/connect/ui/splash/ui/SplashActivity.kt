package com.vdotok.connect.ui.splash.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.vdotok.connect.R
import com.vdotok.connect.databinding.ActivitySplashBinding
import com.vdotok.connect.prefs.Prefs
import com.vdotok.connect.ui.account.ui.AccountActivity.Companion.createAccountsActivity
import com.vdotok.connect.ui.dashBoard.ui.DashboardActivity.Companion.createDashboardActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var prefs: Prefs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()

    }

    private fun init() {
        prefs = Prefs(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        performAuthOperations()
    }


    private fun performAuthOperations() {
        prefs.loginInfo?.let {
            startActivity(createDashboardActivity(this))
            finish()
        }?: kotlin.run {
            moveToAccountsActivity()
        }
    }

    private fun moveToAccountsActivity() {
        startActivity(createAccountsActivity(this))
        finish()
    }

    companion object {

        const val API_ERROR = "API_ERROR"

    }
}
