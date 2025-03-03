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
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.setupdesign.R;

/** A card view that can be used to display a title and an icon. */
public class CardView extends LinearLayout implements View.OnClickListener {

  private Drawable icon;
  private CharSequence title;

  /* The line height of the title. */
  private int lineHeight;

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
    skipClickSelection =
        a.getBoolean(R.styleable.SudCardView_sudCardViewSkipClickSelection, /* defValue= */ false);
    lineHeight =
        a.getDimensionPixelSize(R.styleable.SudCardView_android_lineHeight, /* defValue= */ 0);
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
      titleView.setLineHeight(lineHeight);
      if (title != null) {
        titleView.setText(title);
      }
    }
  }

  public void setCardTitle(CharSequence title) {
    this.title = title;
    if (titleView != null) {
      titleView.setText(title);
    }
  }

  public void setCardIcon(Drawable icon) {
    this.icon = icon;
    if (iconView != null) {
      iconView.setImageDrawable(icon);
    }
  }

  public CharSequence getCardTitle() {
    return title;
  }

  public Drawable getCardIcon() {
    return icon;
  }

  /** Sets the line height of the title. */
  public void setLineHeight(int lineHeight) {
    this.lineHeight = lineHeight;
    if (titleView != null) {
      titleView.setLineHeight(lineHeight);
    }
  }

  /** Returns the line height of the title. */
  public int getLineHeight() {
    if (titleView != null) {
      return titleView.getLineHeight();
    }
    return lineHeight;
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
}
