/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.setupdesign.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import androidx.core.os.BundleCompat
import androidx.core.view.isVisible
import com.google.android.setupdesign.R
import kotlin.math.PI
import kotlin.math.sin

/**
 * A custom circular progress indicator that supports gradient progress, solid color progress, and
 * indeterminate animation.
 */
class GradientCircularProgressIndicator
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  View(context, attrs, defStyleAttr) {

  // Paints
  private val backgroundTrackPaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
  private val progressPaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.ROUND
    }

  // Drawing properties
  private var trackThicknessPx: Float = 0f
  private var centerX = 0f
  private var centerY = 0f
  private var radius = 0f
  private val drawRect = RectF()

  // Gradient animation properties
  private var isMultiColor = false
  private var gradientRotation = 0f
  private val matrix = Matrix()
  private var activeShader: Shader? = null
  private var gradientAnimator: ValueAnimator? = null

  // Indeterminate animation properties
  private var indeterminateAnimator: ValueAnimator? = null
  private var indeterminateOffset = 0f
  private var indeterminateSweep = 0f

  /** The thickness of the indicator in DPs. */
  var trackThicknessDp: Float = 4f
    set(value) {
      field = value
      syncThickness()
    }

  /** The current progress of the indicator (0 to 100). Ignored if [isIndeterminate] is true. */
  var progress: Float = 0f
    set(value) {
      // Coerce input to 0-100 range and store
      field = value.coerceIn(minimumValue = 0f, maximumValue = 100f)
      if (!isIndeterminate) invalidate()
    }

  /**
   * When `true`, the indicator performs a continuous "stretch and spin" animation. When `false`, it
   * displays the value set in [progress].
   */
  var isIndeterminate: Boolean = true
    set(value) {
      if (field != value) {
        field = value
        if (value && isAttachedToWindow && isVisible) {
          startIndeterminateAnimation()
        } else {
          stopIndeterminateAnimation()
        }
        invalidate()
      }
    }

  /** The color of the background track ring. */
  var trackColor: Int = Color.TRANSPARENT
    set(value) {
      field = value
      backgroundTrackPaint.color = value
      invalidate()
    }

  /**
   * The colors used for the progress arc.
   * - Providing one color results in a solid fill.
   * - Providing multiple colors results in a rotating gradient.
   */
  var indicatorColors: IntArray = intArrayOf()
    set(value) {
      // LinearGradient requires at least two colors. If only one is provided, duplicate it.
      field = if (value.size == 1) intArrayOf(value[0], value[0]) else value
      isMultiColor = field.size > 1 && field.any { it != field[0] }

      // Safety Check: Shader requires width/height > 0
      if (width > 0 && height > 0) {
        updateShaderConfiguration()
        invalidate()
      }
    }

  init {
    context.withStyledAttributes(
      attrs,
      R.styleable.SudGradientCircularProgressIndicator,
      defStyleAttr,
      0,
    ) {
      val pxFromXml =
        getDimension(R.styleable.SudGradientCircularProgressIndicator_sudTrackThickness, -1f)
      if (pxFromXml != -1f) {
        trackThicknessDp = pxFromXml / context.resources.displayMetrics.density
      }

      trackColor =
        getColor(R.styleable.SudGradientCircularProgressIndicator_sudTrackColor, trackColor)

      isIndeterminate =
        getBoolean(R.styleable.SudGradientCircularProgressIndicator_sudIsIndeterminate, true)
    }

    context.withStyledAttributes(null, intArrayOf(android.R.attr.colorPrimary)) {
      val colorPrimary = getColor(/* index= */ 0, /* defValue= */ Color.TRANSPARENT)
      indicatorColors = intArrayOf(colorPrimary)
    }

    syncThickness()
    backgroundTrackPaint.color = trackColor
  }

  /** Synchronizes the pixel-based stroke widths with the current DP thickness. */
  private fun syncThickness() {
    trackThicknessPx = trackThicknessDp * context.resources.displayMetrics.density
    backgroundTrackPaint.strokeWidth = trackThicknessPx
    progressPaint.strokeWidth = trackThicknessPx
    invalidate()
  }

  private fun updateShaderConfiguration() {
    if (width == 0 || height == 0) return

    // Create a LinearGradient from left to right (0 to width)
    activeShader =
      LinearGradient(
        /* x0 = */ 0f,
        /* y0 = */ centerY,
        /* x1 = */ width.toFloat(),
        /* y1 = */ centerY,
        /* colors = */ indicatorColors,
        /* positions = */ null,
        Shader.TileMode.CLAMP,
      )
    progressPaint.shader = activeShader
  }

  private fun startGradientAnimation() {
    if (gradientAnimator != null) {
      return
    }
    gradientAnimator =
      ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 2500
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
          gradientRotation = it.animatedValue as Float
          // Only invalidate if there's a gradient to rotate
          if (isMultiColor && isAttachedToWindow && isVisible) {
            invalidate()
          }
        }
        start()
      }
  }

  private fun stopGradientAnimation() {
    gradientAnimator?.cancel()
    gradientAnimator = null
  }

  private fun startIndeterminateAnimation() {
    indeterminateAnimator?.cancel()
    indeterminateAnimator =
      ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
          val fraction = it.animatedValue as Float
          indeterminateOffset = fraction * 720f
          val sweepFraction = (sin(fraction * PI * 2 - PI / 2) + 1) / 2
          indeterminateSweep = (sweepFraction * 280f + 20f).toFloat()
          invalidate()
        }
        start()
      }
  }

  private fun stopIndeterminateAnimation() {
    indeterminateAnimator?.cancel()
    indeterminateAnimator = null
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    startGradientAnimation()
    if (isIndeterminate) {
      startIndeterminateAnimation()
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    stopGradientAnimation()
    stopIndeterminateAnimation()
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    centerX = w / 2f
    centerY = h / 2f
    radius =
      (minOf(w - paddingLeft - paddingRight, h - paddingTop - paddingBottom) - trackThicknessPx) /
        2f
    drawRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    updateShaderConfiguration()
  }

  override fun onDraw(canvas: Canvas) {
    if (width == 0 || height == 0) return

    // 1. Draw Track
    canvas.drawCircle(centerX, centerY, radius, backgroundTrackPaint)

    // 2. Setup Shader Matrix (Only rotate if gradient is present)
    if (isMultiColor) {
      matrix.setRotate(gradientRotation, centerX, centerY)
      activeShader?.setLocalMatrix(matrix)
    } else {
      activeShader?.setLocalMatrix(null)
    }

    // 3. Draw Indicator Arc
    if (isIndeterminate) {
      canvas.drawArc(
        drawRect,
        /* startAngle = */ -90f + indeterminateOffset,
        /* sweepAngle = */ indeterminateSweep,
        /* useCenter = */ false,
        progressPaint,
      )
    } else {
      canvas.drawArc(
        drawRect,
        /* startAngle = */ -90f,
        /* sweepAngle = */ (progress / 100f) * 360f,
        /* useCenter = */ false,
        progressPaint,
      )
    }
  }

  override fun onSaveInstanceState(): Parcelable {
    return Bundle().apply {
      putParcelable(STATE_SUPER_STATE, super.onSaveInstanceState())
      putFloat(STATE_PROGRESS, progress)
      putBoolean(STATE_IS_INDETERMINATE, isIndeterminate)
      putIntArray(STATE_INDICATOR_COLORS, indicatorColors)
      putInt(STATE_TRACK_COLOR, trackColor)
      putFloat(STATE_TRACK_THICKNESS_DP, trackThicknessDp)
    }
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    var viewState = state
    if (viewState is Bundle) {
      progress = viewState.getFloat(STATE_PROGRESS)
      isIndeterminate = viewState.getBoolean(STATE_IS_INDETERMINATE)
      indicatorColors = viewState.getIntArray(STATE_INDICATOR_COLORS) ?: indicatorColors
      trackColor = viewState.getInt(STATE_TRACK_COLOR)
      trackThicknessDp = viewState.getFloat(STATE_TRACK_THICKNESS_DP, 4f)

      backgroundTrackPaint.color = trackColor
      syncThickness()
      viewState = BundleCompat.getParcelable(viewState, STATE_SUPER_STATE, Parcelable::class.java)
    }
    super.onRestoreInstanceState(viewState)
    updateShaderConfiguration()
  }

  private companion object {
    const val STATE_SUPER_STATE = "superState"
    const val STATE_PROGRESS = "progress"
    const val STATE_IS_INDETERMINATE = "isIndeterminate"
    const val STATE_INDICATOR_COLORS = "indicatorColors"
    const val STATE_TRACK_COLOR = "trackColor"
    const val STATE_TRACK_THICKNESS_DP = "trackThicknessDp"
  }
}
