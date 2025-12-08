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

package com.google.android.setupdesign.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import com.google.android.setupcompat.R;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.util.Logger;
import java.util.Locale;

/**
 * A custom LinearLayout that modifies system window insets, specifically the bottom inset, based on
 * partner configuration.
 *
 * <p>This layout for large screen is designed to handle edge-to-edge display scenarios,
 * particularly when {@link PartnerConfigHelper#isGlifExpressiveEnabled(Context)} is true. It
 * removes the bottom system window inset, effectively extending the layout to the bottom edge of
 * the screen.
 *
 * <p>This layout should be used as a root layout or within a view hierarchy where edge-to-edge
 * behavior is desired. It ensures that content extends to the bottom of the screen when the Glif
 * Expressive feature is enabled.
 */
public class InsetAdjustmentLayout extends LinearLayout {

  private static final Logger LOG = new Logger("InsetAdjustmentLayout");

  public InsetAdjustmentLayout(Context context) {
    super(context);
  }

  public InsetAdjustmentLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public InsetAdjustmentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  @SuppressLint("NewApi")
  public WindowInsets onApplyWindowInsets(WindowInsets insets) {
    return super.onApplyWindowInsets(
        shouldApplyEdgeToEdge(insets.getSystemWindowInsetBottom())
            ? applyEdgeToEdge(insets)
            : insets);
  }

  private boolean shouldApplyEdgeToEdge(int windowInsetBottom) {
    boolean glifExpressiveEnabled = PartnerConfigHelper.isGlifExpressiveEnabled(getContext());
    boolean isAtLeastLollipop = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    LOG.atInfo(
        String.format(
            Locale.US,
            "Apply edge to edge, glifExpressiveEnabled: %s, isAtLeastLollipop: %s,"
                + " windowInsetBottom: %d",
            glifExpressiveEnabled,
            isAtLeastLollipop,
            windowInsetBottom));
    return glifExpressiveEnabled && isAtLeastLollipop && windowInsetBottom >= 0;
  }

  @SuppressLint("NewApi")
  private WindowInsets applyEdgeToEdge(WindowInsets insets) {
    View view = findViewById(R.id.suc_layout_status);
    int paddingBottom = view != null ? view.getPaddingBottom() : 0;
    LOG.atDebug("applyEdgeToEdge, paddingBottom: " + paddingBottom);
    return insets.replaceSystemWindowInsets(0, insets.getSystemWindowInsetTop(), 0, paddingBottom);
  }
}
