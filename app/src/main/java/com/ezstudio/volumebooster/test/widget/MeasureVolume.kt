package com.ezstudio.volumebooster.test.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.utils.VibrationUtil
import com.ezteam.baseproject.extensions.scaleBitmap
import com.ezteam.baseproject.utils.PreferencesUtils
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


@SuppressLint("CustomViewStyleable", "Recycle")
class MeasureVolume(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    var isEnable = true
    private var mRadius = 0
    private var mPaint = Paint()
    private var mHandTruncation = 0
    private var canvasRotate: Canvas? = null
    var rotate = 0F
    var color = 0
    private var r = 0F
    private var xA = 0F
    private var yA = 0F


    private var xD = 0F
    private var yD = 0F

    //
    private var xM = 0F
    private var yM = 0F

    //
    private var rotateTouch = 0F
    private var rotateMove = 0F
    private var rotateOld = 0F
    private var rotateStop = 0F

    //
    private var isTouchVolume = false
    private var isVisibleMaxMin: Boolean = false
    var isInvalidate = true

    // listener
    var onTouch: ((Boolean) -> Unit)? = null
    var onPercent: ((Float) -> Unit)? = null


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ViewVolume)
        isVisibleMaxMin = typedArray.getBoolean(R.styleable.ViewVolume_maxmin, true)
        rotate = typedArray.getFloat(R.styleable.ViewVolume_rotate, 240F)
        color = typedArray.getInt(R.styleable.ViewVolume_color_volume, Color.parseColor("#FF9500"))
        //
    }

    @SuppressLint("DrawAllocation", "UseCompatLoadingForDrawables")
    override fun onDraw(canvas: Canvas?) {
        //
        mPaint.reset()
        val minAttr = height.coerceAtMost(width)
        mRadius = minAttr / 2
        // for maintaining different heights among the clock-hands
        mHandTruncation = minAttr / 23
        mPaint.strokeWidth = 7F // set font size (optional)
        r = width / 2F
        // set x y min max
        drawHandLine(0.0)
        //
        yD = getTouchYMin()
        //
        // draw line volume
        drawDegrees(canvas)
        // line cycle full : color :#black
        drawLineCycle(canvas, true)
        // line cycle : color :#FF9500
        drawLineCycle(canvas, false)
        // draw pointer
//        drawPointer(canvas)
        // shadow
        BitmapFactory.decodeResource(resources, R.drawable.bg_shadow)
            ?.scaleBitmap(width * 0.9F, height * 0.9F)
            ?.let {
                canvas!!.drawBitmap(
                    it,
                    width / 2F - (it.width / 2F),
                    height / 2F - it.height / 2.48F,
                    null
                )
                canvasRotate = canvas
            }
        //  volume bitmap

        context.getDrawable(
            try {
                if (Build.VERSION.SDK_INT >= 24) R.drawable.ic_rotion else R.drawable.ic_rotion_2
            } catch (e: OutOfMemoryError) {
                R.drawable.ic_rotion_3
            }
        )
            ?.toBitmap()
            ?.scaleBitmap(width * 0.67F, height * 0.67F)
            ?.let {
                val rotator = Matrix()
                rotator.reset()
                rotator.postRotate(rotate, it.width / 2F, it.height / 2F)
                rotator.postTranslate(width / 2F - (it.width / 2F), height / 2F - it.height / 2F)
                canvas!!.drawBitmap(
                    it,
                    rotator, null
                )
                if (rotate in 240F..360F) {
                    onPercent?.invoke(rotate - 240)
                } else {
                    onPercent?.invoke(rotate + 120)
                }
                canvasRotate = canvas
            }
        //max min view
        if (isVisibleMaxMin) {
            context.getDrawable(R.drawable.ic_minus)?.toBitmap()
                ?.let {
                    canvas!!.drawBitmap(it, width / 2F - r * 0.69F, height / 1.27F, null)
                    canvasRotate = canvas
                }
            context.getDrawable(R.drawable.ic_plus)?.toBitmap()
                ?.let {
                    canvas!!.drawBitmap(it, width / 2F + r * 0.5F, height / 1.27F, null)

                    canvasRotate = canvas
                }
        }
    }

//    private fun drawPointer(canvas: Canvas?) {
//        // tìm điểm B : nơi bắt đầu vẽ pointer
//        var ab = 0F
//        if (rotate !in 120.0001..239.9999) {
//            if (rotate > 0 && rotate <= 180) {
//                Log.d("Huy", "roate: $rotate  , $xA , $yA")
//                ab = sqrt(2 * r.pow(2F) * (1 - cos(rotate)))
//            } else {
//                Log.d("Huy", "roate: ${360F - rotate}, $xA , $yA ")
//                ab = sqrt(2 * r.pow(2F) * (1 - cos(360F - rotate)))
//            }
//        }
//        // denta
//        val D = (ab.pow(2F) - xA.pow(2) - yA.pow(2F) - r.pow(2F))
//        val denta =
//            (2 * r - 2 * xA).pow(2F) - 4 * 2 * D
//        Log.d("Huy", "ab: $ab")
////        Log.d("Huy", "denta: $denta")
//        val xB1 = -(2 * r - 2 * xA) + sqrt((2 * r - 2 * xA).pow(2F) - 4 * 2 * D)
////        Log.d("Huy", " xB1: $xB1")
//    }


    private fun drawLineCycle(canvas: Canvas?, isFullCycle: Boolean = false) {
        val rEnd = r - (width * 0.095)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8F
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = color
        //
        val startTop = (r + rEnd * cos(Math.toRadians(180.toDouble())))

        val endBottom = 2 * mRadius - startTop

        val mRect = RectF(
            startTop.toFloat(), startTop.toFloat(), endBottom.toFloat(),
            endBottom.toFloat()
        )
        // startAngle : goc bat dau ve
        // sweepAngle  : góc quay
        if (isFullCycle) {
            paint.color = Color.parseColor("#181818")
            canvas?.drawArc(mRect, 150F, 240F, false, paint)
        } else {
            paint.color = color
            if (rotate !in 120.0001..239.9999) {
                if (rotate > 0 && rotate < 180) {
                    canvas?.drawArc(mRect, 150F, 120F + rotate, false, paint)
                } else {
                    canvas?.drawArc(mRect, 150F, rotate - 240F, false, paint)
                }
            }
        }

    }

    private fun drawHandLine(moment: Double) {
        val angle = Math.PI * moment / 30 - Math.PI / 2
        var handRadius: Int = 0
        handRadius = mRadius

        val stopX = (width / 2 + cos(angle) * handRadius).toFloat()
        val stopY = (height / 2 + sin(angle) * handRadius).toFloat()
        if (moment == 0.0) {
            xA = stopX
            yA = stopY
        }
        //
    }

    private fun getTouchYMin(): Float {
        val angle = Math.PI * 40 / 30 - Math.PI / 2
        val handRadius: Int = mRadius
        return (height / 2 + sin(angle) * handRadius).toFloat()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnable) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    // giữ giá trị góc lúc bắt đầu touch để tính trong  quá trình move
                    rotateOld = rotate
                    // get góc khi bắt đầu touch
                    var isDownMin = false
                    isTouchVolume = true
                    onTouch?.invoke(true)
                    isDownMin = event.x <= r
                    //
                    getRotateTouch(event.x, event.y, isDownMin)
                }
                MotionEvent.ACTION_UP -> {
                    onTouch?.invoke(false)
                }
                MotionEvent.ACTION_MOVE -> {
                    var isDownMin = false
//                    if (event.y <= yD) {
                    // check kéo trong vòng volume ( phạm vi hình tròn)
                    if (isTouchVolume) {
                        // tinh góc khi move
                        isDownMin = event.x <= r
                        rotateMove = getRotate(event.x, event.y, isDownMin)
                        //  tính góc di chuyển sau khi move từ lúc touch volume ( move  -touch )
                        if (rotateTouch >= 180 && rotateMove < 180) {
                            rotateMove += 360
                        } else if (rotateTouch <= 180 && rotateMove > 180) {
                            rotateMove = (360 - rotateMove) * -1
                        }
                        // góc di chuyển đc  cộng vào góc hiện tại đang quay của volume
                        rotate = rotateOld + (rotateMove - rotateTouch)
                        // check khi quay hết 1 vòng hoặc ngước hết 1 vòng
                        if (rotate > 360.0) {
                            rotate -= 360
                        } else if (rotate < 0) {
                            rotate += 360
                        }
                        // góc quay từ 240 -> 120 thì invalidate xoay
                        if (rotate !in 120.0001..239.9999) {
                            if (PreferencesUtils.getBoolean(VibrationUtil.KEY_VIBRATION, true)) {
                                VibrationUtil.startVibration(context)
                            }
                            isInvalidate = true
                            invalidate()
                        } else {
                            if (isInvalidate) {
                                if (rotate in 120.0..180.0) {
                                    rotate = 120F
                                    rotateStop = 120F
                                    //
                                } else if (rotate in 180.0..240.0) {
                                    rotate = 240F
                                    rotateStop = 240F
                                }
                                invalidate()
                                isInvalidate = false
                            } else {
                                rotate = rotateStop
                                invalidate()
                            }
                            // khi tới góc quay max hoặc min  , ko nhận góc di chuyển giữ  move và touch nữa (cho touch = move )
                            // set lại touch = move , góc di chuyển = 0
                            rotateOld = rotate
                            getRotateTouch(event.x, event.y, isDownMin)
                            //
                        }
                    } else {
                        isTouchVolume = true
                    }

//                    } else {
//                        isTouchVolume = false
//                    }
                }
            }
        }
        return true
    }

    private fun drawDegrees(canvas: Canvas?) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 7F
        var rPadded = 0.0
        var rEnd = 0.0

        for (i in 0..359 step 6) {
            if (i !in 211..329) {
                if (i % 90 != 0 && i % 15 != 0) {
                    rEnd = r - (width * 0.07)
                    rPadded = r - (width * 0.05)
                    paint.color = Color.parseColor("#33949392")
                } else {
                    rEnd = r - (width * 0.095)
                    rPadded = r - (width * 0.05)
                    paint.color = Color.parseColor("#949392")
                }
                //
                val startX = (r + rPadded * cos(Math.toRadians(i.toDouble())))
                val startY = (r - rPadded * sin(Math.toRadians(i.toDouble())))
                val stopX = (r + rEnd * cos(Math.toRadians(i.toDouble())))
                val stopY = (r - rEnd * sin(Math.toRadians(i.toDouble())))
                canvas?.drawLine(
                    startX.toFloat(),
                    startY.toFloat(),
                    stopX.toFloat(),
                    stopY.toFloat(),
                    paint
                )
            }

        }

    }

    private fun getRotate(x: Float, y: Float, isDownMin: Boolean): Float {
        var rotate = 0F
        // tinh độ dài các đoạn thẳng
        val ab = sqrt((x - xA).toDouble().pow(2.0) + (y - yA).toDouble().pow(2.0))
        val ob = sqrt(
            (x - r).toDouble().pow(2.0) + (y - r).toDouble().pow(2.0)
        )
        val oa = r

        val cosO =
            (oa.toDouble().pow(2.0) + ob.pow(2.0) - ab.pow(2.0)) / (2 * oa * ob)
//                    Log.d("Huy", "onTouchEvent:  a : $oa , b : $ob , $ab ,cos $cosO")

        if (cosO < 0.0) {
            val cos = cosO * (-1)
            val per = (cos / 1.0) * 90
            rotate =
                if (isDownMin) (360 - (90F + per)).toFloat() else (90F + per).toFloat()
        } else if (cosO > 0.0) {
            when (cosO) {
                in 0.0..0.5 -> {
                    val per = 30 - ((cosO / 0.5) * 30)
                    rotate =
                        if (isDownMin) (360 - (60 + per)).toFloat() else (60 + per).toFloat()
                }
                in 0.5000001..(sqrt(2.0)) / 2.0 -> {
                    val per = 15 - ((cosO - 0.5) / (((sqrt(2.0)) / 2.0) - 0.5)) * 15
                    rotate =
                        if (isDownMin) (360 - (45 + per)).toFloat() else (45 + per).toFloat()
                }
                in ((sqrt(2.0)) / 2.0 + 0.000001)..(sqrt(3.0)) / 2.0 -> {
                    val per =
                        15 - ((cosO - ((sqrt(2.0)) / 2.0)) / (((sqrt(3.0)) / 2.0) - ((sqrt(
                            2.0
                        )) / 2.0))) * 15
                    rotate =
                        if (isDownMin) (360 - (30 + per)).toFloat() else (30 + per).toFloat()
                }
                else -> {
                    val per =
                        30 - ((cosO - (sqrt(3.0) / 2.0)) / ((1.0 - (sqrt(3.0) / 2.0)))) * 30
                    rotate =
                        if (isDownMin) (360 - (0 + per)).toFloat() else (0 + per).toFloat()
                }
            }
        } else {
            rotate = if (isDownMin) (360 - 90F) else 90F
        }
        return rotate
    }

    private fun getRotateTouch(x: Float, y: Float, isDownMin: Boolean) {
        xM = x
        yM = y
        rotateTouch = getRotate(xM, yM, isDownMin)
    }
}