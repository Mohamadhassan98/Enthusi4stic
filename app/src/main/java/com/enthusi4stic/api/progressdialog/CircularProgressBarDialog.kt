package com.enthusi4stic.api.progressdialog

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import com.enthusi4stic.R
import kotlinx.android.synthetic.main.dialog_progress_bar.*

class CircularProgressBarDialog(ctx: Context?, private val message: String?): ProgressDialog(ctx, R.style.enthusi4stic_circular_progress_bar_dialog){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setContentView(R.layout.dialog_progress_bar)
        message?.let {
            dialog_progress_bar_progress_message.text = it
        }
        dialog_progress_bar_progress_bar.start()
        1
    }
}