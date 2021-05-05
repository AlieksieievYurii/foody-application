package com.yurii.foody.authorization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yurii.foody.R

class FragmentTest : Fragment() {
    private val args: FragmentTestArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)

        view.findViewById<TextView>(R.id.role).text = args.role.role
        view.findViewById<Button>(R.id.button).setOnClickListener {
            findNavController().navigate(FragmentTestDirections.actionFragmentTestToChooseRoleFragment(selectNewRole = true))
        }

        return view
    }
}