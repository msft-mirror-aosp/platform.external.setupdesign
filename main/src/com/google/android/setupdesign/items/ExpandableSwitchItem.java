/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.LayoutStyler;
import com.google.android.setupdesign.view.CheckableLinearLayout;

/**
 * A switch item which is divided into two parts: the start (left for LTR) side shows the title and
 * summary, and when that is clicked, will expand to show a longer summary. The end (right for LTR)
 * side is a switch which can be toggled by the user.
 *
 * <p>Note: It is highly recommended to use this item with recycler view rather than list view,
 * because list view draws the touch ripple effect on top of the item, rather than letting the item
 * handle it. Therefore you might see a double-ripple, one for the expandable area and one for the
 * entire list item, when using this in list view.
 */
public class ExpandableSwitchItem extends SwitchItem
    implements OnCheckedChangeListener, OnClickListener {

  private CharSequence collapsedSummary;
  private CharSequence expandedSummary;
  private boolean isExpanded = false;
  private boolean canExpanded = true;
  private boolean isSwitchItem = true;

  private final AccessibilityDelegateCompat accessibilityDelegate =
      new AccessibilityDelegateCompat() {
        @Override
        public void onInitializeAccessibilityNodeInfo(
            View view, AccessibilityNodeInfoCompat nodeInfo) {
          super.onInitializeAccessibilityNodeInfo(view, nodeInfo);
          nodeInfo.addAction(
              isExpanded()
                  ? AccessibilityActionCompat.ACTION_COLLAPSE
                  : AccessibilityActionCompat.ACTION_EXPAND);
        }

        @Override
        public boolean performAccessibilityAction(View view, int action, Bundle args) {
          boolean result;
          switch (action) {
            case AccessibilityNodeInfoCompat.ACTION_COLLAPSE:
            case AccessibilityNodeInfoCompat.ACTION_EXPAND:
              setExpanded(!isExpanded());
              result = true;
              break;
            default:
              result = super.performAccessibilityAction(view, action, args);
              break;
          }
          return result;
        }
      };

  public ExpandableSwitchItem() {
    super();
    setIconGravity(Gravity.TOP);
  }

  public ExpandableSwitchItem(Context context) {
    super();
    if (!PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      setIconGravity(Gravity.TOP);
    } else {
      setLayoutResource(R.layout.sud_items_expandable_switch_expressive);
      setIconGravity(Gravity.CENTER_VERTICAL);
    }
  }

  public ExpandableSwitchItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudExpandableSwitchItem);
    collapsedSummary = a.getText(R.styleable.SudExpandableSwitchItem_sudCollapsedSummary);
    expandedSummary = a.getText(R.styleable.SudExpandableSwitchItem_sudExpandedSummary);
    if (!PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      setIconGravity(a.getInt(R.styleable.SudItem_sudIconGravity, Gravity.TOP));
    } else {
      setLayoutResource(R.layout.sud_items_expandable_switch_expressive);
      setIconGravity(a.getInt(R.styleable.SudItem_sudIconGravity, Gravity.CENTER_VERTICAL));
    }
    a.recycle();
  }

  public ExpandableSwitchItem(
      Context context, AttributeSet attrs, boolean isSwitchItem, boolean canExpanded) {
    super(context, attrs);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudExpandableSwitchItem);
    collapsedSummary = a.getText(R.styleable.SudExpandableSwitchItem_sudCollapsedSummary);
    expandedSummary = a.getText(R.styleable.SudExpandableSwitchItem_sudExpandedSummary);
    if (!PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      setIconGravity(a.getInt(R.styleable.SudItem_sudIconGravity, Gravity.TOP));
    } else {
      setLayoutResource(R.layout.sud_items_expandable_switch_expressive);
      setIconGravity(a.getInt(R.styleable.SudItem_sudIconGravity, Gravity.CENTER_VERTICAL));
    }
    a.recycle();
    this.isSwitchItem = isSwitchItem;
    this.canExpanded = canExpanded;
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.sud_items_expandable_switch;
  }

  @Override
  public CharSequence getSummary() {
    return isExpanded ? getExpandedSummary() : getCollapsedSummary();
  }

  /** @return True if the item is currently expanded. */
  public boolean isExpanded() {
    return isExpanded;
  }

  /** Sets whether the item should be expanded. */
  public void setExpanded(boolean expanded) {
    if (isExpanded == expanded) {
      return;
    }
    isExpanded = expanded;
    notifyItemChanged();
  }

  /** @return The summary shown when in collapsed state. */
  public CharSequence getCollapsedSummary() {
    return collapsedSummary;
  }

  /**
   * Sets the summary text shown when the item is collapsed. Corresponds to the {@code
   * app:sudCollapsedSummary} XML attribute.
   */
  public void setCollapsedSummary(CharSequence collapsedSummary) {
    this.collapsedSummary = collapsedSummary;
    if (!isExpanded()) {
      notifyChanged();
    }
  }

  /** @return The summary shown when in expanded state. */
  public CharSequence getExpandedSummary() {
    return expandedSummary;
  }

  /**
   * Sets the summary text shown when the item is expanded. Corresponds to the {@code
   * app:sudExpandedSummary} XML attribute.
   */
  public void setExpandedSummary(CharSequence expandedSummary) {
    this.expandedSummary = expandedSummary;
    if (isExpanded()) {
      notifyChanged();
    }
  }

  @Override
  public void onBindView(View view) {
    // TODO: If it is possible to detect, log a warning if this is being used with ListView.
    super.onBindView(view);

    // Expandable switch item is using this view's child to listen clickable event, to avoid
    // accessibility issue, remove clickable event in this view.
    view.setClickable(false);

    if (PartnerConfigHelper.isGlifExpressiveEnabled(view.getContext())) {
      View moreInfo = view.findViewById(R.id.sud_items_more_info);
      View contentContainer = view.findViewById(R.id.sud_items_summary_container);
      if (canExpanded) {
        if (contentContainer != null) {
          contentContainer.setOnClickListener(this);
        }
      }
      if (moreInfo != null) {
        if (!canExpanded) {
          moreInfo.setVisibility(View.GONE);
        }
      }
      View switchItem = view.findViewById(R.id.sud_items_switch);
      if (!isSwitchItem && switchItem != null) {
        switchItem.setVisibility(View.INVISIBLE);
      }
    } else {
      View content = view.findViewById(R.id.sud_items_expandable_switch_content);
      content.setOnClickListener(this);

      if (content instanceof CheckableLinearLayout checkableLinearLayout) {
        checkableLinearLayout.setChecked(isExpanded());

        // On lower versions
        checkableLinearLayout.setAccessibilityLiveRegion(
            isExpanded()
                ? ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE
                : ViewCompat.ACCESSIBILITY_LIVE_REGION_NONE);

        ViewCompat.setAccessibilityDelegate(checkableLinearLayout, accessibilityDelegate);
      }
      LayoutStyler.applyPartnerCustomizationLayoutPaddingStyle(content);
    }
    tintCompoundDrawables(view);

    // Expandable switch item has focusability on the expandable layout on the left, and the
    // switch on the right, but not the item itself.
    view.setFocusable(false);
    updateShowMoreLinkText(view);
  }

  private void updateShowMoreLinkText(View view) {
    TextView showMoreLink = view.findViewById(R.id.sud_items_more_info);
    if (showMoreLink != null) {
      if (isExpanded()) {
        showMoreLink.setText(com.google.android.setupdesign.strings.R.string.sud_less_info);
      } else {
        showMoreLink.setText(com.google.android.setupdesign.strings.R.string.sud_more_info);
      }
    }
  }

  @Override
  public void onClick(View v) {
    if (PartnerConfigHelper.isGlifExpressiveEnabled(v.getContext())) {
      if (v.getId() == R.id.sud_items_summary_container) {
        setExpanded(!isExpanded());
        updateShowMoreLinkText(v);
      }
    } else {
      setExpanded(!isExpanded());
    }
  }

  // Tint the expand arrow with the text color
  private void tintCompoundDrawables(View view) {
    final TypedArray a =
        view.getContext().obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
    final ColorStateList tintColor = a.getColorStateList(0);
    a.recycle();

    if (tintColor != null) {
      TextView titleView = (TextView) view.findViewById(R.id.sud_items_title);
      for (Drawable drawable : titleView.getCompoundDrawables()) {
        if (drawable != null) {
          drawable.setColorFilter(tintColor.getDefaultColor(), Mode.SRC_IN);
        }
      }
      if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
        for (Drawable drawable : titleView.getCompoundDrawablesRelative()) {
          if (drawable != null) {
            drawable.setColorFilter(tintColor.getDefaultColor(), Mode.SRC_IN);
          }
        }
      }
    }
  }
}
