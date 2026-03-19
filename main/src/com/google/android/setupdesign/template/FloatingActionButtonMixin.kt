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

import android.util.AttributeSet
import android.util.Log
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewStub
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import com.google.android.material.button.MaterialButton
import com.google.android.setupcompat.internal.TemplateLayout
import com.google.android.setupcompat.template.Mixin
import com.google.android.setupdesign.R
import com.google.android.setupdesign.util.ItemStyler

/**
 * A [Mixin] for controlling action button on the template layout.
 *
 * @param templateLayout The template layout that this Mixin is a part of
 * @param attrs XML attributes given to the layout
 * @param defStyleAttr The default style attribute as given to the constructor of the layout
 */
class FloatingActionButtonMixin(
  private val templateLayout: TemplateLayout,
  attrs: AttributeSet?,
  defStyleAttr: Int,
) : Mixin {
  private var listener: OnClickListener? = null

  @VisibleForTesting var tryInflatingActionButton: Boolean = false

  val actionButton: MaterialButton?
    /**
     * Check the action button exist or not. If exists, return the button. Otherwise try to inflate
     * it and check again.
     */
    get() {
      // Try to inflate the action button if it's not inflated before.
      if (!tryInflatingActionButton) {
        tryInflatingActionButton = true
        val buttonViewStub =
          templateLayout.findManagedViewById(R.id.sud_floating_action_button_stub) as ViewStub?
            ?: return null
        try {
          inflateButton(buttonViewStub)
        } catch (e: InflateException) {
          Log.w(TAG, "Incorrect theme", e)
          return null
        }
      }
      return findActionButton()
    }

  private fun findActionButton(): MaterialButton? {
    val actionButton =
      templateLayout.findManagedViewById<MaterialButton?>(R.id.sud_floating_action_button)
    if (actionButton == null) {
      Log.w(TAG, "Can't find the action button.")
    }
    return actionButton
  }

  @VisibleForTesting
  fun inflateButton(viewStub: ViewStub) {
    val inflater = LayoutInflater.from(templateLayout.context)
    viewStub.layoutInflater = inflater
    viewStub.inflate()

    val actionButton = findActionButton() ?: return
    ItemStyler.applyFocusRingDrawable(
      actionButton.context,
      actionButton,
      ItemStyler.FocusIndicatorShape.CIRCLE,
      null,
    )
  }

  private val containerView: FrameLayout?
    get() = templateLayout.findManagedViewById(R.id.sud_layout_floating_action_button_container)

  var visibility: Int
    /** Returns the current visibility of the action button. */
    get() = actionButton?.visibility ?: View.GONE

    /**
     * Sets the visibility of the action button. gone map to 8 invisible map to 4 visible map to 0
     *
     * @param visibility Set it visible or not
     */
    set(visibility) {
      actionButton?.visibility = visibility
      this.containerView?.visibility = visibility
    }

  var onClickListener: OnClickListener?
    /** Gets the [OnClickListener] of the action button. */
    get() = this.listener
    /** Sets the [OnClickListener] of the action button. */
    set(listener) {
      this.listener = listener
      actionButton?.setOnClickListener { v: View? ->
        listener?.onClick(v)
      }
    }

  companion object {
    private const val TAG = "FloatingActionButtonMixin"
  }
}