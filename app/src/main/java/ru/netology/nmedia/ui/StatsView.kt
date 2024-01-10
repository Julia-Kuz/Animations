package ru.netology.nmedia.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.withSave
import androidx.core.view.marginStart
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null
    private var animationType = AnimationType.Parallel

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()

            animationType = AnimationType.create(getInt(R.styleable.StatsView_animationType, animationType.value))
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }


    // *************** Задание 3   Attributes*   (объединяет все задачи) ******************

    // установили дефолтное значение  private var animationType = AnimationType.Parallel
    // и считываем значение в init {}  animationType = AnimationType.create(getInt(R.styleable.StatsView_animationType, animationType.value))

    override fun onDraw(canvas: Canvas) {
        when (animationType) {
            AnimationType.Parallel -> drawParallel(canvas)
            AnimationType.Sequential -> drawSequential(canvas)
            AnimationType.Bidirectional -> drawBidirectional(canvas)
        }
    }

    enum class AnimationType (val value: Int) {
        Parallel (0),
        Sequential(1),
        Bidirectional(2);

        companion object {
            fun create (value: Int): AnimationType = entries.find { it.value == value } ?: Parallel // до оптимизации Kotlin вместо entries было values(): values().find { it.value == value } ?: Parallel
        }
    }

    private fun drawParallel (canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        var startFrom = -90F
        for ((index, datum) in data.withIndex()) {
            val angle = 360F * datum
            paint.color = colors.getOrNull(index) ?: randomColor()
            canvas.drawArc(oval, startFrom, angle * progress, false, paint)
            startFrom += angle
        }

        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }


    private fun drawSequential(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        // Поскольку ниже будет возможен вызов return, подняли наверх отрисовку текста
        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )

        var startFrom = -90F
        // Максимальный угол поворота с учётом начального отступа
        val maxAngle = 360 * progress + startFrom

        for ((index, datum) in data.withIndex()) {
            val angle = datum * 360
            // Для того, чтобы при указании меньше 100% последняя дуга не росла до 100%
            val rotationAngle = min(angle, maxAngle - startFrom)
            paint.color = colors.getOrNull(index) ?: randomColor()
            canvas.drawArc(oval, startFrom, rotationAngle, false, paint)
            startFrom += angle
            // Проверка не зашли ли мы слишком далеко
            if (startFrom > maxAngle) return
        }
    }


    private fun drawBidirectional (canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        var startFrom = -90F
        for ((index, datum) in data.withIndex()) {
            val angle = 360F * datum
            paint.color = colors.getOrNull(index) ?: randomColor()

            canvas.withSave {
                canvas.drawArc(oval, (startFrom + (angle /2)),  angle * progress /2, false, paint)
                canvas.drawArc(oval, (startFrom + (angle /2)),  -angle * progress /2, false, paint)
            }

            startFrom += angle
        }

        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }


    // *************** Задание 4   Bidirectional*  мое решение *******************

//        override fun onDraw(canvas: Canvas) {
//        if (data.isEmpty()) {
//            return
//        }
//
//        var startFrom = -90F
//        for ((index, datum) in data.withIndex()) {
//            val angle = 360F * datum
//            paint.color = colors.getOrNull(index) ?: randomColor()
//
//            canvas.withSave {
//                canvas.drawArc(oval, (startFrom + (angle /2)),  angle * progress /2, false, paint)
//                canvas.drawArc(oval, (startFrom + (angle /2)),  -angle * progress /2, false, paint)
//            }
//
//            startFrom += angle
//        }
//
//        canvas.drawText(
//            "%.2f%%".format(data.sum() * 100),
//            center.x,
//            center.y + textPaint.textSize / 4,
//            textPaint,
//        )
//    }

    //    ************* Задание 4   Bidirectional*  (Анатолий) *******************

//    override fun onDraw (canvas: Canvas) {
//        if (data.isEmpty()) {
//            return
//        }
//
//        var startFrom = -90F
//
//        for ((index, datum) in data.withIndex()) {
//            val angle = 360F * datum
//            paint.color = colors.getOrNull(index) ?: randomColor()
//            val fullProgressAngle = angle * progress
//            val halfProgressAngle = fullProgressAngle / 2
//            canvas.drawArc(oval, startFrom - halfProgressAngle, fullProgressAngle, false, paint)
//            startFrom += angle
//        }
//
//        canvas.drawText(
//            "%.2f%%".format(data.sum() * 100),
//            center.x,
//            center.y + textPaint.textSize / 4,
//            textPaint,
//        )
//    }


    // *************** Задание 2   Sequential*  (Анатолий) **********************

//    override fun onDraw (canvas: Canvas) {
//        if (data.isEmpty()) {
//            return
//        }
//
//        // Поскольку ниже будет возможен вызов return, подняли наверх отрисовку текста
//        canvas.drawText(
//            "%.2f%%".format(data.sum() * 100),
//            center.x,
//            center.y + textPaint.textSize / 4,
//            textPaint,
//        )
//
//        var startFrom = -90F
//        // Максимальный угол поворота с учётом начального отступа
//        val maxAngle = 360 * progress + startFrom
//
//        for ((index, datum) in data.withIndex()) {
//            val angle = datum * 360
//            // Для того, чтобы при указании меньше 100% последняя дуга не росла до 100%
//            val rotationAngle = min(angle, maxAngle - startFrom)
//            paint.color = colors.getOrNull(index) ?: randomColor()
//            canvas.drawArc(oval, startFrom, rotationAngle, false, paint)
//            startFrom += angle
//            // Проверка не зашли ли мы слишком далеко
//            if (startFrom > maxAngle) return
//        }
//    }


// **************** Задание 1  Rotation   ************************

//    override fun onDraw(canvas: Canvas) {
//        if (data.isEmpty()) {
//            return
//        }
//
//        var startFrom = -90F
//        for ((index, datum) in data.withIndex()) {
//            val angle = 360F * datum
//            paint.color = colors.getOrNull(index) ?: randomColor()
//            canvas.drawArc(oval, (startFrom + (angle * progress * 4)), angle * progress, false, paint)
//            startFrom += angle
//        }
//
//        canvas.drawText(
//            "%.2f%%".format(data.sum() * 100),
//            center.x,
//            center.y + textPaint.textSize / 4,
//            textPaint,
//        )
//    }

// **************** лекция Parallel ****************************************

//    override fun onDraw(canvas: Canvas) {
//        if (data.isEmpty()) {
//            return
//        }
//
//        var startFrom = -90F
//        for ((index, datum) in data.withIndex()) {
//            val angle = 360F * datum
//            paint.color = colors.getOrNull(index) ?: randomColor()
//            canvas.drawArc(oval, startFrom, angle * progress, false, paint)
//            startFrom += angle
//        }
//
//        canvas.drawText(
//            "%.2f%%".format(data.sum() * 100),
//            center.x,
//            center.y + textPaint.textSize / 4,
//            textPaint,
//        )
//    }

    // ****************  ************************ ****************************

private fun update() {
    valueAnimator?.let {
        it.removeAllListeners()
        it.cancel()
    }
    progress = 0F

    valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
        addUpdateListener { anim ->
            progress = anim.animatedValue as Float
            invalidate()
        }
        duration = 3000
        interpolator = LinearInterpolator()
    }.also {
        it.start()
    }
}

private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}