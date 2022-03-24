package com.ezstudio.volumebooster.test.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import com.ezstudio.volumebooster.test.databinding.LayoutDialogSaveNameEqualizerBinding
import com.ezstudio.volumebooster.test.utils.KeyEqualizer
import com.ezstudio.volumebooster.test.widget.DialogCustom
import com.ezteam.baseproject.utils.PreferencesUtils

class DialogSaveNameEqualizer(
    context: Context,
    style: Int
) : DialogCustom(context, style) {
    private lateinit var binding: LayoutDialogSaveNameEqualizerBinding
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
        binding = LayoutDialogSaveNameEqualizerBinding.inflate(LayoutInflater.from(context))
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
    }

    private fun initListener() {
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvOk.setOnClickListener {
            val name = binding.edtInput.text.toString()
            PreferencesUtils.putString(KeyEqualizer.KEY_EQUALIZER_NAME, name)
            listenerYes?.invoke(name)
        }
    }

}