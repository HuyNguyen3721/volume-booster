package com.ezstudio.volumebooster.test.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import com.ezstudio.volumebooster.test.databinding.LayoutDialogRenameEqualizerBinding
import com.ezstudio.volumebooster.test.utils.EqualizerUtils
import com.ezstudio.volumebooster.test.widget.DialogCustom

class DialogRenameEqualizer(context: Context, style: Int, var name: String) :
    DialogCustom(context, style) {
    private lateinit var binding: LayoutDialogRenameEqualizerBinding
    var listenerYes: ((String) -> Unit)? = null
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
        binding = LayoutDialogRenameEqualizerBinding.inflate(LayoutInflater.from(context))
        binding.edtInput.setText(name)
        binding.edtInput.selectAll()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
    }

    private fun initListener() {
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvOk.setOnClickListener {
            listenerYes?.invoke(binding.edtInput.text.toString())
            dismiss()
        }

    }


}