package com.ezstudio.volumebooster.test.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.ezstudio.volumebooster.test.R

class WaveSound(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    var value = 0

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.wave)
        value = typedArray.getInt(R.styleable.wave_value, 0)
        //
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        val sp4 = context.resources.getDimensionPixelSize(R.dimen._3sdp)
        val sp10 = context.resources.getDimensionPixelSize(R.dimen._10sdp)
        val widthRec = context.resources.getDimensionPixelSize(R.dimen._11sdp)
        val heightRec = (height - 22 * sp4 - sp10) / 23

        val paint = Paint()
        var left =
            (width / 2) - widthRec// initial start position of rectangles (50 pixels from left)
        var top = 10 // // 50 pixels from the top

        for (row in 0..22) { // draw 2 rows
            when (row) {
                in 0..1 -> {
                    if (value != 0) {
                        when (value) {
                            in -70..45 -> {
                                paint.color = Color.parseColor("#33FB0000")
                            }
                            else -> {
                                paint.color = Color.parseColor("#FB0000")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#33FB0000")
                    }
                }
                in 2..3 -> {
                    if (value != 0) {
                        when (value) {
                            in -50..35 -> {
                                paint.color = Color.parseColor("#33FB0000")
                            }
                            else -> {
                                paint.color = Color.parseColor("#FB0000")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#33FB0000")
                    }
                }
                in 4..5 -> {
                    if (value != 0) {
                        when (value) {
                            in -35..30 -> {
                                paint.color = Color.parseColor("#33FB0000")
                            }
                            else -> {
                                paint.color = Color.parseColor("#FB0000")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#33FB0000")
                    }
                }
                //
                in 6..7 -> {
                    if (value != 0) {
                        when (value) {
                            in -30..25 -> {
                                paint.color = Color.parseColor("#33FD4C00")
                            }
                            else -> {
                                paint.color = Color.parseColor("#FD4C00")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#33FD4C00")
                    }
                }
                8 -> {
                    if (value != 0) {
                        when (value) {
                            in -22..20 -> {
                                paint.color = Color.parseColor("#33FD4C00")
                            }
                            else -> {
                                paint.color = Color.parseColor("#FD4C00")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#33FD4C00")
                    }
                }
                //
                in 9..10 -> {
                    if (value != 0) {
                        when (value) {
                            in -18..18 -> {
                                paint.color = Color.parseColor("#33FBC400")
                            }
                            else -> {
                                paint.color = Color.parseColor("#FBC400")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#33FBC400")
                    }
                }
                11 -> {
                    if (value != 0) {
                        when (value) {
                            in -15..15 -> {
                                paint.color = Color.parseColor("#33FBC400")
                            }
                            else -> {
                                paint.color = Color.parseColor("#FBC400")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#33FBC400")
                    }
                }
                //
                in 12..13 -> {
                    if (value != 0) {
                        when (value) {
                            in -12..12 -> {
                                paint.color = Color.parseColor("#33EEF300")
                            }
                            else -> {
                                paint.color = Color.parseColor("#EEF300")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#33EEF300")
                    }
                }
                14 -> {
                    if (value != 0) {
                        when (value) {
                            in -10..10 -> {
                                paint.color = Color.parseColor("#33EEF300")
                            }
                            else -> {
                                paint.color = Color.parseColor("#EEF300")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#33EEF300")
                    }
                }
                //
                in 15..16 -> {
                    if (value != 0) {
                        when (value) {
                            in -8..8 -> {
                                paint.color = Color.parseColor("#3380FF00")
                            }
                            else -> {
                                paint.color = Color.parseColor("#80FF00")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#3380FF00")
                    }
                }
                17 -> {
                    if (value != 0) {
                        when (value) {
                            in -5..7 -> {
                                paint.color = Color.parseColor("#3380FF00")
                            }
                            else -> {
                                paint.color = Color.parseColor("#80FF00")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#3380FF00")
                    }
                }
                //
                in 18..19 -> {
                    if (value != 0) {
                        when (value) {
                            in -3..5 -> {
                                paint.color = Color.parseColor("#3305F200")
                            }
                            else -> {
                                paint.color = Color.parseColor("#05F200")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#3305F200")
                    }
                }
                20 -> {
                    if (value != 0) {
                        when (value) {
                            in -2..3 -> {
                                paint.color = Color.parseColor("#3305F200")
                            }
                            else -> {
                                paint.color = Color.parseColor("#05F200")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#3305F200")
                    }
                }
                21 -> {
                    if (value != 0) {
                        when (value) {
                            in -1..2 -> {
                                paint.color = Color.parseColor("#3305F200")
                            }
                            else -> {
                                paint.color = Color.parseColor("#05F200")
                            }
                        }
                    } else {
                        paint.color = Color.parseColor("#3305F200")
                    }
                }
                22 -> {
                    if (value != 0) {
                        paint.color = Color.parseColor("#05F200")
                    } else {
                        paint.color = Color.parseColor("#3305F200")
                    }
                }
            }
            for (col in 0..1) { // draw 2 columns
                canvas!!.drawRect(
                    left.toFloat(), top.toFloat(), (left + widthRec).toFloat(),
                    (top + heightRec).toFloat(), paint
                )
                left += widthRec + sp4 // set new left co-ordinate + 10 pixel gap
                // Do other things here
                // i.e. change colour
            }
            left = (width / 2) - widthRec
            top += heightRec + sp4 // move to new row by changing the top co-ordinate
        }
    }
}