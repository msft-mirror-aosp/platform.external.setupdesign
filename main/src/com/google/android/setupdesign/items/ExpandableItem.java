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

package com.google.android.setupdesign.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.LayoutStyler;

/**
 * A expandable item which is has a button in the end to expand or collapse the item. The item can
 * show the layout which is set by the layout resource.
 */
public class ExpandableItem extends Item implements OnClickListener {

  private boolean isExpanded = false;
  private boolean canExpanded = true;
  private int expandedLayoutRes = 0;
  private View expandedContent = null;

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

  public ExpandableItem() {
    super();
  }

  public ExpandableItem(Context context) {
    super();
  }

  public ExpandableItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudExpandableItem);
    expandedLayoutRes = a.getResourceId(R.styleable.SudExpandableItem_sudExpandedContent, 0);
    a.recycle();
  }

  public ExpandableItem(Context context, AttributeSet attrs, boolean canExpanded) {
    super(context, attrs);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudExpandableItem);
    expandedLayoutRes = a.getResourceId(R.styleable.SudExpandableItem_sudExpandedContent, 0);
    a.recycle();
    this.canExpanded = canExpanded;
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.sud_items_expandable;
  }

  /** Returns true if the item is currently expanded. */
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

  /** Sets whether the item can be expanded. */
  public void setCanExpanded(boolean canExpanded) {
    this.canExpanded = canExpanded;
    notifyItemChanged();
  }

  /** Sets the view for the expanded content. */
  public void setExpandedView(View expandedContent) {
    this.expandedContent = expandedContent;
  }

  /** Sets the layout resource for the expanded content. */
  public void setExpandedLayoutRes(int expandedLayoutRes) {
    this.expandedLayoutRes = expandedLayoutRes;
  }

  @Override
  public void onBindView(View view) {
    super.onBindView(view);

    // Expandable item is using this view's child to listen clickable event, to avoid
    // accessibility issue, remove clickable event in this view.
    view.setClickable(false);

    View expandButton = view.findViewById(R.id.sud_items_expand_button);
    if (expandButton != null) {
      if (canExpanded) {
        expandButton.setOnClickListener(this);
      } else {
        expandButton.setVisibility(View.GONE);
      }
    }
    View expandableContentContainer =
        view.findViewById(R.id.sud_items_expandable_content_container);
    if (expandableContentContainer != null) {
      if (expandedContent != null) {
        ((ViewGroup) expandedContent.getParent()).removeView(expandedContent);
        ((ViewGroup) expandableContentContainer).addView(expandedContent);
      } else {
        if (expandedLayoutRes != 0) {
          LayoutInflater inflater = LayoutInflater.from(expandableContentContainer.getContext());
          View expandableContent =
              inflater.inflate(expandedLayoutRes, (ViewGroup) expandableContentContainer, false);
          ((ViewGroup) expandableContentContainer).addView(expandableContent);
        }
        if (isExpanded) {
          expandableContentContainer.setVisibility(View.VISIBLE);
        } else {
          expandableContentContainer.setVisibility(View.GONE);
        }
      }
    }
    ViewCompat.setAccessibilityDelegate(view, accessibilityDelegate);
    if (!PartnerConfigHelper.isGlifExpressiveEnabled(view.getContext())) {
      LayoutStyler.applyPartnerCustomizationLayoutPaddingStyle(view);
    }

    // Expandable item has focusability on the expandable layout on the left, and the
    // expand button on the right, but not the item itself.
    view.setFocusable(false);
    updateExpandButtonImage(view);
  }

  private void updateExpandButtonImage(View view) {
    ImageView expandButton = view.findViewById(R.id.sud_items_expand_button);
    if (expandButton != null) {
      if (isExpanded()) {
        expandButton.setImageResource(R.drawable.sud_items_collapse_button_icon);
      } else {
        expandButton.setImageResource(R.drawable.sud_items_expand_button_icon);
      }
    }
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.sud_items_expand_button && canExpanded) {
      setExpanded(!isExpanded());
      updateExpandButtonImage(v);
    }
  }
}
