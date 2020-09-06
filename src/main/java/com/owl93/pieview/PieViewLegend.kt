package com.owl93.pieview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.lang.Exception
import kotlin.math.max

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
            shapePaints = generatePaints(value)
            textPaint = generateTextPaint(textColor, textSize)
            texts = generateTexts(value, valueFormat, drawLabel, drawValue)
            requestLayout()
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

    var drawLabel : Boolean = DEFAULT_DRAW_LEGEND_LABEL
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var drawValue : Boolean = DEFAULT_DRAW_LEGEND_VALUE
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }
    var valueFormat: LegendFormat = DEFAULT_LEGEND_FORMAT
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var textSize: Float = DEFAULT_TEXT_SIZE
        set(value) {
            field = value
            textPaint = generateTextPaint(textColor, textSize)
            requestLayout()
            invalidate()
        }

    var textColor: Int  = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            textPaint = generateTextPaint(textColor, textSize)
            invalidate()
        }

    var legendShape: LegendShape = DEFAULT_LEGEND_SHAPE
        set(value) {
            field = value
            invalidate()
        }

    var shapeCornerRadius: Float = DEFAULT_ROUNDED_RECT_RADIUS
        set(value) {
            field = value
            invalidate()
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

    private var shapePaints = generatePaints(pieView)
    private var textPaint  = generateTextPaint(textColor, textSize)
    private var texts = generateTexts(pieView, valueFormat, drawLabel, drawValue)
    private var textHeightBound = Rect()
    private val bounds = RectF()

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
            drawLabel = attributes.getBoolean(R.styleable.PieViewLegend_drawLegendLabel, DEFAULT_DRAW_LEGEND_LABEL)
            drawValue = attributes.getBoolean(R.styleable.PieViewLegend_drawLegendValue, DEFAULT_DRAW_LEGEND_VALUE)
            valueFormat = when(attributes.getInt(R.styleable.PieViewLegend_legendValueTextFormat, 0)) {
                1 -> LegendFormat.VALUE
                2 -> LegendFormat.DECIMAL_PERCENT
                else -> LegendFormat.PERCENT
            }
            textSize = attributes.getDimension(R.styleable.PieViewLegend_legendTextSize, DEFAULT_TEXT_SIZE)
            textColor = attributes.getColor(R.styleable.PieViewLegend_legendTextColor, DEFAULT_TEXT_COLOR)
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



    fun update(view: PieView) {
        //Log.d(TAG, "update pieView")
        pieView = view
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val calcHeight: Float
        val calcWidth: Float
        val count = if(isInEditMode) PieView.testComponents.size else pieView?.components?.size ?: 0
        texts = generateTexts(pieView, valueFormat, drawLabel, drawValue)
        val textHeight = getTextHeight(textPaint, textHeightBound)
        if(orientation == Orientation.VERTICAL) {
            val maxHeight = max(shapeSize, textHeight)
            val maxLengthLabel = texts.maxBy { it.length }
            val maxTextWidth = textPaint.measureText(maxLengthLabel ?: "")
            calcHeight = paddingTop + paddingBottom + (maxHeight * count) + (verticalSpacing * count.minus(1))
            calcWidth = paddingLeft + paddingRight + shapeSize + horizontalSpacing + maxTextWidth
        }else {
            val textWidthSum = texts.fold(0f) {acc, txt -> acc + max(textPaint.measureText(txt), shapeSize) }
            calcHeight = paddingTop + paddingBottom + verticalSpacing + shapeSize + textHeight
            calcWidth = paddingLeft + paddingRight + (horizontalSpacing * count.minus(1)) + textWidthSum
        }
        setMeasuredDimension(getDimen(calcWidth.toInt(), widthMeasureSpec), getDimen(calcHeight.toInt(), heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas?) {
        if(canvas == null) return
        if(!isInEditMode && pieView == null) {
            Log.w(TAG, "PieView is null, please call PieViewLegend.setPieView() first")
            return
        }
        bounds.let {
            it.left = paddingLeft.toFloat()
            it.top = paddingTop.toFloat()
            it.right = (this.width - paddingRight).toFloat()
            it.bottom = (this.height - paddingBottom).toFloat()
        }
        texts = generateTexts(pieView, valueFormat, drawLabel, drawValue)
        textPaint.apply {
            color = textColor
            textSize = textSize
        }
        val components = pieView?.components ?: PieView.testComponents
        val textHeight = getTextHeight(textPaint, textHeightBound)
        if(orientation == Orientation.VERTICAL) {
            var top = paddingTop.toFloat()
            components.forEachIndexed { index, _ ->
                val spacing = if(index == components.size.minus(1)) 0f else verticalSpacing
                val maxHeight = max(shapeSize, textHeight)
                val cy = top + maxHeight / 2f
                when(legendOrder) {
                    LegendOrder.SHAPE_FIRST -> {
                        drawShape(canvas, bounds.left, cy - shapeSize/2f, shapeSize, shapePaints[index])
                        canvas.drawText(texts[index],bounds.left + shapeSize + horizontalSpacing, cy + textHeight/2f, textPaint)
                    }
                    LegendOrder.LABEL_FIRST -> {
                        canvas.drawText(texts[index], bounds.left, cy + textHeight/2f, textPaint)
                        drawShape(canvas, bounds.left + textPaint.measureText(texts[index]) + horizontalSpacing, cy - shapeSize/2f, shapeSize, shapePaints[index])
                    }
                }
                top += maxHeight + spacing
            }
        }else {
            var left = bounds.left
            components.forEachIndexed { index, _ ->
                val spacing = if(index == components.size.minus(1)) 0f else horizontalSpacing
                val textWidth = textPaint.measureText(texts[index])
                val maxWidth = max(shapeSize, textWidth)
                val cx = maxWidth/2f + left
                when(legendOrder) {
                    LegendOrder.SHAPE_FIRST -> {
                        drawShape(canvas, cx - shapeSize/2f, bounds.top, shapeSize, shapePaints[index])
                        canvas.drawText(texts[index], cx - textWidth/2f,bounds.top + verticalSpacing + shapeSize + textHeight, textPaint)
                    }
                    LegendOrder.LABEL_FIRST -> {
                        canvas.drawText(texts[index], cx - textWidth/2f,bounds.top + textHeight, textPaint)
                        drawShape(canvas, cx - shapeSize/2f, bounds.top + verticalSpacing + textHeight, shapeSize, shapePaints[index])
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

        private fun generateTextBounds(pie: PieView?) = (pie?.components ?: PieView.testComponents).map { Rect() }

        private fun getTextHeight(paint: Paint, bound: Rect) : Float {
            paint.getTextBounds("Test",0, "Test".length, bound)
            return bound.height().toFloat()
        }

        private fun generatePaints(pie: PieView?) = (pie?.components ?: PieView.testComponents).map {
            Paint().apply {
                flags = Paint.ANTI_ALIAS_FLAG
                color = it.color
                style = Paint.Style.FILL
            }
        }

        private fun generateTextPaint(textColor: Int, size: Float) = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = textColor
            textSize = size
        }


        private fun generateTexts(pie: PieView?, format: LegendFormat, drawLabel: Boolean, drawValue: Boolean): List<String> {
            val fmt = when (format) {
                LegendFormat.VALUE -> "%#.2f"
                LegendFormat.DECIMAL_PERCENT -> "%3.2f%%"
                else -> "%3.0f%%"
            }
            val comps = pie?.components ?: PieView.testComponents
            val sum = comps.fold(0f) { acc, c -> acc + c.value}
            val percents = comps.map { (it.value/sum)*100f }
            return comps.mapIndexed { idx, it ->
                if(!drawValue) it.label
                else {
                    "%s%s%s".format(
                        if(drawLabel) it.label else "",
                        if(drawLabel && drawValue) " " else "",
                        fmt.format(if(format != LegendFormat.VALUE) percents[idx] else it.value)
                    )
                }
            }
        }

    }

}