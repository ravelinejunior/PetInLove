package com.raveline.petinlove.presentation.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.raveline.petinlove.R
import com.raveline.petinlove.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(mainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerViewMain) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.profileFragment,
                R.id.homeFragment,
                R.id.postFragment,
                R.id.searchPersonFragment,
                R.id.savedPostsFragment
            )
        )

        mainBinding.bnvMainId.setupWithNavController(navController)
        // mainBinding.bnvMainId.itemIconTintList = null
        setupActionBarWithNavController(navController, appBarConfig)
    }

    fun hideBottomNavigationView() {
        mainBinding.apply {
            bnvMainId.clearAnimation()
            bnvMainId.animate().translationY(bnvMainId.height.toFloat()).duration = 600
        }.also {
            lifecycleScope.launch {
                delay(600)
                mainBinding.bnvMainId.visibility = View.GONE
            }
        }
    }

    fun showBottomNavigationView() {
        mainBinding.apply {
            bnvMainId.visibility = View.VISIBLE
            bnvMainId.clearAnimation()
            bnvMainId.animate().translationY(0f).duration = 600

        }
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        finish()
    }
}