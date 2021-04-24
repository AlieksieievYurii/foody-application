package com.yurii.foody.authorization.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.yurii.foody.R

class LoadingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onResume() {
        super.onResume()
        statusBar(hide = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        statusBar(hide = false)
    }

    private fun statusBar(hide: Boolean) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            activity?.window?.setDecorFitsSystemWindows(!hide)
        } else {
            @Suppress("DEPRECATION")
            if (hide)
                activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
            else
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }
}