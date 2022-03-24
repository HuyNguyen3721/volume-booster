package com.ezstudio.volumebooster.test.utils

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.item.ItemCustomEqualizer
import com.ezstudio.volumebooster.test.item.ItemEqualizer
import com.ezteam.baseproject.utils.PreferencesUtils
import com.orhanobut.hawk.Hawk

object EqualizerUtils {

    val flags: Int = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    private val listEqualizer = mutableListOf<ItemEqualizer>()

    fun getListEqualizer(context: Context): MutableList<ItemEqualizer> {
        listEqualizer.clear()
        val nameSelected = PreferencesUtils.getString(
            KeyEqualizer.KEY_EQUALIZER_NAME,
            context.getString(R.string.normal)
        )
        listEqualizer.apply {
            add(ItemEqualizer(context.getString(R.string.normal), 1800, 1500, 1500, 1500, 1800))
            add(ItemEqualizer(context.getString(R.string.vintage), 2100, 1500, 1900, 1700, 700))
            add(ItemEqualizer(context.getString(R.string.dance), 2100, 1500, 1700, 1300, 1600))
            add(ItemEqualizer(context.getString(R.string.hiphop), 2000, 1800, 1500, 1600, 1800))
            add(ItemEqualizer(context.getString(R.string.jazz), 1900, 1700, 1300, 1700, 2000))
            add(ItemEqualizer(context.getString(R.string.pop), 1400, 1700, 2000, 1600, 1300))
            add(ItemEqualizer(context.getString(R.string.r_and_B), 2600, 1600, 1200, 1900, 2100))
            add(ItemEqualizer(context.getString(R.string.latin), 2000, 1500, 1500, 700, 1900))
            add(ItemEqualizer(context.getString(R.string.piono), 1600, 1800, 1600, 2100, 1900))
            add(ItemEqualizer(context.getString(R.string.edm), 2100, 1500, 1700, 1600, 2100))
            add(ItemEqualizer(context.getString(R.string.room), 3000, 3000, 3000, 0, 3000))
        }
        val listCustom =
            Hawk.get(KeyEqualizer.KEY_EQUALIZER_CUSTOM, mutableListOf<ItemCustomEqualizer>())
        if (listCustom.isNotEmpty()) {
            for (custom in listCustom) {
                listEqualizer.add(
                    ItemEqualizer(
                        custom.name,
                        custom.listValues[0],
                        custom.listValues[1],
                        custom.listValues[2],
                        custom.listValues[3],
                        custom.listValues[4],
                        isSelected = false,
                        custom.name != context.getString(R.string.custom)
                    )
                )
            }
        }
        listEqualizer.forEach {
            it.isSelected = it.name == nameSelected
        }
        return listEqualizer
    }

    fun getEqualizerSave(context: Context): ItemEqualizer {
        val nameSave = getNameEqualizerSave(context)
        for (item in listEqualizer) {
            if (item.name == nameSave) {
                return item
            }
        }
        return ItemEqualizer(context.getString(R.string.normal), 1800, 1500, 1500, 1500, 1800)
    }

    fun getNameEqualizerSave(context: Context): String {
        return PreferencesUtils.getString(
            KeyEqualizer.KEY_EQUALIZER_NAME,
            context.getString(R.string.normal)
        )
    }


    fun deleteItem(name: String) {
        val listCustom =
            Hawk.get(KeyEqualizer.KEY_EQUALIZER_CUSTOM, mutableListOf<ItemCustomEqualizer>())
        for (item in listCustom) {
            if (item.name == name) {
                listCustom.remove(item)
                break
            }
        }
        Hawk.put(KeyEqualizer.KEY_EQUALIZER_CUSTOM, listCustom)
    }

    fun editItem(nameOld: String, nameNew: String) {
        val listCustom =
            Hawk.get(KeyEqualizer.KEY_EQUALIZER_CUSTOM, mutableListOf<ItemCustomEqualizer>())
        for (item in listCustom) {
            if (item.name == nameOld) {
                item.name = nameNew
                break
            }
        }
        Hawk.put(KeyEqualizer.KEY_EQUALIZER_CUSTOM, listCustom)
    }

    fun hideNavigation(activity: FragmentActivity) {
        activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}