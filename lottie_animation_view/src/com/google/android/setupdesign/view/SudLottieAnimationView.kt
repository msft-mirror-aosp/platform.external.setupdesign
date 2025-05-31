/*
 * Copyright (C) 2025 The Android Open Source Project
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

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.setupcompat.util.Logger
import com.google.android.setupdesign.strings.R

/** A [LottieAnimationView] that take response to pause and resume animation when user clicks. */
class SudLottieAnimationView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null) :
  LottieAnimationView(context, attrs),
  View.OnClickListener,
  AnimatorListener,
  Animator.AnimatorPauseListener {
  var clickListener: OnClickListener? = null

  private val actionInfoAnimatorPlaying =
    buildAccessibilityAction(R.string.sud_lottie_animation_view_accessibility_action_pause)

  private val actionInfoAnimatorPaused =
    buildAccessibilityAction(R.string.sud_lottie_animation_view_accessibility_action_resume)

  init {
    super.setOnClickListener(this)
    setContentDescription(
      resources.getString(R.string.sud_lottie_animation_view_accessibility_description)
    )
    addAnimatorListener(this)
    addAnimatorPauseListener(this)
  }

  private fun buildAccessibilityAction(@StringRes stringId: Int) =
    AccessibilityActionCompat(
      AccessibilityNodeInfoCompat.ACTION_CLICK,
      resources.getString(stringId),
    )

  override fun setOnClickListener(listener: OnClickListener?) {
    clickListener = listener
  }

  private fun setAccessibilityDelegate(
    accessibilityAction: AccessibilityNodeInfoCompat.AccessibilityActionCompat
  ) {
    ViewCompat.setAccessibilityDelegate(
      this,
      object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(
          host: View,
          info: AccessibilityNodeInfoCompat,
        ) {
          super.onInitializeAccessibilityNodeInfo(host, info)
          info.addAction(accessibilityAction)
        }
      },
    )
  }

  override fun onClick(v: View) {
    clickListener?.onClick(v)
    if (isAnimating) {
      pauseAnimation()
    } else {
      resumeAnimation()
    }
  }

  override fun onAnimationPause(animation: Animator) {
    LOG.atInfo("onAnimationPause")
    setAccessibilityDelegate(actionInfoAnimatorPaused)
  }

  override fun onAnimationResume(animation: Animator) {
    LOG.atInfo("onAnimationResume")
    setAccessibilityDelegate(actionInfoAnimatorPlaying)
  }

  override fun onAnimationStart(animation: Animator) {
    LOG.atInfo("onAnimationStart")
    setAccessibilityDelegate(actionInfoAnimatorPlaying)
  }

  override fun onAnimationEnd(animation: Animator) {
    // Do nothing
  }

  override fun onAnimationCancel(animation: Animator) {
    // Do nothing
  }

  override fun onAnimationRepeat(animation: Animator) {
    // Do nothing
  }

  private companion object {
    val LOG = Logger(SudLottieAnimationView::class.java)
  }
}
