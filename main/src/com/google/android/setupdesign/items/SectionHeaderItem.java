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

package com.google.android.setupdesign.items;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.LayoutStyler;

/** A section header item that represents a default style or bluechip styles. */
public class SectionHeaderItem extends Item implements Dividable {

  public SectionHeaderItem() {}

  // dereference of possibly-null reference params
  @SuppressWarnings("nullness:dereference.of.nullable")
  @Override
  public void onBindView(View view) {
    TextView label = (TextView) view.findViewById(R.id.sud_items_title);
    if (getTitleColor() != Color.TRANSPARENT) {
      label.setTextColor(getTitleColor());
    }
    label.setText(getTitle());
    TextView summaryView = (TextView) view.findViewById(R.id.sud_items_summary);
    CharSequence summary = getSummary();
    if (hasSummary(summary)) {
      summaryView.setText(summary);
      summaryView.setVisibility(View.VISIBLE);
    } else {
      summaryView.setVisibility(View.GONE);
    }
    view.setId(getViewId());
    view.findViewById(R.id.sud_items_icon_container).setVisibility(View.GONE);
    view.setContentDescription(getContentDescription());
    view.setClickable(/* clickable= */ false);

    if (getTitle().isEmpty()) {
      view.setFocusable(false);
      view.setFocusableInTouchMode(false);
      view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    if (!PartnerConfigHelper.isGlifExpressiveEnabled(view.getContext())) {
      LayoutStyler.applyPartnerCustomizationLayoutPaddingStyle(view);
    }
  }

  private boolean hasSummary(CharSequence summary) {
    return summary != null && summary.length() > 0;
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.sud_items_section_header;
  }

  @Override
  public boolean isDividerAllowedAbove() {
    // Keep hiding the divide behavior when we set enable as true to prevent talkback speaks
    // "disabled" for header item.
    return false;
  }

  @Override
  public boolean isDividerAllowedBelow() {
    // Keep hiding the divide behavior when we set enable as true to prevent talkback speaks
    // "disabled" for header item.
    return false;
  }

  @Override
  public boolean isGroupDivider() {
    return true;
  }
}
