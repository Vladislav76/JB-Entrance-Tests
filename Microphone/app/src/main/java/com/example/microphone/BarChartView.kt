package com.example.microphone

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import java.util.*

class BarChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var maxValue = 0
    private var maxAmount = 0
    private var values: LinkedList<Int> = LinkedList()
    private val paint = Paint()

    fun changeConfig(maxAmount: Int) {
        this.maxAmount = maxAmount
        maxValue = 0
        clear()
    }

    fun clear() {
        values.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val currentWidth = width - paddingLeft - paddingRight
        val currentHeight = height - paddingTop - paddingBottom
        val offset = currentWidth.toFloat() / maxAmount

        val borderLeft = (marginLeft + paddingLeft).toFloat()
        val borderRight = borderLeft + currentWidth
        val borderTop = (marginTop + paddingTop).toFloat()
        val borderBottom = borderTop + currentHeight

        paint.apply {
            color = Color.GREEN
            style = Paint.Style.FILL
        }

        val leftOffset = offset * (maxAmount - values.size)

        for ((index, value) in values.withIndex()) {
            val currentLeft = leftOffset + borderLeft + offset * index
            val currentTop = borderBottom - value.toFloat() / maxValue * currentHeight
            val currentRight = currentLeft + offset
            canvas?.drawRect(currentLeft, currentTop, currentRight, borderBottom, paint)
        }

        paint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
        }

        canvas?.drawRect(borderLeft, borderTop, borderRight, borderBottom, paint)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            putParcelable(SUPER_STATE_EXTRA, super.onSaveInstanceState())
            putInt(MAX_VALUE_EXTRA, maxValue)
            putInt(MAX_AMOUNT_EXTRA, maxAmount)
            putIntArray(VALUES_EXTRA, values.toIntArray())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            maxValue = state.getInt(MAX_VALUE_EXTRA)
            maxAmount = state.getInt(MAX_AMOUNT_EXTRA)
            state.getIntArray(VALUES_EXTRA)?.toCollection(values)
            super.onRestoreInstanceState(state.getParcelable(SUPER_STATE_EXTRA))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun addValue(value: Int) {
        if (value > maxValue) {
            maxValue = value
        }
        if (values.size < maxAmount) {
            values.add(value)
        } else {
            values.removeAt(0)
            values.add(value)
        }
        invalidate()
    }

    companion object {
        private const val SUPER_STATE_EXTRA = "super_state_extra"
        private const val MAX_VALUE_EXTRA = "max_value_extra"
        private const val MAX_AMOUNT_EXTRA = "max_amount_extra"
        private const val VALUES_EXTRA = "values_extra"
    }
}