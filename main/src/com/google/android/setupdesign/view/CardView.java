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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;

/** A card view that can be used to display a title and an icon. */
public class CardView extends LinearLayout implements View.OnClickListener {

  private Drawable icon;
  private CharSequence title;
  private float titleSize;
  private String fontFamily;

  private ImageView iconView;
  protected WrapTextView titleView;
  private OnClickListener onClickListener;
  protected boolean skipClickSelection;

  public CardView(Context context) {
    this(context, /* attrs= */ null, /* defStyleAttr= */ 0);
  }

  public CardView(Context context, AttributeSet attrs) {
    this(context, attrs, /* defStyleAttr= */ 0);
  }

  public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudCardView);
    icon = a.getDrawable(R.styleable.SudCardView_sudIcon);
    title = a.getText(R.styleable.SudCardView_sudTitleText);
    titleSize = a.getDimensionPixelSize(R.styleable.SudCardView_sudTitleSize, /* defValue= */ 0);
    fontFamily = a.getString(R.styleable.SudCardView_sudFontFamily);
    skipClickSelection =
        a.getBoolean(R.styleable.SudCardView_sudCardViewSkipClickSelection, /* defValue= */ false);
    a.recycle();
    init();
  }

  private void init() {
    View.inflate(getContext(), R.layout.sud_card_view_default, this);
    // set on click listener to this view to handle the internal click event.
    super.setOnClickListener(this);
    iconView = findViewById(R.id.sud_items_icon);
    titleView = findViewById(R.id.sud_items_title);
    if (iconView != null && icon != null) {
      iconView.setImageDrawable(icon);
    }
    if (titleView != null) {
      if (title != null) {
        titleView.setText(title);
      }
      if (titleSize > 0) {
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
      }
      if (fontFamily != null) {
        titleView.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
      }
    }

    if (PartnerConfigHelper.get(getContext())
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_CARD_VIEW_SELECTED_RADIUS)) {
      int selectedRadius =
          (int)
              PartnerConfigHelper.get(getContext())
                  .getDimension(getContext(), PartnerConfig.CONFIG_CARD_VIEW_SELECTED_RADIUS);
      updateCardSelectedRadius(selectedRadius);
    }
  }

  /** Sets the title of the card. */
  public void setCardTitle(CharSequence title) {
    this.title = title;
    if (titleView != null) {
      titleView.setText(title);
    }
  }

  /** Sets the icon of the card. */
  public void setCardIcon(Drawable icon) {
    this.icon = icon;
    if (iconView != null) {
      iconView.setImageDrawable(icon);
    }
  }

  /** Returns the title of the card. */
  public CharSequence getCardTitle() {
    return title;
  }

  /** Returns the icon of the card. */
  public Drawable getCardIcon() {
    return icon;
  }

  /** Returns the title size of the title. */
  public float getCardTitleSize() {
    return titleSize;
  }

  /** Sets the title size of the title. */
  public void setCardTitleSize(float titleSize) {
    this.titleSize = titleSize;
    if (titleView != null) {
      titleView.setTextSize(titleSize);
    }
  }

  /** Returns the font family of the title. */
  public String getCardFontFamily() {
    return fontFamily;
  }

  /** Sets the font family of the title. */
  public void setCardFontFamily(String fontFamily) {
    this.fontFamily = fontFamily;
    if (titleView != null) {
      titleView.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
    }
  }

  @Override
  public void onClick(View v) {
    if (!skipClickSelection) {
      v.setSelected(true);
      if (iconView != null) {
        iconView.setImageDrawable(
            ContextCompat.getDrawable(this.getContext(), R.drawable.sud_ic_check_mark));
        iconView.setSelected(true);
        v.setContentDescription(
            getContext()
                .getString(
                    com.google.android.setupdesign.strings.R.string
                        .sud_card_view_check_mark_icon_label));
      }
      if (titleView != null) {
        titleView.setSelected(true);
      }
    }
    if (onClickListener != null) {
      // handle the external click event.
      onClickListener.onClick(v);
    }
  }

  @Override
  public final void setOnClickListener(@Nullable OnClickListener listener) {
    onClickListener = listener;
  }

  private void updateCardSelectedRadius(float radius) {
    // generate a new selected state drawable
    GradientDrawable selectedStateDrawable = generateSelectedStateDrawable(radius);

    // get default state drawable
    Drawable defaultDrawable =
        ContextCompat.getDrawable(
                getContext(), R.drawable.sud_card_view_container_background_normal)
            .mutate();

    // generate a new selector drawable with selected state and default state drawable
    StateListDrawable selectorDrawable = new StateListDrawable();
    selectorDrawable.addState(new int[] {android.R.attr.state_selected}, selectedStateDrawable);
    selectorDrawable.addState(new int[] {}, defaultDrawable);

    // set the selector drawable to the background drawable
    LinearLayout layout = findViewById(R.id.sud_card_view_default);
    if (layout != null) {
      layout.setBackground(selectorDrawable);
    }
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  GradientDrawable generateSelectedStateDrawable(float radius) {
    GradientDrawable selectedStateDrawable = new GradientDrawable();
    selectedStateDrawable.setShape(GradientDrawable.RECTANGLE);
    selectedStateDrawable.setColor(
        ContextCompat.getColor(getContext(), R.color.sud_card_view_selected_background_color));
    selectedStateDrawable.setCornerRadius(radius);
    return selectedStateDrawable;
  }
}
