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

package com.google.android.setupdesign.data

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.google.android.setupcompat.restore.restoreprogress.ProgressUiType
import com.google.android.setupcompat.restore.restoreprogress.RestoreProgressUiData
import com.google.android.setupcompat.util.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Manages and exposes the [SetupProgressUiData], derived from the [RestoreProgressUiData] state in
 * [RestoreProgressRepository].
 */
class SetupProgressViewModel(private val applicationContext: Context) : ViewModel() {

  private val restoreProgressRepository by lazy { RestoreProgressRepository(applicationContext) }

  val uiState: StateFlow<SetupProgressUiData> by lazy {
    restoreProgressRepository.progressUiDataFlow
      .map { it.toSetupProgressUiData() }
      .stateIn(
        scope = viewModelScope,
        started =
          SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000, replayExpirationMillis = 0),
        initialValue = getDefaultProgressUiData(),
      )
  }

  /**
   * Whether the tooltip should be persisted across orientation changes. This is set to `true` when
   * the tooltip is shown and `false` when the tooltip is dismissed.
   */
  var persistToolTip: Boolean = false
    private set

  /** Should be called when the progress indicator in the UI is clicked. */
  fun notifyProgressIndicatorClicked() {
    viewModelScope.launch { restoreProgressRepository.notifyProgressIndicatorClicked() }
  }

  /** Should be called when the progress indicator in the UI is shown. */
  fun notifyTooltipShown() {
    persistToolTip = true
    viewModelScope.launch { restoreProgressRepository.notifyTooltipShown() }
  }

  /** Should be called when the tooltip in the UI is dismissed. */
  fun notifyToolTipDismissed() {
    persistToolTip = false
  }

  private fun RestoreProgressUiData?.toSetupProgressUiData(): SetupProgressUiData {
    if (this == null) return getDefaultProgressUiData()

    return SetupProgressUiData(
      progressUiType = this.progressUiType,
      progressIndeterminate = this.progressIndeterminate,
      progressValue = this.progressValue,
      progressIcon = getBitmap(this.progressIcon),
      bottomSheetIcon = getBitmap(this.bottomSheetIcon),
      bottomSheetTitle = this.bottomSheetTitle,
      bottomSheetDescription = this.bottomSheetDescription,
      bottomSheetButtonText = this.bottomSheetButtonText,
      bottomSheetProgressBarVisible = this.bottomSheetProgressBarVisible,
      bottomSheetCardVisible = this.bottomSheetCardVisible,
      bottomSheetCardHighlighted = this.bottomSheetCardHighlighted,
      bottomSheetCardShowIndicator = this.bottomSheetCardShowIndicator,
      bottomSheetCardIcon = getBitmap(this.bottomSheetCardIcon),
      bottomSheetCardTitle = this.bottomSheetCardTitle,
      bottomSheetCardDescription = this.bottomSheetCardDescription,
      toolTipVisible = this.toolTipVisible,
      toolTipTitle = this.toolTipTitle,
      toolTipDescription = this.toolTipDescription,
      toolTipButtonText = this.toolTipButtonText,
    )
  }

  private fun getDefaultProgressUiData() = SetupProgressUiData(ProgressUiType.NONE)

  private fun getBitmap(@DrawableRes resId: Int): Bitmap? {
    if (resId == 0) return null
    return try {
      applicationContext.packageManager
        .getDrawable(RestoreProgressRepository.SERVICE_PACKAGE, resId, /* appInfo= */ null)
        ?.toBitmapOrNull()
    } catch (e: Exception) {
      logger.e("Failed to get bitmap for resId $resId", e)
      null
    }
  }

  companion object {
    private val logger = Logger("SetupProgressViewModel")

    /**
     * Returns a [SetupProgressViewModel] from the provided [Context] hierarchy if possible, or
     * returns `null`.
     *
     * A [ViewModelStoreOwner] is required to create a [ViewModelProvider]. This function attempts
     * to find the outer-most [ViewModelStoreOwner] by unwrapping [ContextWrapper] instances in the
     * provided [Context] hierarchy.
     */
    fun getViewModel(context: Context?): SetupProgressViewModel? {
      if (context == null) {
        logger.e("Context is null. Not creating ViewModel.")
        return null
      }

      var owner: ViewModelStoreOwner? = null
      var baseContext = context
      while (baseContext is ContextWrapper) {
        if (baseContext is ViewModelStoreOwner) {
          owner = baseContext
        }
        baseContext = baseContext.baseContext
      }

      if (owner == null) {
        logger.e("Failed to get ViewModelStoreOwner. Not creating ViewModel.")
        return null
      }

      return try {
        val factory = SetupProgressViewModelFactory(context)
        ViewModelProvider(owner, factory)[SetupProgressViewModel::class.java]
      } catch (e: Exception) {
        logger.e("Failed to create SetupProgressViewModel", e)
        null
      }
    }
  }
}
