package com.refo.cottontails

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import com.google.android.material.snackbar.Snackbar
import com.refo.cottontails.databinding.CustomSnackBarBinding

fun View.ShowSnackBar(message:String,type:Int,view:View){
    val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)

    // Mengambil tampilan Snackbar
    val snackbarView = snackbar.view

    // Menyesuaikan layout params berdasarkan jenis tampilan
    val layoutParams = snackbarView.layoutParams
    if (layoutParams is CoordinatorLayout.LayoutParams) {
        layoutParams.gravity = Gravity.BOTTOM
    } else if (layoutParams is FrameLayout.LayoutParams) {
        layoutParams.gravity = Gravity.BOTTOM
    }

    snackbarView.layoutParams = layoutParams

    val snackBarLayoutBinding = snackbarView as? Snackbar.SnackbarLayout
    snackBarLayoutBinding?.setPadding(0, 0, 0, 0)

    when (type) {
        100 -> {
            val binding = CustomSnackBarBinding.inflate(LayoutInflater.from(view.context))
            binding.textViewMessage.text = message
            snackBarLayoutBinding?.addView(binding.root)
        }
    }

    snackbar.show()
}