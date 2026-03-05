package com.google.android.setupdesign

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.graphics.shapes.transformed
import kotlin.math.cos
import kotlin.math.sin

/**
 * A custom [Drawable] that draws a "blob" shape using [RoundedPolygon].
 *
 * This drawable supports a shrinking/growing animation via [blobScale] and a circular cutout at
 * ([cutoutCx], [cutoutCy]) with a radius of [cutoutRadius].
 */
class ToolTipBlobDrawable : Drawable() {

  private val shape =
    RoundedPolygon.star(
        numVerticesPerRadius = NUM_VERTICES_PER_RADIUS,
        radius = BASE_RADIUS, // Base radius for the shape
        innerRadius = BASE_INNER_RADIUS, // Base inner radius
        rounding = CornerRounding(CORNER_ROUNDING_RADIUS, 0f),
      )
      .transformed(createRotationalMatrix(SHAPE_ROTATION_DEGREES))

  /** The scale factor for the blob animation (0f to 1f). */
  var blobScale: Float = 0f
    set(value) {
      field = value
      updatePathTransform(bounds) // Re-calculate path on every animation frame
      invalidateSelf() // Force a redraw
    }

  /** The X coordinate of the center of the cutout hole. */
  var cutoutCx: Float = 0f

  /** The Y coordinate of the center of the cutout hole. */
  var cutoutCy: Float = 0f

  /** Animation start angle for the drawable. */
  var alignmentAngleDeg: Double = 0.0

  /** The radius of the cutout hole. */
  var cutoutRadius: Float = 0f
    set(value) {
      field = value
      updatePathTransform(bounds)
      invalidateSelf()
    }

  private fun createRotationalMatrix(deg: Float): Matrix {
    return Matrix().apply { setRotate(deg) }
  }

  private val basePath = shape.toPath() // The original path at base scale
  private val drawPath = Path() // Path to be drawn, transformed to fit bounds
  private val shapeBounds = RectF()
  private val transformMatrix = Matrix()

  private val paint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
  }

  init {
    basePath.computeBounds(shapeBounds, true)
  }

  override fun onBoundsChange(bounds: Rect) {
    super.onBoundsChange(bounds)
    updatePathTransform(bounds)
  }

  private fun createAndSetPath(bounds: Rect) {
    val boundsF = RectF(bounds)

    // Scale based on the LARGEST dimension so it covers dynamic text without distorting the
    // perfectly circular 12-sided shape.
    val targetSize = boundsF.width().coerceAtLeast(boundsF.height())
    val baseScale = targetSize / shapeBounds.width().coerceAtLeast(shapeBounds.height())

    transformMatrix.reset()

    // 2. Center to origin and scale to final 100% size
    transformMatrix.postTranslate(-shapeBounds.centerX(), -shapeBounds.centerY())
    transformMatrix.postScale(baseScale, baseScale)

    // 3. Keep the shape perfectly centered in the View's dynamic bounds
    val cx = boundsF.centerX()
    val cy = boundsF.centerY()
    transformMatrix.postTranslate(cx, cy)

    // 4. Calculate the Pivot Point for the animation (30 degrees from Horizontal)
    val radius = targetSize / 2f
    val angleRadians = Math.toRadians(alignmentAngleDeg)

    val pivotX = cx + (radius * sin(angleRadians)).toFloat()
    val pivotY = cy - (radius * cos(angleRadians)).toFloat()

    // 5. Apply the animation scale from that exact petal!
    transformMatrix.postScale(blobScale, blobScale, pivotX, pivotY)

    // Apply the transformation
    basePath.transform(transformMatrix, drawPath)

    if (cutoutRadius > 0f) {
      val cutoutPath =
        Path().apply { addCircle(cutoutCx, cutoutCy, cutoutRadius, Path.Direction.CW) }
      // Punches the hole exactly where we tell it to
      drawPath.op(cutoutPath, Path.Op.DIFFERENCE)
    }
  }

  private fun updatePathTransform(bounds: Rect) {
    if (bounds.isEmpty || shapeBounds.isEmpty) {
      drawPath.reset()
      return
    }
    createAndSetPath(bounds)
  }

  override fun draw(canvas: Canvas) {
    if (!drawPath.isEmpty) {
      canvas.drawPath(drawPath, paint)
    }
  }

  @Deprecated("Deprecated in framework") override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
    invalidateSelf() // Request a redraw when alpha changes
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    paint.colorFilter = colorFilter
    invalidateSelf() // Request a redraw when color filter changes
  }

  companion object {
    private const val NUM_VERTICES_PER_RADIUS = 12
    private const val BASE_RADIUS = 1f
    private const val BASE_INNER_RADIUS = 0.8f
    private const val CORNER_ROUNDING_RADIUS = 0.5f
    private const val SHAPE_ROTATION_DEGREES = -90f
  }
}
