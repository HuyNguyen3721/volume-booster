package com.ezstudio.volumebooster.test.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ezstudio.volumebooster.test.item.ItemSong
import com.ezteam.baseproject.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class MusicActiveViewModel(application: Application) : BaseViewModel(application) {

    var isMusicActive: MutableLiveData<Boolean> = MutableLiveData()

    // music
    var nameMusicSong: MutableLiveData<String> = MutableLiveData()
    var nameAuthorSong: MutableLiveData<String> = MutableLiveData()
    var imgSong: MutableLiveData<Bitmap?> = MutableLiveData()
    var listItemSong: MutableLiveData<MutableList<ItemSong>?> = MutableLiveData()
//    var isDestroyMusicView: MutableLiveData<Boolean> = MutableLiveData()

    // control music
    var actionNext: MutableLiveData<Boolean> = MutableLiveData()
    var actionBack: MutableLiveData<Boolean> = MutableLiveData()

    // changeVolume or seekbar
    var isChangeSound: MutableLiveData<Boolean> = MutableLiveData()
    var isCustom: MutableLiveData<Boolean> = MutableLiveData()
    var percentValue: MutableLiveData<Int> = MutableLiveData()

    fun getDataMusicOffline(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (result == PackageManager.PERMISSION_GRANTED) {
                val list = mutableListOf<ItemSong>()
                //        uri dia chi cua bang
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                uri?.let {
                    val cursor = context.contentResolver
                        ?.query(uri, null, null, null, null)
                    cursor?.let {
                        cursor.moveToFirst()
                        var a = 0
                        while (!cursor.isAfterLast) {
                            a++
                            val path = cursor.getString(
                                cursor.getColumnIndex("_data")
                            )
                            var arrName: List<String>? = null
                            path?.let {
                                arrName = path.split("/")
                            }
                            val name = arrName?.get((arrName!!.size - 1)) ?: "UnKnown"
                            //
                            var art: Bitmap? = null
                            var artist: String? = null
                            var rawArt: ByteArray? = null
                            try {
                                val mmr = MediaMetadataRetriever()
                                val file = File(path)
                                if (file.length() > 0 && file.exists()) {
                                    mmr.setDataSource(context, Uri.parse(path))
                                    artist =
                                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                    rawArt = try {
                                        mmr.embeddedPicture
                                    } catch (e: OutOfMemoryError) {
                                        null
                                    }
                                }
                                if (rawArt != null) {
                                    try {
                                        art = BitmapFactory.decodeByteArray(
                                            rawArt,
                                            0,
                                            rawArt.size,
                                            BitmapFactory.Options()
                                        )
                                    } catch (e: OutOfMemoryError) {
                                        art = null
                                    }
                                }

                            } catch (e: FileNotFoundException) {
                            } catch (e: IOException) {
                            } catch (e: RuntimeException) {
                            } catch (e: Exception) {
                            }
//                        listItemSong.postValue(ItemSong(art, name, artist ?: "Unknown", path))
                            list.add(ItemSong(art, name, artist ?: "Unknown", path))
                            cursor.moveToNext()
                        }
                        cursor.close()
                        listItemSong.postValue(list)
                    }
                }
            }
        }

    }
}