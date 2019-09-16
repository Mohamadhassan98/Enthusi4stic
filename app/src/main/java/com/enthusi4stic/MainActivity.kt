package com.enthusi4stic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.enthusi4stic.api.progressdialog.CircularProgressBarDialog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CircularProgressBarDialog(this, "Please wait...").show()

    }
}
