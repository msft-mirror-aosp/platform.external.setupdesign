package com.google.android.setupdesign

import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.google.android.setupdesign.view.ToolTipOverlayView

/**
 * A popup that highlights a specific feature by showing a tooltip over an [anchor] view.
 *
 * This popup can be configured with [primaryText], [secondaryText], and [primaryButtonText] to
 * provide information to the user. [isClippingEnabled] determines if the popup is clipped to the
 * screen bounds. A [dismissCallback] can be provided to be notified when the popup is dismissed.
 */
class FeatureHighlightPopup(
  private val anchor: View,
  private val clippingEnabled: Boolean = true,
  private val primaryText: String? = null,
  private val secondaryText: String? = null,
  private val primaryButtonText: String? = null,
  private val dismissCallback: () -> Unit = {},
) {
  private var popUpWindow: PopupWindow? = null

  /** Returns true if the feature highlight popup is currently visible. */
  fun isToolTipVisible() = popUpWindow?.isShowing ?: false

  /** Inflates and shows the feature highlight popup over the associated [anchor] view. */
  fun showPopup() {
    val context = anchor.context
    // Inflate tooltipView in PopupWindow.
    popUpWindow =
      PopupWindow(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

    // Create the view for Tooltip overlay.
    val toolTipView = ToolTipOverlayView(context)
    toolTipView.anchorView = anchor
    toolTipView.onDismissRequested = { popUpWindow?.dismiss() }
    primaryText?.let { toolTipView.setPrimaryText(it) }
    secondaryText?.let { toolTipView.setSecondaryText(it) }
    primaryButtonText?.let { toolTipView.setPrimaryButtonText(it) }

    // Set properties of popUpWindow
    popUpWindow?.apply {
      contentView = toolTipView
      isFocusable = true
      isClippingEnabled = clippingEnabled
      setOnDismissListener {
        popUpWindow = null
        dismissCallback.invoke()
      }
    }

    // Show popUpWindow.
    // Since our FeatureHighlight takes the entire screen, no need to set x and y.
    popUpWindow?.showAtLocation(anchor, Gravity.NO_GRAVITY, 0, 0)
  }

  /**
   * Dismisses the feature highlight popup if it is currently visible. Should mostly be called if
   * orientation changes because if Popup is focusable, popup itself handles the back button.
   */
  fun dismissWithoutCallback() {
    popUpWindow?.setOnDismissListener { popUpWindow = null }
    popUpWindow?.dismiss()
  }
}
