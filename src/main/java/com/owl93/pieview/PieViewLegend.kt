package com.owl93.pieview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min

class PieViewLegend: View {
    private val DEFAULT_TEXT_SIZE = 60f
    private val DEFAULT_TEXT_COLOR = Color.DKGRAY
    private val DEFAULT_DRAW_LEGEND_LABEL = true
    private val DEFAULT_DRAW_LEGEND_VALUE = true
    private val DEFAULT_LEGEND_FORMAT = LegendFormat.PERCENT
    private val DEFAULT_ORIENTATION = Orientation.HORIZONTAL
    private val DEFAULT_LEGEND_SHAPE_SIZE = 100f
    private val DEFAULT_LEGEND_SHAPE = LegendShape.CIRCLE
    private val DEFAULT_ROUNDED_RECT_RADIUS = 25f
    private val DEFAULT_VERTICAL_MARGIN = 10f
    private val DEFAULT_HORIZONTAL_MARGIN = 10f



    private var pieView: PieView? = null
        set(value) {
            field = value;
            paints = generatePaints()
            textBounds = generateTextBounds()
            textPaints = generateTextPaints()
            invalidate()
        }

    var orientation: Orientation = DEFAULT_ORIENTATION
        set(value) { field = value; invalidate()}

    var drawLegendLabel : Boolean = DEFAULT_DRAW_LEGEND_LABEL
        set(value) { field = value; invalidate() }

    var drawLegendValue : Boolean = DEFAULT_DRAW_LEGEND_VALUE
        set(value) { field = value; invalidate() }

    var legendValueFormat: LegendFormat = DEFAULT_LEGEND_FORMAT
        set(value) { field = value; invalidate() }

    var legendTextSize: Float = DEFAULT_TEXT_SIZE
        set(value) {
            field = value
            textBounds = generateTextBounds()
            textPaints = generateTextPaints()
            invalidate()
        }

    var legendTextColor: Int  = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            textBounds = generateTextBounds()
            textPaints = generateTextPaints()
            invalidate()
        }

    var legendShape: LegendShape = DEFAULT_LEGEND_SHAPE
        set(value) { field = value; postInvalidate() }

    var shapeCornerRadius: Float = DEFAULT_ROUNDED_RECT_RADIUS
        set(value) {field = value; postInvalidate() }

    var shapeSize: Float = DEFAULT_LEGEND_SHAPE_SIZE
        set(value) { field = value; postInvalidate() }

    var verticalMargin: Float = DEFAULT_VERTICAL_MARGIN
        set(value) { field = value; postInvalidate() }

    var horizontalMargin: Float = DEFAULT_HORIZONTAL_MARGIN
        set(value) { field = value; postInvalidate()}

    private var bounds = Rect()
    private var paints = generatePaints()
    private var textPaints  = generateTextPaints()
    private var textBounds = generateTextBounds()
    private var texts = generateTexts()

    constructor(context: Context) : super(context) { init(null, context) }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) { init(attrs, context) }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(attrs, context) }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) { init(attrs, context) }


    private fun init(attrs: AttributeSet?, context: Context) {
        if(attrs == null) return
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.PieViewLegend, 0,0)
        try {
            orientation = when(attributes.getInt(R.styleable.PieViewLegend_legendOrientation, 0)) {
                1 -> Orientation.HORIZONTAL
                else -> Orientation.VERTICAL
            }
            drawLegendLabel = attributes.getBoolean(R.styleable.PieViewLegend_drawLegendLabel, DEFAULT_DRAW_LEGEND_LABEL)
            drawLegendValue = attributes.getBoolean(R.styleable.PieViewLegend_drawLegendValue, DEFAULT_DRAW_LEGEND_VALUE)
            legendValueFormat = when(attributes.getInt(R.styleable.PieViewLegend_legendValueTextFormat, 0)) {
                1 -> LegendFormat.VALUE
                2 -> LegendFormat.DECIMAL_PERCENT
                else -> LegendFormat.PERCENT
            }
            legendTextSize = attributes.getDimension(R.styleable.PieViewLegend_legendTextSize, DEFAULT_TEXT_SIZE)
            legendTextColor = attributes.getColor(R.styleable.PieViewLegend_legendTextColor, DEFAULT_TEXT_COLOR)
            legendShape = when(attributes.getInt(R.styleable.PieViewLegend_legendShape, 0)) {
                1 -> LegendShape.SQUARE
                else -> LegendShape.CIRCLE
            }
            shapeCornerRadius = attributes.getDimension(R.styleable.PieViewLegend_shapeCornerRadius, DEFAULT_ROUNDED_RECT_RADIUS)
            shapeSize = attributes.getDimension(R.styleable.PieViewLegend_shapeSize, DEFAULT_LEGEND_SHAPE_SIZE)
            verticalMargin = attributes.getDimension(R.styleable.PieViewLegend_verticalSpacing, DEFAULT_VERTICAL_MARGIN)
            horizontalMargin = attributes.getDimension(R.styleable.PieViewLegend_horizontalSpacing, DEFAULT_HORIZONTAL_MARGIN)
        }catch (e: Exception) { Log.w(PieView.TAG, e.message.toString()) } finally { attributes.recycle() }
    }

    private fun generatePaints() =
        (if(isInEditMode) PieView.testComponents else pieView?.components ?: emptyList()).map { Paint().apply {
                flags = Paint.ANTI_ALIAS_FLAG
                color = it.color
                style = Paint.Style.FILL
            }}

    private fun generateTextPaints() = (if(isInEditMode) PieView.testComponents else pieView?.components ?: emptyList()).map {
        Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = legendTextColor
            textSize = legendTextSize
        }
    }

    private fun generateTextBounds() = (if(isInEditMode) PieView.testComponents else pieView?.components ?: emptyList()).map { Rect() }


    private fun generateTexts() = (if(isInEditMode) PieView.testComponents else pieView?.components ?: emptyList()).mapIndexed { idx, it ->
        val baseString = "${if(drawLegendLabel) it.label else ""}${if(drawLegendLabel && drawLegendValue) " - " else ""}"
        if(!drawLegendValue) baseString
        else {
            val pcntg = (pieView?.angles?.get(idx)?.first ?: 0f) * 100
            baseString + when (legendValueFormat) {
                LegendFormat.VALUE -> "${it.value}"
                LegendFormat.DECIMAL_PERCENT -> String.format("%3.2f%%", pcntg)
                else -> String.format("%3.0f%%", pcntg)
            }
        }
    }

    fun update(view: PieView) {
        pieView = view
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //
        Log.d(TAG, "onMeasure w ${MeasureSpec.toString(widthMeasureSpec)}")
        Log.d(TAG, "onMeasure h ${MeasureSpec.toString(heightMeasureSpec)}")
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        setMeasuredDimension(measureDimen(desiredWidth, widthMeasureSpec), measureDimen(desiredHeight, heightMeasureSpec))
        bounds.let {
            it.left = this.left + paddingLeft
            it.top = this.top + paddingTop
            it.right = this.right - paddingRight
            it.bottom = this.bottom - paddingBottom
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

    override fun onDraw(canvas: Canvas?) {
        if(canvas == null) return
        if(!isInEditMode && pieView == null) {
            Log.w(TAG, "PieView is null, please call PieViewLegend.setPieView() first")
            return
        }
        //compute text boundaries & format labels

        texts = generateTexts()
        pieView?.components?.forEachIndexed{ index, _ ->
            textPaints[index].getTextBounds(texts[index], 0, texts[index].length, textBounds[index])
        }
        val components: List<PieView.Component>? = if(isInEditMode) PieView.testComponents else pieView?.components
        if(orientation == Orientation.VERTICAL) {
            var top = bounds.top.toFloat()
            components?.forEachIndexed { index, _ ->
                val textHeight = textBounds[index].height().toFloat()
                val maxHeight = max(shapeSize, textHeight)
                val cy = top + maxHeight / 2f
                drawShape(canvas, bounds.left.toFloat(), cy - shapeSize/2, shapeSize, paints[index])
                canvas.drawText(texts[index],bounds.left + shapeSize + horizontalMargin, cy + textHeight/2, textPaints[index])
                top += maxHeight + verticalMargin
            }
        }else {
            var left = bounds.left.toFloat()
            components?.forEachIndexed { index, _ ->
                val tb = textBounds[index]
                val maxWidth = max(shapeSize, tb.width().toFloat())
                val cx = maxWidth/2 + left
                drawShape(canvas, cx - shapeSize/2, bounds.top.toFloat(), shapeSize, paints[index])
                canvas.drawText(texts[index], cx - tb.width()/2f,bounds.top + verticalMargin + shapeSize + tb.height(), textPaints[index])
                left += maxWidth + horizontalMargin
            }
        }

    }

    private fun drawShape(c: Canvas, left: Float, top: Float, size: Float, paint: Paint) {
        when(legendShape) {
            LegendShape.CIRCLE -> c.drawOval(left, top, left + size, top + size, paint)
            LegendShape.SQUARE -> c.drawRoundRect(left, top, left + size, top + size, shapeCornerRadius, shapeCornerRadius, paint)
        }
    }

    enum class LegendFormat {
        PERCENT,
        VALUE,
        DECIMAL_PERCENT
    }

    enum class LegendShape {
        CIRCLE,
        SQUARE
    }

    enum class Orientation {
        VERTICAL,
        HORIZONTAL
    }
    companion object {
        private const val TAG = "PieViewLegend"
    }

}