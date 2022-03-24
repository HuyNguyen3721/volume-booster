package com.ezstudio.volumebooster.test.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.databinding.LayoutDialogPremiumBinding
import com.ezstudio.volumebooster.test.widget.DialogCustom
import com.google.android.gms.ads.ez.IAPUtils

class DialogPremium(context: Context, var style: Int) : DialogCustom(context, style) {

    private val binding by lazy {
        LayoutDialogPremiumBinding.inflate(LayoutInflater.from(context))
    }

    var onClickMonth: (() -> Unit)? = null
    var onClickSixMonth: (() -> Unit)? = null
    var onClickYear: (() -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        this.setCancelable(true)
        initData()
        initView()
        initListener()
    }

    private fun initView() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //
        convertHtmlPrice(IAPUtils.KEY_PREMIUM_ONE_MONTH)?.let {
            binding.txtOneMonth.text = Html.fromHtml(it)
            binding.oneMonth.visibility = View.VISIBLE
        }
        convertHtmlPrice(IAPUtils.KEY_PREMIUM_SIX_MONTHS)?.let {
            binding.txtSixMonth.text = Html.fromHtml(it)
            binding.sixMonth.visibility = View.VISIBLE
        }
        convertHtmlPrice(IAPUtils.KEY_PREMIUM_ONE_YEAR)?.let {
            binding.txtOneYear.text = Html.fromHtml(it)
            binding.oneYear.visibility = View.VISIBLE
        }
    }

    private fun initListener() {
        binding.oneMonth.setOnClickListener {
            binding.oneMonth.animate().scaleX(0.92F).scaleY(0.92F).setDuration(100)
                .withEndAction {
                    binding.oneMonth.animate().scaleX(01F).scaleY(1F).duration = 100
                    onClickMonth?.invoke()
                    dismiss()
                }
        }
        binding.sixMonth.setOnClickListener {
            binding.sixMonth.animate().scaleX(0.92F).scaleY(0.92F).setDuration(100)
                .withEndAction {
                    binding.sixMonth.animate().scaleX(01F).scaleY(1F).duration = 100
                    onClickSixMonth?.invoke()
                    dismiss()
                }
        }
        binding.oneYear.setOnClickListener {
            binding.oneYear.animate().scaleX(0.92F).scaleY(0.92F).setDuration(100)
                .withEndAction {
                    binding.oneYear.animate().scaleX(01F).scaleY(1F).duration = 100
                    onClickYear?.invoke()
                    dismiss()
                }
        }
        binding.icClose.setOnClickListener {
            dismiss()
        }
    }

    private fun initData() {

    }

    private fun convertSubscriptionPeriod(subscriptionPeriod: String): String {
        return if (subscriptionPeriod.length == 3) {
            try {
                Log.e("Period", subscriptionPeriod)
                val time = subscriptionPeriod[1].toString()
                "$time " + when (subscriptionPeriod[2].toString()) {
                    "D" -> if ("1" == time) {
                        context.getString(R.string.day)
                    } else {
                        context.getString(R.string.days)
                    }
                    "W" -> if ("1" == time) {
                        context.getString(R.string.week)
                    } else {
                        context.getString(R.string.weeks)
                    }
                    "M" -> if ("1" == time) {
                        context.getString(R.string.month)
                    } else {
                        context.getString(R.string.months)
                    }
                    "Y" -> if ("1" == time) {
                        context.getString(R.string.year)
                    } else {
                        context.getString(R.string.years)
                    }
                    else -> ""
                }
            } catch (ex: Exception) {
                ""
            }

        } else {
            ""
        }
    }

    private fun convertHtmlPrice(keyPremium: String): String? {
        IAPUtils.getInstance().getSubcriptionById(keyPremium)?.let {
            return "<font color=#ffffff>" + "<b>" + "<big>" + it.price + "</big>" + "</b>" + "<small>" +
                    "/${convertSubscriptionPeriod(it.subscriptionPeriod)}" + "</small>"
        }
        return null
    }


}