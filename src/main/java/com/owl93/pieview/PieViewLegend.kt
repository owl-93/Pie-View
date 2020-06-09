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
    private val DEFAULT_ORDER = LegendOrder.SHAPE_FIRST
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
            field = value
            paints = generatePaints()
            textBounds = generateTextBounds()
            textPaint = generateTextPaint()
            invalidate()
        }

    var orientation: Orientation = DEFAULT_ORIENTATION
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var legendOrder: LegendOrder = DEFAULT_ORDER
        set(value) {
            field = value
            invalidate()
        }
    var drawLegendLabel : Boolean = DEFAULT_DRAW_LEGEND_LABEL
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var drawLegendValue : Boolean = DEFAULT_DRAW_LEGEND_VALUE
        set(value) {
            field = value
            requestLayout()
            invalidate() }

    var legendValueFormat: LegendFormat = DEFAULT_LEGEND_FORMAT
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var legendTextSize: Float = DEFAULT_TEXT_SIZE
        set(value) {
            field = value
            textBounds = generateTextBounds()
            textPaint = generateTextPaint()
            requestLayout()
            invalidate()
        }

    var legendTextColor: Int  = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            textBounds = generateTextBounds()
            textPaint = generateTextPaint()
            invalidate()
        }

    var legendShape: LegendShape = DEFAULT_LEGEND_SHAPE
        set(value) {
            field = value
            postInvalidate()
        }

    var shapeCornerRadius: Float = DEFAULT_ROUNDED_RECT_RADIUS
        set(value) {
            field = value
            postInvalidate()
        }

    var shapeSize: Float = DEFAULT_LEGEND_SHAPE_SIZE
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var verticalSpacing: Float = DEFAULT_VERTICAL_MARGIN
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var horizontalSpacing: Float = DEFAULT_HORIZONTAL_MARGIN
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    private var bounds = Rect()
    private var paints = generatePaints()
    private var textPaint  = generateTextPaint()
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
            legendOrder = when(attributes.getInt(R.styleable.PieViewLegend_legendOrder, 0)) {
                1 -> LegendOrder.LABEL_FIRST
                else -> LegendOrder.SHAPE_FIRST
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
            verticalSpacing = attributes.getDimension(R.styleable.PieViewLegend_verticalSpacing, DEFAULT_VERTICAL_MARGIN)
            horizontalSpacing = attributes.getDimension(R.styleable.PieViewLegend_horizontalSpacing, DEFAULT_HORIZONTAL_MARGIN)
        }catch (e: Exception) { Log.w(PieView.TAG, e.message.toString()) } finally { attributes.recycle() }
    }

    private fun generatePaints() =
        (if(isInEditMode) PieView.testComponents else pieView?.components ?: emptyList()).map { Paint().apply {
                flags = Paint.ANTI_ALIAS_FLAG
                color = it.color
                style = Paint.Style.FILL
            }}

    private fun generateTextPaint() = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = legendTextColor
            textSize = legendTextSize
        }

    private fun generateTextBounds() = (if(isInEditMode) PieView.testComponents else pieView?.components ?: emptyList()).map { Rect() }


    private fun generateTexts() = (if(isInEditMode) PieView.testComponents else pieView?.components ?: emptyList()).mapIndexed { idx, it ->
        val baseString = "${if(drawLegendLabel) it.label else ""}${if(drawLegendLabel && drawLegendValue) " - " else ""}"
        if(!drawLegendValue) baseString
        else {
            val pcntg = (pieView?.angles?.get(idx)?.first ?: 0f) * 100
            baseString + when (legendValueFormat) {
                LegendFormat.VALUE -> String.format("%#.2f", it.value)
                LegendFormat.DECIMAL_PERCENT -> String.format("%3.2f%%", pcntg)
                else -> String.format("%3.0f%%", pcntg)
            }
        }
    }

    fun update(view: PieView) {
        Log.d(TAG, "update pieView")
        pieView = view
    }

    fun updateTexts() {
        //Log.d(TAG, "update texts")
        texts = generateTexts()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "onMeasure w ${MeasureSpec.toString(widthMeasureSpec)}")
        Log.d(TAG, "onMeasure h ${MeasureSpec.toString(heightMeasureSpec)}")
        val calcHeight: Float
        val calcWidth: Float
        val count = if(isInEditMode) PieView.testComponents.size else pieView?.components?.size ?: 0
        val texts = generateTexts()
        val tb = generateTextBounds()
        for((i, t) in texts.withIndex()) textPaint.getTextBounds(t, 0, t.length, tb[i])
        val maxLengthLabel = texts.maxBy { it.length }
        val textHeight = tb[0].height().toFloat() ?: legendTextSize
        val maxTextWidth = textPaint.measureText(maxLengthLabel)
        if(orientation == Orientation.VERTICAL) {
            val maxHeight = max(shapeSize, textHeight)
            calcHeight = paddingTop + paddingBottom + (maxHeight * count) + (verticalSpacing * count.minus(1))
            calcWidth = paddingLeft + paddingRight + shapeSize + horizontalSpacing + maxTextWidth
        }else {
            calcHeight = paddingTop + paddingBottom + verticalSpacing + shapeSize + textHeight
            val textWidthSum = tb.fold(0f) {acc, bounds -> acc + max(bounds.width().toFloat(), shapeSize) }
            calcWidth = paddingLeft + paddingRight + (horizontalSpacing * count.minus(1)) + textWidthSum
        }
        Log.d(TAG, "calculated dimens: [${calcWidth}x${calcHeight}]")
        setMeasuredDimension(getDimen(calcWidth.toInt(), widthMeasureSpec), getDimen(calcHeight.toInt(), heightMeasureSpec))
        bounds.let {
            it.left = this.left + paddingLeft
            it.top = this.top + paddingTop
            it.right = this.right - paddingRight
            it.bottom = this.bottom - paddingBottom
        }
    }

    private fun getDimen(desiredSize: Int, measureSpec: Int) : Int {
        var result = desiredSize
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
            textPaint.getTextBounds(texts[index], 0, texts[index].length, textBounds[index])
        }
        val components: List<PieView.Component>? = if(isInEditMode) PieView.testComponents else pieView?.components
        val textHeight = if(isInEditMode) legendTextSize else textBounds[0].height().toFloat()
        if(orientation == Orientation.VERTICAL) {
            var top = bounds.top.toFloat()
            components?.forEachIndexed { index, _ ->
                val spacing = if(index == components.size.minus(1)) 0f else verticalSpacing
                val maxHeight = max(shapeSize, textHeight)
                val cy = top + maxHeight / 2f
                when(legendOrder){
                    LegendOrder.SHAPE_FIRST -> {
                        drawShape(canvas, bounds.left.toFloat(), cy - shapeSize/2f, shapeSize, paints[index])
                        canvas.drawText(texts[index],bounds.left + shapeSize + horizontalSpacing, cy + textHeight/2f, textPaint)
                    }
                    LegendOrder.LABEL_FIRST -> {
                        canvas.drawText(texts[index], bounds.left.toFloat(), cy + textHeight/2f, textPaint)
                        drawShape(canvas, bounds.left.toFloat() + textPaint.measureText(texts[index]) + horizontalSpacing, cy - shapeSize/2f, shapeSize, paints[index])
                    }
                }

                top += maxHeight + spacing
            }
        }else {
            var left = bounds.left.toFloat()
            components?.forEachIndexed { index, _ ->
                val spacing = if(index == components.size.minus(1)) 0f else horizontalSpacing
                val textWidth = textPaint.measureText(texts[index])
                val maxWidth = max(shapeSize, textWidth)
                val cx = maxWidth/2f + left
                when(legendOrder) {
                    LegendOrder.SHAPE_FIRST -> {
                        drawShape(canvas, cx - shapeSize/2f, bounds.top.toFloat(), shapeSize, paints[index])
                        canvas.drawText(texts[index], cx - textWidth/2f,bounds.top + verticalSpacing + shapeSize + textHeight, textPaint)
                    }
                    LegendOrder.LABEL_FIRST -> {
                        canvas.drawText(texts[index], cx - textWidth/2f,bounds.top + textHeight, textPaint)
                        drawShape(canvas, cx - shapeSize/2f, bounds.top.toFloat() + verticalSpacing + textHeight, shapeSize, paints[index])
                    }
                }

                left += maxWidth + spacing
            }
        }

    }

    private fun drawShape(c: Canvas, left: Float, top: Float, size: Float, paint: Paint) {
        when(legendShape) {
            LegendShape.CIRCLE -> c.drawOval(left, top, left + size, top + size, paint)
            LegendShape.SQUARE -> c.drawRoundRect(left, top, left + size, top + size, shapeCornerRadius, shapeCornerRadius, paint)
        }
    }

    enum class LegendOrder {
        SHAPE_FIRST,
        LABEL_FIRST
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