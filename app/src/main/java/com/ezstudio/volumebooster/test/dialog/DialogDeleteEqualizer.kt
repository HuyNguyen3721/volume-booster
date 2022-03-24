package com.ezstudio.volumebooster.test.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import com.ezstudio.volumebooster.test.databinding.LayoutDialogDeleteBinding
import com.ezstudio.volumebooster.test.utils.EqualizerUtils
import com.ezstudio.volumebooster.test.widget.DialogCustom


class DialogDeleteEqualizer(context: Context, style: Int, var binding: LayoutDialogDeleteBinding) :
    DialogCustom(context, style) {
    var listenerNo: (() -> Unit)? = null
    var listenerYes: (() -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setCancelable(true)
        initData()
        initView()
        initListener()
    }

    private fun initData() {

    }

    private fun initView() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
    }

    private fun initListener() {
        binding.btnNo.setOnClickListener {
            listenerNo?.invoke()
        }
        binding.btnYes.setOnClickListener {
            listenerYes?.invoke()
        }

    }


}