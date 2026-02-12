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
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.color.MaterialColors
import com.google.android.material.loadingindicator.LoadingIndicator
import com.google.android.setupcompat.PartnerCustomizationLayout
import com.google.android.setupcompat.internal.TemplateLayout
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper
import com.google.android.setupcompat.restore.restoreprogress.ProgressUiType
import com.google.android.setupcompat.template.Mixin
import com.google.android.setupcompat.util.Logger
import com.google.android.setupdesign.R
import com.google.android.setupdesign.data.SetupProgressUiData
import com.google.android.setupdesign.data.SetupProgressViewModel
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
  private var progressIndeterministicView: LoadingIndicator? = null
  private var progressTextView: TextView? = null
  private var progressIconView: ImageView? = null
  private var indicatorClickTarget: FrameLayout? = null
  private var currentBottomSheet: SetupProgressBottomSheet? = null
  private var setupProgressViewModel: SetupProgressViewModel? = null
  private var showSetupProgressIndicator: Boolean = true

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
    progressIndeterministicView =
      indicatorView.findViewById(R.id.sud_setup_progress_indicator_indeterminate)
    progressTextView = indicatorView.findViewById(R.id.sud_setup_progress_indicator_text)
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
      val inflater = LayoutInflater.from(context)
      viewStub.layoutInflater = inflater
      viewStub.inflate()
      findIndicator()
    } catch (e: InflateException) {
      logger.w("Incorrect theme: $e")
      null
    }
  }

  private fun findIndicator(): View? {
    return templateLayout.findManagedViewById<View?>(R.id.sud_floating_setup_progress_indicator)
  }

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
    ensureIndicatorInflated()

    progressIndeterministicView?.isVisible = false
    progressTextView?.isVisible = false
    progressIconView?.isVisible = false
    indicatorContainer?.isVisible = true

    when (uiState.progressUiType) {
      ProgressUiType.INDETERMINATE_PROGRESS -> {
        progressIndeterministicView?.isVisible = true
      }
      ProgressUiType.DETERMINATE_PROGRESS -> {
        progressTextView?.apply {
          isVisible = true
          text =
            context.getString(
              com.google.android.setupdesign.strings.R.string.sud_setup_progress_percentage,
              uiState.progressValue,
            )
        }
      }
      ProgressUiType.ICON -> {
        if (uiState.icon != null) {
          progressIconView?.apply {
            isVisible = true
            setImageBitmap(uiState.icon)
            if (uiState.iconTint != -1) {
              setColorFilter(uiState.iconTint, Mode.SRC_IN)
            } else {
              val tintColor = MaterialColors.getColor(this, android.R.attr.colorPrimary)
              setColorFilter(tintColor, Mode.SRC_IN)
            }
          }
        } else {
          logger.w("ICON type specified but no icon resource provided")
        }
      }
      ProgressUiType.NONE -> {
        indicatorContainer?.isVisible = false
      }
      else -> {
        logger.w("Unknown ProgressUiType: ${uiState.progressUiType}")
      }
    }
  }

  companion object {
    private val logger = Logger("SetupProgressIndicatorMixin")
  }
}
