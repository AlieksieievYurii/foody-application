package com.yurii.foody

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.yurii.foody.utils.OnBackPressed

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.forEach {
            if (it is NavHostFragment) {
                for (fragment in it.childFragmentManager.fragments)
                    if (fragment is OnBackPressed && fragment.onBackPressed()) {
                        return
                    }
            }
        }
        super.onBackPressed()
    }
}