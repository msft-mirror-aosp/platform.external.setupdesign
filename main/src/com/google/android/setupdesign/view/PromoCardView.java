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

package com.google.android.setupdesign.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;

/**
 * The promo card item is extension of a linear layout with an icon with title and summary and with
 * some modifications to make it more suitable for promo items. For example, the promo card item
 * layout has a larger icon size and a different background color.
 */
public class PromoCardView extends LinearLayout {

  @Nullable private Drawable icon;
  @Nullable private CharSequence title;
  @Nullable private CharSequence summary;

  private RichTextView titleView;
  private RichTextView summaryView;
  private ImageView iconView;
  private boolean topRoundedCorner;
  private boolean bottomRoundedCorner;

  public PromoCardView(Context context) {
    super(context);
    init();
  }

  public PromoCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudPromoCardView);
    icon = a.getDrawable(R.styleable.SudPromoCardView_android_icon);
    title = a.getText(R.styleable.SudPromoCardView_android_title);
    summary = a.getText(R.styleable.SudPromoCardView_android_summary);
    topRoundedCorner = a.getBoolean(R.styleable.SudPromoCardView_sudTopRoundedCorner, false);
    bottomRoundedCorner = a.getBoolean(R.styleable.SudPromoCardView_sudBottomRoundedCorner, false);
    a.recycle();
    init();
  }

  public PromoCardView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudPromoCardView);
    icon = a.getDrawable(R.styleable.SudPromoCardView_android_icon);
    title = a.getText(R.styleable.SudPromoCardView_android_title);
    summary = a.getText(R.styleable.SudPromoCardView_android_summary);
    topRoundedCorner = a.getBoolean(R.styleable.SudPromoCardView_sudTopRoundedCorner, false);
    bottomRoundedCorner = a.getBoolean(R.styleable.SudPromoCardView_sudBottomRoundedCorner, false);
    a.recycle();
    init();
  }

  private void init() {
    View.inflate(getContext(), R.layout.sud_promo_card_default, this);
    titleView = findViewById(R.id.sud_items_title);
    summaryView = findViewById(R.id.sud_items_summary);
    iconView = findViewById(R.id.sud_items_icon);
    if (titleView != null && title != null) {
      titleView.setText(title);
      titleView.setVisibility(View.VISIBLE);
    }
    if (summaryView != null && summary != null) {
      summaryView.setText(summary);
      summaryView.setVisibility(View.VISIBLE);
    }
    if (iconView != null && icon != null) {
      iconView.setImageDrawable(icon);
      iconView.setVisibility(View.VISIBLE);
    }
    updateBackground();
  }

  private Drawable getFirstBackground(Context context) {
    TypedArray a =
        context.getTheme().obtainStyledAttributes(new int[] {R.attr.sudItemBackgroundFirst});
    Drawable firstBackground = a.getDrawable(0);
    a.recycle();
    return firstBackground;
  }

  private Drawable getLastBackground(Context context) {
    TypedArray a =
        context.getTheme().obtainStyledAttributes(new int[] {R.attr.sudItemBackgroundLast});
    Drawable lastBackground = a.getDrawable(0);
    a.recycle();
    return lastBackground;
  }

  private Drawable getMiddleBackground(Context context) {
    TypedArray a = context.getTheme().obtainStyledAttributes(new int[] {R.attr.sudItemBackground});
    Drawable middleBackground = a.getDrawable(0);
    a.recycle();
    return middleBackground;
  }

  private Drawable getSingleBackground(Context context) {
    TypedArray a =
        context.getTheme().obtainStyledAttributes(new int[] {R.attr.sudItemBackgroundSingle});
    Drawable singleBackground = a.getDrawable(0);
    a.recycle();
    return singleBackground;
  }

  private void updateBackground() {
    float cornerRadius =
        getResources().getDimension(R.dimen.sud_glif_expressive_item_corner_radius);
    float groupCornerRadius =
        PartnerConfigHelper.get(getContext())
            .getDimension(
                getContext(), PartnerConfig.CONFIG_ITEMS_GROUP_CORNER_RADIUS, cornerRadius);
    Drawable background = getMiddleBackground(getContext());

    if (topRoundedCorner && bottomRoundedCorner) {
      background = getSingleBackground(getContext());
    } else if (topRoundedCorner) {
      background = getFirstBackground(getContext());
    } else if (bottomRoundedCorner) {
      background = getLastBackground(getContext());
    }

    if (background instanceof GradientDrawable) {
      float topCornerRadius = cornerRadius;
      float bottomCornerRadius = cornerRadius;
      if (topRoundedCorner) {
        topCornerRadius = groupCornerRadius;
      }
      if (bottomRoundedCorner) {
        bottomCornerRadius = groupCornerRadius;
      }
      ((GradientDrawable) background)
          .setCornerRadii(
              new float[] {
                topCornerRadius,
                topCornerRadius,
                topCornerRadius,
                topCornerRadius,
                bottomCornerRadius,
                bottomCornerRadius,
                bottomCornerRadius,
                bottomCornerRadius
              });
      findViewById(R.id.sud_promo_card_container).setBackgroundDrawable(background);
    }
  }

  public void setTopRoundedCorner(boolean topRoundedCorner) {
    this.topRoundedCorner = topRoundedCorner;
    updateBackground();
  }

  public void setBottomRoundedCorner(boolean bottomRoundedCorner) {
    this.bottomRoundedCorner = bottomRoundedCorner;
    updateBackground();
  }

  public void setTitle(CharSequence title) {
    this.title = title;
    if (titleView != null) {
      titleView.setText(title);
      titleView.setVisibility(View.VISIBLE);
    }
  }

  public void setSummary(CharSequence summary) {
    this.summary = summary;
    if (summaryView != null) {
      summaryView.setText(summary);
      summaryView.setVisibility(View.VISIBLE);
    }
  }

  public void setIcon(Drawable icon) {
    this.icon = icon;
    if (iconView != null) {
      iconView.setImageDrawable(icon);
      iconView.setVisibility(View.VISIBLE);
    }
  }

  public Drawable getIcon() {
    return icon;
  }

  public CharSequence getTitle() {
    return title;
  }

  public CharSequence getSummary() {
    return summary;
  }

  public boolean isTopRoundedCorner() {
    return topRoundedCorner;
  }

  public boolean isBottomRoundedCorner() {
    return bottomRoundedCorner;
  }
}
