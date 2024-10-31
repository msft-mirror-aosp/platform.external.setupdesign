/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.google.android.setupdesign.template;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.template.Mixin;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.LayoutStyler;
import com.google.android.setupdesign.util.PartnerStyleHelper;

/** A {@link Mixin} for controlling back button on the template layout. */
public class FloatingBackButtonMixin implements Mixin {

  private final TemplateLayout templateLayout;
  private static final String TAG = "FloatingBackButtonMixin";

  @Nullable private OnClickListener listener;

  /**
   * A {@link Mixin} for setting and getting the back button.
   *
   * @param layout The template layout that this Mixin is a part of
   * @param attrs XML attributes given to the layout
   * @param defStyleAttr The default style attribute as given to the constructor of the layout
   */
  public FloatingBackButtonMixin(TemplateLayout layout, AttributeSet attrs, int defStyleAttr) {
    templateLayout = layout;
  }

  /**
   * Sets the visibility of the back button. gone map to 8 invisible map to 4 visible map to 0
   *
   * @param visibility Set it visible or not
   */
  public void setVisibility(int visibility) {
    final Button backbutton = getBackButton();
    if (backbutton != null) {
      getBackButton().setVisibility(visibility);
      getContainerView().setVisibility(visibility);
    }
  }

  /** Sets the {@link OnClickListener} of the back button. */
  public void setOnClickListener(@Nullable OnClickListener listener) {
    final Button backbutton = getBackButton();
    if (backbutton != null) {
      this.listener = listener;
      getBackButton().setOnClickListener(listener);
    }
  }

  /** Tries to apply the partner customization to the back button. */
  public void tryApplyPartnerCustomizationStyle() {
    if (PartnerStyleHelper.shouldApplyPartnerResource(templateLayout)
        && getContainerView() != null) {
      LayoutStyler.applyPartnerCustomizationExtraPaddingStyle(getContainerView());
    }
  }

  /** Returns the current back button. */
  @VisibleForTesting
  Button getBackButton() {
    Button backbutton = templateLayout.findManagedViewById(R.id.sud_floating_back_button);
    if (backbutton == null) {
      Log.w(TAG, "Can't find the back button.");
    }
    return backbutton;
  }

  protected FrameLayout getContainerView() {
    return templateLayout.findManagedViewById(R.id.sud_layout_floating_back_button_container);
  }

  /** Returns the current visibility of the back button. */
  public int getVisibility() {
    final Button backbutton = getBackButton();
    return (backbutton != null) ? getBackButton().getVisibility() : View.GONE;
  }

  /** Gets the {@link OnClickListener} of the back button. */
  public OnClickListener getOnClickListener() {
    return this.listener;
  }
}
