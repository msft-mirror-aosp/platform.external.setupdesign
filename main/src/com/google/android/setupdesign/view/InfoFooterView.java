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
import com.google.android.setupdesign.R;

/**
 * An extension of ScrollView that will invoke a listener callback when the ScrollView needs
 * scrolling, and when the ScrollView is being scrolled to the bottom. This is often used in Setup
 * Wizard as a way to ensure that users see all the content before proceeding.
 */
public class InfoFooterView extends LinearLayout {

  @Nullable private Drawable icon;
  @Nullable private CharSequence title;

  private RichTextView titleView;
  private ImageView iconView;

  public InfoFooterView(Context context) {
    super(context);
    init();
  }

  public InfoFooterView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudInfoFooterView);
    icon = a.getDrawable(R.styleable.SudInfoFooterView_android_icon);
    title = a.getText(R.styleable.SudInfoFooterView_android_title);
    a.recycle();
    init();
  }

  public InfoFooterView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudInfoFooterView);
    icon = a.getDrawable(R.styleable.SudInfoFooterView_android_icon);
    title = a.getText(R.styleable.SudInfoFooterView_android_title);
    a.recycle();
    init();
  }

  private void init() {
    View.inflate(getContext(), R.layout.sud_info_footer_default, this);
    titleView = findViewById(R.id.sud_info_footer_title);
    iconView = findViewById(R.id.sud_info_footer_icon);
    if (titleView != null && title != null) {
      titleView.setText(title);
      titleView.setVisibility(View.VISIBLE);
    }
    if (iconView != null && icon != null) {
      iconView.setImageDrawable(icon);
      iconView.setVisibility(View.VISIBLE);
    }
  }

  public void setTitle(CharSequence title) {
    this.title = title;
    if (titleView != null) {
      titleView.setText(title);
      titleView.setVisibility(View.VISIBLE);
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
}
