package com.example.githubrepoviewer.ui.auth

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.githubrepoviewer.R

class AlertDialogFragment : DialogFragment() {

    private val title: String by lazy { arguments?.getString(ARG_TITLE) ?: "" }
    private val message: String by lazy { arguments?.getString(ARG_MESSAGE) ?: "" }
    private var overlayView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create().apply {
                setCanceledOnTouchOutside(false)
            }
    }

    override fun onStart() {
        super.onStart()

        overlayView = View(requireContext()).apply {
            setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.semi_transparent_white
                )
            )
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        (requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .addView(overlayView, layoutParams)
    }

    override fun onStop() {
        super.onStop()
        (requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .removeView(overlayView)
        overlayView = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"

        fun newInstance(title: String, message: String): AlertDialogFragment {
            val fragment = AlertDialogFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }
}