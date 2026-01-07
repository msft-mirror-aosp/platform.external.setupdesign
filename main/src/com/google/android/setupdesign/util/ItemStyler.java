/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.setupdesign.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.util.Logger;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.TextViewPartnerStyler.TextPartnerConfigs;

/**
 * Applies the partner style of layout to the given View {@code view}. The user needs to check if
 * the {@code view} should apply partner heavy theme before calling this method.
 */
public final class ItemStyler {

  private static final Logger LOG = new Logger(ItemStyler.class);

  /**
   * Applies the heavy theme partner configs to the given listItemView {@code listItemView}. The
   * user needs to check before calling this method:
   *
   * <p>1) If the {@code listItemView} should apply heavy theme resource by calling {@link
   * PartnerStyleHelper#shouldApplyPartnerHeavyThemeResource}.
   *
   * <p>2) If the layout of the {@code listItemView} contains fixed resource IDs which attempts to
   * apply heavy theme resources (The resource ID of the title is "sud_items_title" and the resource
   * ID of the summary is "sud_items_summary"), refer to {@link R.layout#sud_items_default}.
   *
   * @param listItemView A view would be applied heavy theme styles
   */
  @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
  public static void applyPartnerCustomizationItemStyle(@Nullable View listItemView) {
    if (listItemView == null) {
      return;
    }
    if (!PartnerStyleHelper.shouldApplyPartnerHeavyThemeResource(listItemView)) {
      return;
    }

    final TextView titleTextView = listItemView.findViewById(R.id.sud_items_title);
    // apply title text style
    applyPartnerCustomizationItemTitleStyle(titleTextView);

    // adjust list item view gravity
    TextView summaryTextView = listItemView.findViewById(R.id.sud_items_summary);
    if (summaryTextView.getVisibility() == View.GONE && listItemView instanceof LinearLayout) {
      // Set list items to vertical center when there is no summary.
      ((LinearLayout) listItemView).setGravity(Gravity.CENTER_VERTICAL);
    }

    // apply summary text style
    applyPartnerCustomizationItemSummaryStyle(summaryTextView);

    // apply list item view style
    applyPartnerCustomizationItemViewLayoutStyle(listItemView);

    // apply list item view background color
    applyPartnerCustomizationItemViewBackgroundColor(listItemView);
  }

  /**
   * Applies the partner heavy style to the given list item title text view. Will check the current
   * text view enabled the partner customized heavy theme configurations before applying.
   *
   * @param titleTextView A textView of a list item title text.
   */
  public static void applyPartnerCustomizationItemTitleStyle(TextView titleTextView) {
    if (!PartnerStyleHelper.shouldApplyPartnerHeavyThemeResource(titleTextView)) {
      return;
    }
    Context context = titleTextView.getContext();

    TextViewPartnerStyler.applyPartnerCustomizationStyle(
        titleTextView,
        new TextPartnerConfigs(
            /* textColorConfig= */ null,
            /* textLinkedColorConfig= */ null,
            PartnerConfig.CONFIG_ITEMS_TITLE_TEXT_SIZE,
            PartnerConfig.CONFIG_ITEMS_TITLE_FONT_FAMILY,
            /* textFontWeightConfig= */ null,
            /* textLinkFontFamilyConfig= */ null,
            /* textMarginTopConfig= */ null,
            /* textMarginBottomConfig= */ null,
            PartnerStyleHelper.getLayoutGravity(titleTextView.getContext())));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      if (PartnerConfigHelper.isGlifExpressiveEnabled(context)
          && PartnerConfigHelper.get(context)
              .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_TITLE_FONT_VARIATION_SETTINGS)) {
        // TODO: add unit test for this case.
        String fontVariationSettings =
            PartnerConfigHelper.get(context)
                .getString(context, PartnerConfig.CONFIG_ITEMS_TITLE_FONT_VARIATION_SETTINGS);
        if (fontVariationSettings != null && !fontVariationSettings.isEmpty()) {
          try {
            titleTextView.setFontVariationSettings(fontVariationSettings);
          } catch (IllegalArgumentException e) {
            LOG.e("Failed to set font variation settings: " + fontVariationSettings, e);
          }
        }
      }
    }
  }

  /**
   * Applies the partner heavy style to the given summary text view. Will check the current text
   * view enabled the partner customized heavy theme configurations before applying.
   *
   * @param summaryTextView A textView of a list item summary text.
   */
  public static void applyPartnerCustomizationItemSummaryStyle(TextView summaryTextView) {
    if (!PartnerStyleHelper.shouldApplyPartnerHeavyThemeResource(summaryTextView)) {
      return;
    }

    TextViewPartnerStyler.applyPartnerCustomizationStyle(
        summaryTextView,
        new TextPartnerConfigs(
            /* textColorConfig= */ null,
            /* textLinkedColorConfig= */ null,
            PartnerConfig.CONFIG_ITEMS_SUMMARY_TEXT_SIZE,
            PartnerConfig.CONFIG_ITEMS_SUMMARY_FONT_FAMILY,
            /* textFontWeightConfig= */ null,
            /* textLinkFontFamilyConfig= */ null,
            PartnerConfig.CONFIG_ITEMS_SUMMARY_MARGIN_TOP,
            /* textMarginBottomConfig= */ null,
            PartnerStyleHelper.getLayoutGravity(summaryTextView.getContext())));
  }

  /**
   * Applies the partner layout margin style to the given list item view {@code listItemView}. The
   * theme should set partner heavy theme config first, and then the partner layout style would be
   * applied.
   *
   * @param listItemView A view would be applied partner layout margin style
   */
  @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
  public static void applyPartnerCustomizationLayoutMarginStyle(@Nullable View listItemView) {
    if (listItemView == null) {
      return;
    }

    Context context = listItemView.getContext();
    boolean partnerMarginStartAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_START);
    boolean partnerMarginEndAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_END);

    // TODO: After all users added the check before calling the API, this check can be
    // deleted.
    if (PartnerStyleHelper.shouldApplyPartnerResource(listItemView)
        && (partnerMarginStartAvailable || partnerMarginEndAvailable)) {
      int marginStart;
      int marginEnd;
      if (listItemView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
        ViewGroup.MarginLayoutParams layoutParams =
            (ViewGroup.MarginLayoutParams) listItemView.getLayoutParams();
        if (partnerMarginStartAvailable) {
          marginStart =
              (int)
                  PartnerConfigHelper.get(context)
                      .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_START);
        } else {
          marginStart = layoutParams.leftMargin;
        }
        if (partnerMarginEndAvailable) {
          marginEnd =
              (int)
                  PartnerConfigHelper.get(context)
                      .getDimension(context, PartnerConfig.CONFIG_LAYOUT_MARGIN_END);
        } else {
          marginEnd = layoutParams.rightMargin;
        }
        layoutParams.setMargins(
            marginStart, layoutParams.topMargin, marginEnd, layoutParams.bottomMargin);
        layoutParams.setMarginStart(marginStart);
        layoutParams.setMarginEnd(marginEnd);
      }
    }
  }

  public static void applyPartnerCustomizationItemViewLayoutStyle(@Nullable View listItemView) {
    Context context = listItemView.getContext();
    float paddingTop;
    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_PADDING_TOP)) {
      paddingTop =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_ITEMS_PADDING_TOP);
    } else {
      paddingTop = listItemView.getPaddingTop();
    }

    float paddingBottom;
    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_PADDING_BOTTOM)) {
      paddingBottom =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_ITEMS_PADDING_BOTTOM);
    } else {
      paddingBottom = listItemView.getPaddingBottom();
    }

    if (paddingTop != listItemView.getPaddingTop()
        || paddingBottom != listItemView.getPaddingBottom()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        listItemView.setPadding(
            listItemView.getPaddingStart(),
            (int) paddingTop,
            listItemView.getPaddingEnd(),
            (int) paddingBottom);
      } else {
        listItemView.setPadding(
            listItemView.getPaddingLeft(),
            (int) paddingTop,
            listItemView.getPaddingRight(),
            (int) paddingBottom);
      }
    }

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_MIN_HEIGHT)) {
      float minHeight =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_ITEMS_MIN_HEIGHT);
      listItemView.setMinimumHeight((int) minHeight);
    }
  }

  /**
   * Applies the partner background color to the given list item view {@code listItemView} when the
   * glif expressive is enabled and dynamic color is not enabled.
   *
   * @param listItemView A view would be applied partner background color style.
   */
  public static void applyPartnerCustomizationItemViewBackgroundColor(@Nullable View listItemView) {
    if (listItemView == null) {
      return;
    }

    Context context = listItemView.getContext();

    if (!PartnerConfigHelper.isGlifExpressiveEnabled(context)) {
      return;
    }

    if (ThemeHelper.shouldApplyDynamicColor(context)) {
      return;
    }

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_BACKGROUND_COLOR)) {
      int backgroundColor =
          PartnerConfigHelper.get(context)
              .getColor(context, PartnerConfig.CONFIG_ITEMS_BACKGROUND_COLOR);
      listItemView.setBackgroundColor(backgroundColor);
    }
  }

  private ItemStyler() {}
}
