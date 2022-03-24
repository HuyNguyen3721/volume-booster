package com.ezstudio.volumebooster.test.item

import android.graphics.Bitmap
import android.view.ViewGroup

data class ItemSong(
    var resId: Bitmap?,
    var songName: String?,
    var singer: String?,
    var path: String?,
    var isSelected: Boolean = false,
    var ads: ViewGroup? = null
)
