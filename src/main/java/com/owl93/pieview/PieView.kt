package com.owl93.pieview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min


class PieView: View {
    private val DEFAULT_STROKE_WIDTH = 70f
    private val DEFAULT_STROKE_END = Paint.Cap.BUTT
    private val DEFAULT_START_ANGLE = 0f
    private val DEFAULT_DRAW_TRACK = true
    private val DEFAULT_TRACK_WIDTH = DEFAULT_STROKE_WIDTH * 1.1f
    private val DEFAULT_TRACK_COLOR = Color.GRAY
    private val DEFAULT_TRACK_ALPHA = 255
    private val DEFAULT_SHOW_DIVIDERS = false
    private val DEFAULT_DIVIDER_WIDTH = 2 //default divider with is 3 degrees


    var startAngle: Float = DEFAULT_START_ANGLE
        set(value) {
            field = value
            invalidate()
        }

    var strokeWidth: Float = DEFAULT_STROKE_WIDTH
        set(value) {
            field = value
            componentPaints = components.map { makePiePaint(it.color, value, strokeEnd) }
            invalidate()
        }

    var strokeEnd: Paint.Cap = DEFAULT_STROKE_END
        set(value) {
            field = value
            componentPaints = components.map { makePiePaint(it.color, strokeWidth, strokeEnd) }
        }

    var drawTrack: Boolean = DEFAULT_DRAW_TRACK
        set(value) {
            field = value
            invalidate()
        }

    var trackWidth : Float = DEFAULT_TRACK_WIDTH
        set(value) {
            field = value
            invalidate()
        }
    var trackColor: Int = DEFAULT_TRACK_COLOR
        set(value) {
            field = value
            invalidate()
        }

    var trackAlpha: Int = DEFAULT_TRACK_ALPHA
        set(value) {
            field = value
            invalidate()
        }

    var showDividers: Boolean = DEFAULT_SHOW_DIVIDERS
        set(value) {
            field = value
            invalidate()
        }

    var dividerWidth: Int = DEFAULT_DIVIDER_WIDTH
        set(value) {
            field = value
            invalidate()
        }


    var components: List<Component> = testComponents
        set(value) {
            field = value
            componentPaints = value.map { makePiePaint(it.color, strokeWidth, strokeEnd) }
            invalidate()
            legend?.update(this)
        }

    var angles: List<Pair<Float, Float>> = emptyList()

    private val trackPaint = Paint().also {
        it.flags = Paint.ANTI_ALIAS_FLAG
        it.style = Paint.Style.STROKE
    }

    private val dividerPaint = Paint().also {
        it.flags = Paint.ANTI_ALIAS_FLAG
        it.style = Paint.Style.STROKE
        it.color = Color.TRANSPARENT
    }

    var legend: PieViewLegend? = null
        set(value) {
            field = value
            value?.update(this)
        }

    private var componentPaints: List<Paint> = emptyList()
    private var viewBounds = Rect(0, 0, 0, 0)
    private var pieBounds = RectF(viewBounds)
    constructor(context: Context) : super(context) { init(null, context) }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) { init(attrs, context) }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(attrs, context) }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) { init(attrs, context) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(TAG, "onMeasure w ${MeasureSpec.toString(widthMeasureSpec)}")
        Log.d(TAG, "onMeasure h ${MeasureSpec.toString(heightMeasureSpec)}")
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        val calcWidth = measureDimen(desiredWidth, widthMeasureSpec)
        val calcHeight = measureDimen(desiredHeight, widthMeasureSpec)
        val minDimen = min(calcWidth, calcHeight)

        viewBounds.let {
            it.left = this.left + paddingLeft
            it.top = this.top + paddingTop
            it.right =  minDimen - paddingRight
            it.bottom = minDimen - paddingBottom
        }

        pieBounds.apply {
            left = viewBounds.left.toFloat()
            top = viewBounds.top.toFloat()
            right = viewBounds.right.toFloat()
            bottom = viewBounds.bottom.toFloat()
        }
    }

    private fun measureDimen(desiredSize: Int, measureSpec: Int) : Int {
        var result = 0
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        result = when(mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(result, size)
            else -> desiredSize
        }
        if(result < desiredSize) Log.w(TAG, "View too small, may be clipped")
        return result
    }


    private fun init(attrs: AttributeSet?, context: Context) {
        if(attrs == null) return

        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.PieView, 0,0)
        try {
            strokeWidth = attributes.getDimension(R.styleable.PieView_strokeWidth, DEFAULT_STROKE_WIDTH)
            strokeEnd = when(attributes.getInt(R.styleable.PieView_strokeEnd, 0)) {
                1 -> Paint.Cap.ROUND
                else -> Paint.Cap.BUTT
            }
            startAngle = attributes.getFloat(R.styleable.PieView_startAngle, DEFAULT_START_ANGLE)
            drawTrack = attributes.getBoolean(R.styleable.PieView_drawTrack, DEFAULT_DRAW_TRACK)
            trackWidth = attributes.getDimension(R.styleable.PieView_trackWidth, DEFAULT_STROKE_WIDTH)
            trackColor = attributes.getColor(R.styleable.PieView_trackColor, DEFAULT_TRACK_COLOR)
            trackAlpha = (attributes.getFloat(R.styleable.PieView_trackAlpha, .5f) * 255).toInt()
            showDividers = attributes.getBoolean(R.styleable.PieView_drawDividers, DEFAULT_SHOW_DIVIDERS)
            dividerWidth = attributes.getInt(R.styleable.PieView_dividerWidth, DEFAULT_DIVIDER_WIDTH)
        }catch (e: Exception) {
            Log.w(TAG, e.message.toString())
        }finally {
            attributes.recycle()
        }

    }


    override fun onDraw(canvas: Canvas?) {
        if(canvas == null) return
        angles = calculateAngles(components, showDividers, dividerWidth)
        val maxStroke = max(strokeWidth, trackWidth)

        if(drawTrack) {
            trackPaint.apply {
                color = trackColor
                strokeWidth = trackWidth
                alpha = trackAlpha
                strokeCap = Paint.Cap.BUTT
            }
            canvas.drawArc(
                    pieBounds.left + maxStroke/2,
                    pieBounds.top + maxStroke/2,
                    pieBounds.bottom - maxStroke/2,
                    pieBounds.right - maxStroke/2,
                    0f, 360f, false, trackPaint
            )
        }

        dividerPaint.strokeWidth = strokeWidth
        var start = -90f + startAngle + if(showDividers) dividerWidth/2f else 0f
        for((idx, _) in components.withIndex()) {
            canvas.drawArc(
                    pieBounds.left + maxStroke/2,
                    pieBounds.top + maxStroke/2,
                    pieBounds.bottom - maxStroke/2,
                    pieBounds.right - maxStroke/2,
                    start, angles[idx].second, false, componentPaints[idx]
            )
            start += angles[idx].second
            if(showDividers){
                canvas.drawArc(
                        pieBounds.left + maxStroke/2,
                        pieBounds.top + maxStroke/2,
                        pieBounds.bottom - maxStroke/2,
                        pieBounds.right - maxStroke/2,
                        start, dividerWidth.toFloat(), false, dividerPaint
                )
                start += dividerWidth
            }
        }

    }


    fun animateChange(newComponents: List<Component>, anim: ChangeAnimation, duration: Long = 500) {
    }

    enum class ChangeAnimation {
        SHIFT,
        SEQUENCE,
        SLIDE,
        SLIDE_STAGGER
    }

    data class Component(
        var label: String = "",
        var value: Float = 1f,
        var color: Int = Color.BLACK
    )

    companion object {
        const val TAG = "PieView"
        var testComponents: List<Component> = listOf(
            Component("One",.53f,  Color.parseColor("#D500F9")),
            Component("Four", 1f, Color.parseColor("#F50057")),
            Component("Two",.33f, Color.parseColor("#2979FF")),
            Component("Three", .14f,  Color.parseColor("#76FF03"))
        )


        //returns paris of (%, and degrees) for each component
        private fun calculateAngles(components: List<Component>, showDividers: Boolean, dividerWidth: Int): List<Pair<Float, Float>> {
            val total = components.fold(0f) {sum, comp -> sum + comp.value}
            val availableDegrees = 360f - if(showDividers) (dividerWidth.toFloat() * components.size) else 0f
            return components.map { Pair(it.value/total, (it.value/total) * availableDegrees) }

        }

        private fun makePiePaint(paintColor: Int, width: Float, cap: Paint.Cap) = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = paintColor
            strokeWidth = width
            strokeCap = cap
            style = Paint.Style.STROKE
        }
    }




}