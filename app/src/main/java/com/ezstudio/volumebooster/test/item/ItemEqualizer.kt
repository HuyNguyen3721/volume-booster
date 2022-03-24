package com.ezstudio.volumebooster.test.item

data class ItemEqualizer(
    var name: String,
    var value60Hz : Int  = 0,
    var value230Hz : Int  = 0,
    var value910Hz : Int  = 0,
    var value3kHz : Int  = 0,
    var value14kHz : Int  = 0,
    var isSelected: Boolean = false,
    var isCustom: Boolean = false
)