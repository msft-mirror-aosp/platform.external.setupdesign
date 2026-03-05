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

package com.google.android.setupdesign.template

import android.content.Context
import android.graphics.PorterDuff.Mode
import android.util.AttributeSet
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewStub
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.color.MaterialColors
import com.google.android.setupcompat.PartnerCustomizationLayout
import com.google.android.setupcompat.internal.TemplateLayout
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper
import com.google.android.setupcompat.restore.restoreprogress.ProgressUiType
import com.google.android.setupcompat.template.Mixin
import com.google.android.setupcompat.util.Logger
import com.google.android.setupdesign.FeatureHighlightPopup
import com.google.android.setupdesign.R
import com.google.android.setupdesign.data.SetupProgressUiData
import com.google.android.setupdesign.data.SetupProgressViewModel
import com.google.android.setupdesign.view.GradientCircularProgressIndicator
import com.google.android.setupdesign.view.SetupProgressBottomSheet
import kotlinx.coroutines.launch

/**
 * A Mixin that manages the floating setup progress indicator. Observes [SetupProgressViewModel]
 * which provides [SetupProgressUiData] to update the indicator.
 */
class FloatingSetupProgressIndicatorMixin(
  private val templateLayout: TemplateLayout,
  attrs: AttributeSet?,
  defStyleAttr: Int,
) : Mixin {

  private val context: Context = templateLayout.context
  private val isOneTapEnabled by lazy { PartnerConfigHelper.isOneTapEnabled(context) }

  private var indicatorContainer: View? = null
  private var gradientIndicator: GradientCircularProgressIndicator? = null
  private var progressIconView: ImageView? = null
  private var indicatorClickTarget: FrameLayout? = null
  private var currentBottomSheet: SetupProgressBottomSheet? = null
  private var setupProgressViewModel: SetupProgressViewModel? = null
  private var showSetupProgressIndicator: Boolean = true
  private var featureHighlightPopup: FeatureHighlightPopup? = null

  init {
    context.withStyledAttributes(
      set = attrs,
      attrs = R.styleable.SudSetupProgressIndicatorMixin,
      defStyleAttr = defStyleAttr,
      defStyleRes = 0,
    ) {
      showSetupProgressIndicator =
        getBoolean(R.styleable.SudSetupProgressIndicatorMixin_sudShowSetupProgressIndicator, true)
    }
  }

  /** Hides the indicator. */
  fun hideIndicator() {
    showSetupProgressIndicator = false
    indicatorContainer?.isVisible = false
  }

  /** Should be called when the layout that contains the indicator is attached to a window. */
  fun onAttachedToWindow() {
    logger.atInfo("onAttachedToWindow")

    if (!showSetupProgressIndicator) {
      logger.atInfo("Hiding indicator as showSetupProgressIndicator is false")
      return
    }

    // Keep this after showSetupProgressIndicator as this is an IPC call.
    if (!isOneTapEnabled) {
      logger.atInfo("Hiding indicator as One Tap is disabled")
      return
    }

    setupProgressViewModel = SetupProgressViewModel.getViewModel(context)
    if (setupProgressViewModel == null) {
      logger.atInfo("Cannot get ViewModel, hiding indicator")
      return
    }

    val viewLifecycleOwner = templateLayout.findViewTreeLifecycleOwner()
    if (viewLifecycleOwner == null) {
      logger.atInfo("Hiding indicator as viewLifecycleOwner is null")
      return
    }

    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        setupProgressViewModel?.uiState?.collect { uiState -> updateProgressUI(uiState) }
      }
    }
  }

  private fun ensureIndicatorInflated() {
    if (indicatorContainer != null) {
      return
    }
    indicatorContainer =
      templateLayout.findManagedViewById(
        R.id.sud_layout_floating_setup_progress_indicator_container
      )

    val indicatorView = getIndicator()
    if (indicatorView == null) {
      logger.e("Indicator view is null after inflation attempt")
      return
    }

    gradientIndicator = indicatorView.findViewById(R.id.sud_setup_progress_gradient_indicator)
    progressIconView = indicatorView.findViewById(R.id.sud_setup_progress_indicator_icon)
    indicatorClickTarget = indicatorView as FrameLayout?

    indicatorClickTarget?.setOnClickListener {
      setupProgressViewModel?.notifyProgressIndicatorClicked()
      showBottomSheet()
    }
  }

  private fun getIndicator(): View? {
    val indicatorView = findIndicator()
    if (indicatorView != null) return indicatorView

    val viewStub =
      templateLayout.findManagedViewById<View>(R.id.sud_floating_setup_progress_indicator_stub)
        as ViewStub?
    if (viewStub == null) {
      logger.w("ViewStub not found")
      return null
    }

    return try {
      viewStub.layoutInflater = LayoutInflater.from(context)
      viewStub.inflate()
      findIndicator()
    } catch (e: InflateException) {
      logger.w("Incorrect theme: $e")
      null
    }
  }

  private fun findIndicator(): View? =
    templateLayout.findManagedViewById(R.id.sud_floating_setup_progress_indicator)

  private fun showBottomSheet() {
    if (currentBottomSheet?.isShowing == true) {
      return
    }
    val activity = PartnerCustomizationLayout.lookupActivityFromContext(context)
    if (activity == null) {
      logger.e("Cannot show BottomSheet. Context is not an Activity.")
      return
    }
    try {
      currentBottomSheet =
        SetupProgressBottomSheet(activity, checkNotNull(setupProgressViewModel)).also { it.show() }
    } catch (e: WindowManager.BadTokenException) {
      logger.e("Failed to show BottomSheet", e)
    } catch (e: IllegalStateException) {
      logger.e("Failed to show BottomSheet", e)
    }
  }

  private fun updateProgressUI(uiState: SetupProgressUiData) {
    if (!showSetupProgressIndicator) {
      indicatorContainer?.isVisible = false
      return
    }
    ensureIndicatorInflated()

    if (uiState.progressUiType == ProgressUiType.UNKNOWN) {
      logger.w("ProgressUiType not set. Not updating progress UI.")
      return
    }

    if (uiState.progressUiType == ProgressUiType.NONE) {
      indicatorContainer?.isVisible = false
      return
    }

    indicatorContainer?.isVisible = true
    gradientIndicator?.updateGradientIndicator(uiState)
    progressIconView?.updateIndicatorIcon(uiState)
    inflateTooltip(uiState)
  }

  private fun GradientCircularProgressIndicator.updateGradientIndicator(
    uiState: SetupProgressUiData
  ) {
    isIndeterminate = uiState.progressIndeterminate
    progress = uiState.progressValue.toFloat()

    when (uiState.progressUiType) {
      ProgressUiType.DEFAULT_SOLID ->
        indicatorColors = intArrayOf(ContextCompat.getColor(this.context, R.color.sud_color_primary))
      ProgressUiType.DEFAULT_GRADIENT ->
        indicatorColors =
          intArrayOf(
            ContextCompat.getColor(this.context, R.color.sud_color_primary),
            ContextCompat.getColor(this.context, R.color.sud_color_secondary),
            ContextCompat.getColor(this.context, R.color.sud_color_primary_container),
          )
      ProgressUiType.ACTIONABLE_GRADIENT ->
        indicatorColors =
          intArrayOf(
            ContextCompat.getColor(this.context, R.color.sud_color_tertiary_container),
            ContextCompat.getColor(this.context, R.color.sud_color_on_tertiary),
          )
      ProgressUiType.ERROR_SOLID -> indicatorColors = intArrayOf(this.getColor(android.R.attr.colorError))
      else -> logger.e("Unsupported ProgressUiType: ${uiState.progressUiType}")
    }
  }

  private fun inflateTooltip(uiState: SetupProgressUiData) {
    if (setupProgressViewModel?.persistToolTip == true || uiState.toolTipVisible) {
      findIndicator()?.let { anchorView ->
        if (featureHighlightPopup == null) {
          featureHighlightPopup =
            FeatureHighlightPopup(
              anchorView,
              clippingEnabled = false,
              primaryText = uiState.toolTipTitle,
              secondaryText = uiState.toolTipDescription,
              primaryButtonText = uiState.toolTipButtonText,
              dismissCallback = { setupProgressViewModel?.notifyToolTipDismissed() },
            )
        }
        // Do not display if already visible on screen
        if (featureHighlightPopup?.isToolTipVisible() == false) {
          setupProgressViewModel?.notifyTooltipShown()
          featureHighlightPopup?.showPopup()
        }
      }
    }
  }

  private fun ImageView.updateIndicatorIcon(uiState: SetupProgressUiData) {
    isVisible = uiState.progressIcon != null

    if (uiState.progressIcon != null) {
      setImageBitmap(uiState.progressIcon)
    }

    val iconColor = getIconColor(this, uiState)
    if (iconColor == 0) {
      // 0 indicates "No color found in theme"
      // Remove any existing tint so the icon shows its original drawable colors.
      clearColorFilter()
    } else {
      setColorFilter(iconColor, Mode.SRC_IN)
    }
  }

  private fun getIconColor(view: View, uiState: SetupProgressUiData): Int {
    return when (uiState.progressUiType) {
      ProgressUiType.DEFAULT_SOLID ->
        ContextCompat.getColor(view.context, R.color.sud_color_primary)
      ProgressUiType.DEFAULT_GRADIENT ->
        ContextCompat.getColor(view.context, R.color.sud_color_on_surface)
      ProgressUiType.ACTIONABLE_GRADIENT ->
        ContextCompat.getColor(view.context, R.color.sud_color_tertiary)
      ProgressUiType.ERROR_SOLID -> view.getColor(android.R.attr.colorError)
      else -> 0 // This should never happen. Return 0 (TRANSPARENT Color) to not set any color.
    }
  }

  private fun View.getColor(attr: Int): Int {
    return try {
      MaterialColors.getColor(this, attr)
    } catch (e: Exception) {
      logger.e("Failed to get color for attribute: $attr", e)
      0 // Return 0 (TRANSPARENT Color) to not set any color.
    }
  }

  /** Should be called when the layout that contains the indicator is detached from a window. */
  fun onDetachFromWindow() {
    // We would want to show the tool tip again if this is triggered from orientation change. If
    // this is not triggered from Orientation change, tool tip would have handled dismiss with
    // callback as expected behavior.
    featureHighlightPopup?.dismissWithoutCallback()
    featureHighlightPopup = null
  }

  companion object {
    private val logger = Logger("SetupProgressIndicatorMixin")
  }
}
