package com.yurii.foody.ui

import android.content.Context
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.hsalf.smileyrating.SmileyRating
import com.yurii.foody.R

class RatingDialog(context: Context) {
    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(context)
            .setView(R.layout.dialog_give_rating)
            .setCancelable(true)
            .create()
    }

    fun show(onRate: (rating: Int) -> Unit) {
        if (!dialog.isShowing) {
            dialog.show()
            val raringView = dialog.findViewById<SmileyRating>(R.id.rating)!!
            raringView.resetSmiley()

            dialog.findViewById<Button>(R.id.rate)?.setOnClickListener {
                onRate.invoke(raringView.selectedSmiley.rating)
                close()
            }
        }

    }

    fun close() {
        if (dialog.isShowing)
            dialog.dismiss()
    }
}