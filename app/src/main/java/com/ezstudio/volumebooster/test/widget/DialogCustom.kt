package com.ezstudio.volumebooster.test.widget

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import com.ezstudio.volumebooster.test.utils.EqualizerUtils

open class DialogCustom(context: Context, style: Int) : Dialog(context, style) {
    override fun show() {
        // Set the dialog to not focusable.
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        window?.decorView?.systemUiVisibility = EqualizerUtils.flags
        // Show the dialog with NavBar hidden.
        super.show()
        // Set the dialog to focusable again.
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }
}