package com.example.runningapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runningapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog : DialogFragment() {

    private var yesListener: (() -> Unit)? = null

    fun setListener(listener:() -> Unit) {
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the run?")
            .setMessage("Are you sure to cancel the current run and delete all it's data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
//                stopRun()
                yesListener?.let { yes ->
                    yes.invoke()
                }
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        /** doesn't need to invoke show() method inside separate class. only in fragment as func invoke */
//        dialog.show()
        return dialog
    }
}