package com.yurii.foody.authorization.loading

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentLoadingBinding
import timber.log.Timber

class LoadingFragment : Fragment() {

    private lateinit var binding: FragmentLoadingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_loading, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.animation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                Timber.i("Animation Start")
            }

            override fun onAnimationEnd(p0: Animator?) {
                Timber.i("Animation End")
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {
                findNavController().navigate(R.id.action_loadingFragment_to_authenticationFragment)
            }

        })
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