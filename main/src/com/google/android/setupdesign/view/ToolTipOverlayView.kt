package com.google.android.setupdesign.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.setupcompat.util.Logger
import com.google.android.setupdesign.R
import com.google.android.setupdesign.ToolTipBlobDrawable
import com.google.android.setupdesign.util.DrawableLayoutDirectionHelper
import kotlin.math.cos
import kotlin.math.sin

/**
 * A custom [FrameLayout] that displays a tooltip overlay over an [anchorView].
 *
 * This view handles the positioning and animation of the tooltip, ensuring it SITs correctly
 * relative to the anchor view and includes a cutout to reveal the anchor.
 */
class ToolTipOverlayView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  FrameLayout(context, attrs, defStyleAttr) {

  lateinit var anchorView: View

  private var parentView: FrameLayout
  /** A callback invoked when the tooltip overlay is requested to be dismissed. */
  var onDismissRequested: (() -> Unit)? = null

  init {
    LayoutInflater.from(context).inflate(R.layout.sud_setup_tooltip_overlay, this, true)
    parentView = this.findViewById(R.id.tool_tip_container)
    val tooltipButton = findViewById<MaterialButton>(R.id.button_primary_tooltip)

    tooltipButton.setOnClickListener {
      tooltipButton.isClickable = false
      animateOut()
    }
    this.setOnClickListener {
      this.isClickable = false
      animateOut()
    }
    parentView.isClickable = true
  }

  /** Sets the primary title text in the tooltip. */
  fun setPrimaryText(textString: String) {
    this.findViewById<RichTextView>(R.id.text_primary_tooltip)?.text = textString
  }

  /** Sets the secondary summary text in the tooltip. */
  fun setSecondaryText(textString: String) {
    this.findViewById<RichTextView>(R.id.text_secondary_tooltip)?.text = textString
  }

  /** Sets the text for the primary action button in the tooltip. */
  fun setPrimaryButtonText(textString: String) {
    this.findViewById<MaterialButton>(R.id.button_primary_tooltip)?.text = textString
  }

  private val isRtl by lazy { DrawableLayoutDirectionHelper.isRtl(this) }

  /**
   * Intercepts the view hierarchy layout phase right before the first frame is drawn.
   * * This method is responsible for sizing the tooltip container, aligning a specific "petal" of
   *   drawable to point at the anchor view.
   * * This method also configuring the exact coordinates to punch a transparent hole over the
   *   target to make the anchor visible which exists behind this view.
   *
   * It executes in 4 distinct phases:
   * =================================================================================
   * PHASE 1: Forcing a Perfect Square (The Two-Pass Layout)
   * =================================================================================
   * The [ToolTipBlobDrawable] draws a mathematically perfect radial shape. If the text inside
   * forces this container to be rectangular, the drawable will scale up to fit the text, but the
   * container's bounds will clip its top/bottom edges.
   * =================================================================================
   * PHASE 2: Screen Coordinate Mapping
   * =================================================================================
   * We capture the absolute screen positions of both the target (Gear Icon) and this full-screen
   * overlay to calculate the target's relative X/Y.
   * =================================================================================
   * PHASE 3: Trigonometric Alignment
   * =================================================================================
   * We want the tooltip to sit above and to the left of the target. For our use case we calculate
   * the angle as 360 / 12 = 30. With the 30-degree petal pointing directly at it, leaving a small
   * visual gap. This class can later be made to put the customization for drawable shape and angle
   * from the client.
   *
   * We use Sine and Cosine to calculate the exact X/Y offset of that petal relative to the center
   * of our square container, then shift the container backward along that exact same 30-degree
   * trajectory.
   * =================================================================================
   * PHASE 4: Configuring the Cutout Hole
   * =================================================================================
   * Finally, we calculate where the center of the anchor view lives *relative to the top-left
   * corner of our newly positioned square container*. We pass these local coordinates to the
   * background drawable so it can use `Path.Op.DIFFERENCE` to punch a hole exactly over the Gear
   * Icon.
   *
   * @return `true` to proceed with drawing the frame, or `false` to cancel the current draw frame
   *   and wait for a re-layout.
   */
  override fun onAttachedToWindow() {
    logger.atInfo("onAttachToWindow")
    super.onAttachedToWindow()
    viewTreeObserver.addOnPreDrawListener(
      object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
          if (!::anchorView.isInitialized || !anchorView.isAttachedToWindow) {
            logger.w("Anchor view is not initialized or not attached to window, skipping layout")
            return true
          }
          // Force the container to be a perfect square so the drawable doesn't clip
          val currentWidth = parentView.width
          val currentHeight = parentView.height
          val maxSize = currentWidth.coerceAtLeast(currentHeight)

          if (currentWidth != maxSize || currentHeight != maxSize) {
            val params = parentView.layoutParams
            params.width = maxSize
            params.height = maxSize
            parentView.layoutParams = params
            return false // Wait for the new square layout to finish
          }
          // If we are here, parentView is a perfect square!
          viewTreeObserver.removeOnPreDrawListener(this)

          // Get ABSOLUTE screen coordinates of the anchor view
          val targetLoc = IntArray(2)
          anchorView.getLocationOnScreen(targetLoc)
          val anchorWidth = anchorView.width
          val anchorHeight = anchorView.height

          // Get the absolute coordinates of this full-screen overlay
          val myLoc = IntArray(2)
          this@ToolTipOverlayView.getLocationOnScreen(myLoc)

          // Translate to RELATIVE coordinates inside the overlay
          val relativeAnchorX = targetLoc[0] - myLoc[0]
          val relativeAnchorY = targetLoc[1] - myLoc[1]

          val anchorCenterX = relativeAnchorX + (anchorWidth / 2f)
          val anchorCenterY = relativeAnchorY + (anchorHeight / 2f)

          // Position the Cookie Container
          val cookieRadius = maxSize / 2f
          val angleDegree =
            if (isRtl) {
              -1 * ALIGNMENT_ANGLE_DEGREES
            } else {
              ALIGNMENT_ANGLE_DEGREES
            }
          val angleRadians = Math.toRadians(angleDegree)

          // Define the diagonal gap between the petal and the gear
          val paddingPx = resources.getDimension(R.dimen.sud_setup_tooltip_diagonal_padding)
          val anchorRadius = anchorWidth / 2f

          // Calculate the diagonal shift magnitude (R - r - padding)
          val shiftMagnitude = cookieRadius - anchorRadius - paddingPx

          // Apply the shifts to perfectly position the cookie container!
          // x = p - R - shift * sin(30)
          // y = q - R + shift * cos(30)
          parentView.x =
            anchorCenterX - cookieRadius - (shiftMagnitude * sin(angleRadians)).toFloat()
          parentView.y =
            anchorCenterY - cookieRadius + (shiftMagnitude * cos(angleRadians)).toFloat()

          // CONFIGURE THE CUTOUT HOLE
          // Calculate where the center of the anchor is relative to the cookie's top-left corner
          val relativeCutoutX = anchorCenterX - parentView.x
          val relativeCutoutY = anchorCenterY - parentView.y

          // TODO: b/489021596 - Once the status of progress indicator can be
          // updated in the tooltips view, It should directly be added in the tooltips view and
          // remove the cutout hole.
          // Grab the drawable and pass it the exact hole coordinates and radius
          val blobDrawable = parentView.background as? ToolTipBlobDrawable
          blobDrawable?.apply {
            alignmentAngleDeg = angleDegree
            cutoutCx = relativeCutoutX
            cutoutCy = relativeCutoutY
            // Make the hole
            cutoutRadius = (anchorWidth / 2f) - anchorView.paddingStart
          }

          // Start the enter animation
          startEnterAnimation()
          return true
        }
      }
    )
    parentView.background =
      ToolTipBlobDrawable().apply {
        val color = ContextCompat.getColor(context, R.color.sud_color_secondary_container)
        colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
      }
  }

  private fun startEnterAnimation() {
    val blobDrawable = parentView.background as? ToolTipBlobDrawable ?: return
    // Set initial state
    blobDrawable.blobScale = 0f

    // Animate the Blob Background
    ValueAnimator.ofFloat(0f, 1f).apply {
      duration = ENTER_ANIMATION_DURATION
      interpolator = OvershootInterpolator(0f)
      addUpdateListener { animator -> blobDrawable.blobScale = animator.animatedValue as Float }
      start()
    }
  }

  /**
   * Triggers the exit animation for the tooltip and invokes [onDismissRequested] upon completion.
   */
  fun animateOut() {
    val blobDrawable = parentView.background as? ToolTipBlobDrawable ?: return

    // 1. Shrink the background using AnticipateInterpolator (pulls back before shrinking)
    ValueAnimator.ofFloat(blobDrawable.blobScale, 0f).apply {
      duration = EXIT_ANIMATION_DURATION
      interpolator = AnticipateInterpolator()
      addUpdateListener { animator -> blobDrawable.blobScale = animator.animatedValue as Float }
      addListener(
        object : AnimatorListenerAdapter() {

          override fun onAnimationStart(animation: Animator) {
            super.onAnimationStart(animation)
          }

          override fun onAnimationEnd(animation: Animator) {
            onDismissRequested?.invoke()
          }
        }
      )
      start()
    }

    // Quickly fade out the text and fake progress bar so they don't linger
    parentView.animate().alpha(0f).setDuration(EXIT_ANIMATION_DURATION / 2).start()
  }

  companion object {
    private val logger = Logger("ToolTipOverlayView")
    private const val ENTER_ANIMATION_DURATION = 800L
    private const val EXIT_ANIMATION_DURATION = 800L
    private const val ALIGNMENT_ANGLE_DEGREES = 30.0
  }
}
