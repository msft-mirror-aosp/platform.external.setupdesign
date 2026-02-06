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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff.Mode
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.setupcompat.util.Logger
import com.google.android.setupdesign.R
import com.google.android.setupdesign.data.SetupProgressUiData
import com.google.android.setupdesign.data.SetupProgressViewModel
import kotlinx.coroutines.launch

/**
 * A BottomSheetDialog to display the progress of a restore operation. Observes
 * [SetupProgressViewModel] for [SetupProgressUiData] updates.
 */
class SetupProgressBottomSheet(context: Context, private val viewModel: SetupProgressViewModel) :
  BottomSheetDialog(context, R.style.SudThemeOverlay_BottomSheetDialog_Rounded) {
  private var binding: ViewBinding? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sud_setup_progress_bottom_sheet)
    val rootView = findViewById<View>(R.id.sud_setup_progress_bottom_sheet)
    val viewBinding = ViewBinding(checkNotNull(rootView))
    viewBinding.dismissButton.setOnClickListener { dismiss() }
    binding = viewBinding
  }

  override fun onStart() {
    super.onStart()
    // Start collecting updates when the dialog becomes visible
    setupViewModelObservation()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    binding = null
  }

  private fun setupViewModelObservation() {
    val viewLifecycleOwner = binding?.root?.findViewTreeLifecycleOwner()
    if (viewLifecycleOwner == null) {
      logger.e("ViewLifecycleOwner is null. Not observing ViewModel.")
      return
    }

    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state -> updateViews(state) }
      }
    }
  }

  private fun updateViews(state: SetupProgressUiData) {
    binding?.apply {
      // Icon
      val iconTint =
        if (state.bottomSheetIconTint == -1) {
          iconView.getColor(android.R.attr.colorPrimary)
        } else {
          state.bottomSheetIconTint
        }
      iconView.setBitmapAndVisibility(state.bottomSheetIcon, iconTint)

      // Title and Description
      titleView.setTextAndVisibility(state.bottomSheetTitle)
      descriptionView.setTextAndVisibility(state.bottomSheetDescription)

      // Progress Bar
      updateProgressBar(state.bottomSheetProgressBarVisible, state.progressValue)

      // Card
      updateCard(
        state.bottomSheetCardVisible,
        state.bottomSheetCardIcon,
        state.bottomSheetCardTitle,
        state.bottomSheetCardDescription,
      )

      // Button
      dismissButton.setTextAndVisibility(state.bottomSheetButtonText)
    }
  }

  private fun View.getColor(colorAttr: Int): Int {
    val array = context.obtainStyledAttributes(intArrayOf(colorAttr))
    return try {
      array.getColor(/* index= */ 0, /* defValue= */ -1)
    } finally {
      array.recycle()
    }
  }

  private fun ImageView.setBitmapAndVisibility(bitmap: Bitmap?, tint: Int = -1) {
    if (bitmap != null) {
      setImageBitmap(bitmap)
    }
    if (tint != -1) {
      setColorFilter(tint, Mode.SRC_IN)
    }
    isVisible = bitmap != null
  }

  private fun TextView.setTextAndVisibility(newText: String?) {
    if (!newText.isNullOrEmpty()) {
      text = newText
    }
    isVisible = !newText.isNullOrEmpty()
  }

  private fun updateProgressBar(isProgressBarVisible: Boolean, progressValue: Int) {
    binding?.apply {
      progressBarContainer.isVisible = isProgressBarVisible
      if (isProgressBarVisible) {
        progressBar.progress = progressValue
        progressPercentageView.text =
          context.resources.getString(
            com.google.android.setupdesign.strings.R.string.sud_setup_progress_percentage,
            progressValue,
          )
      }
    }
  }

  private fun updateCard(
    isCardVisible: Boolean,
    cardIcon: Bitmap?,
    cardTitle: String?,
    cardDescription: String?,
  ) {
    binding?.apply {
      cardContainer.isVisible = isCardVisible
      cardIconView.setBitmapAndVisibility(cardIcon)
      cardTitleView.setTextAndVisibility(cardTitle)
      cardDescriptionView.setTextAndVisibility(cardDescription)
    }
  }

  @VisibleForTesting
  class ViewBinding(val root: View) {
    val iconView: ImageView = root.findViewById(R.id.sud_setup_progress_bottom_sheet_icon)
    val titleView: TextView = root.findViewById(R.id.sud_setup_progress_bottom_sheet_title)
    val descriptionView: TextView =
      root.findViewById(R.id.sud_setup_progress_bottom_sheet_description)
    val progressBarContainer: LinearLayout =
      root.findViewById(R.id.sud_setup_progress_bottom_sheet_progress_bar_container)
    val progressBar: LinearProgressIndicator =
      root.findViewById(R.id.sud_setup_progress_bottom_sheet_progress_bar)
    val progressPercentageView: TextView =
      root.findViewById(R.id.sud_setup_progress_bottom_sheet_progress_percentage)
    val cardContainer: MaterialCardView =
      root.findViewById(R.id.sud_setup_progress_bottom_sheet_card_view)
    val cardIconView: ImageView = root.findViewById(R.id.sud_setup_progress_bottom_sheet_card_icon)
    val cardTitleView: TextView = root.findViewById(R.id.sud_setup_progress_bottom_sheet_card_title)
    val cardDescriptionView: TextView =
      root.findViewById(R.id.sud_setup_progress_bottom_sheet_card_description)
    val dismissButton: Button =
      root.findViewById(R.id.sud_setup_progress_bottom_sheet_dismiss_button)
  }

  companion object {
    const val TAG = "SetupProgressBottomSheet"
    private val logger = Logger("SetupProgressBottomSheet")
  }
}
