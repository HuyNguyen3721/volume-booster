package com.ezstudio.volumebooster.test.utils

import androidx.fragment.app.FragmentActivity
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.dialog.DialogPremium
import com.google.android.gms.ads.ez.IAPUtils

object PremiumUtils {
    const val TIME_SHOW_ADS = "TIME_SHOW_ADS"
    fun showDialogPremium(activity: FragmentActivity) {
        val dialogPremium = DialogPremium(activity, R.style.StyleDialogPremium)
        dialogPremium.onClickMonth = {
            IAPUtils.getInstance()
                .callSubcriptions(activity, IAPUtils.KEY_PREMIUM_ONE_MONTH)
        }
        dialogPremium.onClickSixMonth = {
            IAPUtils.getInstance()
                .callSubcriptions(activity, IAPUtils.KEY_PREMIUM_SIX_MONTHS)
        }
        dialogPremium.onClickYear = {
            IAPUtils.getInstance()
                .callSubcriptions(activity, IAPUtils.KEY_PREMIUM_ONE_YEAR)
        }
        dialogPremium.show()
    }
}