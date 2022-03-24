package com.ezstudio.volumebooster.test.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.adapter.AdapterEqualizer
import com.ezstudio.volumebooster.test.databinding.LayoutDialogDeleteBinding
import com.ezstudio.volumebooster.test.databinding.LayoutDialogEqualizerBinding
import com.ezstudio.volumebooster.test.item.ItemEqualizer
import com.ezstudio.volumebooster.test.utils.EqualizerUtils
import com.ezstudio.volumebooster.test.utils.KeyEqualizer
import com.ezstudio.volumebooster.test.widget.DialogCustom
import com.ezteam.baseproject.utils.PreferencesUtils


class DialogEqualizer(
    context: Context, var style: Int
) : DialogCustom(context, style) {
    private var adapterEqualizer: AdapterEqualizer? = null
    private lateinit var binding: LayoutDialogEqualizerBinding
    private val listEqualizer = mutableListOf<ItemEqualizer>()
    private var dialogRenameEqualizer: DialogRenameEqualizer? = null
    private var dialogDeleteEqualizer: DialogDeleteEqualizer? = null
    var onClickItem: ((ItemEqualizer) -> Unit)? = null
    var onDismiss: (() -> Unit)? = null
    var onFocus: (() -> Unit)? = null
    var onEdited: ((String) -> Unit)? = null
    var onDeleted: (() -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setCancelable(true)
        initData()
        initView()
        initListener()
    }

    private fun initListener() {
        binding.layoutDialog.setOnClickListener {
            dismiss()
        }
        adapterEqualizer?.onClickListener = {
            PreferencesUtils.putString(KeyEqualizer.KEY_EQUALIZER_NAME, listEqualizer[it].name)
            onClickItem?.invoke(listEqualizer[it])
            dismiss()
        }
        adapterEqualizer?.onClickDeleteListener = {
            val bindDialog =
                LayoutDialogDeleteBinding.inflate(LayoutInflater.from(context))
            dialogDeleteEqualizer = DialogDeleteEqualizer(context, R.style.StyleDialog, bindDialog)
            dialogDeleteEqualizer?.listenerYes = {
                val equalizer = listEqualizer[it]
                val nameCustom = context.getString(R.string.custom)
                if (equalizer.name == EqualizerUtils.getNameEqualizerSave(context)) {
                    var check = false
                    for (item in listEqualizer) {
                        if (item.name == nameCustom) {
                            check = true
                            PreferencesUtils.putString(
                                KeyEqualizer.KEY_EQUALIZER_NAME,
                                item.name
                            )
                            EqualizerUtils.deleteItem(equalizer.name)
                            onClickItem?.invoke(item)
                            dismiss()
                            break
                        }
                    }
                    //
                    if (!check) {
                        EqualizerUtils.editItem(listEqualizer[it].name, nameCustom)
                        PreferencesUtils.putString(KeyEqualizer.KEY_EQUALIZER_NAME, nameCustom)
                        dismiss()
                    }
                } else {
                    EqualizerUtils.deleteItem(equalizer.name)
                    listEqualizer.removeAt(it)
                    adapterEqualizer?.notifyItemRemoved(it)
                }
                Toast.makeText(context, context.getString(R.string.deleted), Toast.LENGTH_SHORT)
                    .show()
                //call back
                onDeleted?.invoke()
                dialogDeleteEqualizer?.dismiss()
            }
            dialogDeleteEqualizer?.listenerNo = {
                dialogDeleteEqualizer?.dismiss()
            }
            dialogDeleteEqualizer?.setOnDismissListener {
//            super.onBackPressed()
            }
            dialogDeleteEqualizer?.show()

        }
        // edit
        adapterEqualizer?.onClickEditListener = {
            onFocus?.invoke()
            dialogRenameEqualizer =
                DialogRenameEqualizer(context, R.style.StyleDialog, listEqualizer[it].name)
            dialogRenameEqualizer?.show()
            dialogRenameEqualizer?.listenerYes = { newName ->
                EqualizerUtils.editItem(listEqualizer[it].name, newName)
                //
                val nameSelected = PreferencesUtils.getString(
                    KeyEqualizer.KEY_EQUALIZER_NAME,
                    context.getString(R.string.normal)
                )
                if (nameSelected == listEqualizer[it].name) {
                    PreferencesUtils.putString(KeyEqualizer.KEY_EQUALIZER_NAME, newName)
                }
                //
                listEqualizer[it].name = newName
                adapterEqualizer?.notifyItemChanged(it)
                //
                Toast.makeText(context, context.getString(R.string.edited), Toast.LENGTH_SHORT)
                    .show()
                //
                onEdited?.invoke(newName)
            }
        }
        dialogRenameEqualizer?.setOnDismissListener {
            onDismiss?.invoke()
        }
    }

    private fun initView() {
        //
        binding = LayoutDialogEqualizerBinding.inflate(LayoutInflater.from(context))
        adapterEqualizer = AdapterEqualizer(context, listEqualizer)
        binding.rcl.adapter = adapterEqualizer
        //
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
    }

    private fun initData() {
        listEqualizer.clear()
        listEqualizer.addAll(EqualizerUtils.getListEqualizer(context))
    }



}