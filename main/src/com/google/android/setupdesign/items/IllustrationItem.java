/*
 * Copyright (C) 2021 The Android Open Source Project
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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.google.android.setupdesign.R;
import org.jetbrains.annotations.Nullable;

/**
 * An item that is displayed with a Illustration, with methods to manipulate state of the imageView.
 */
public class IllustrationItem extends Item {

  private Drawable illustration;

  IllustrationItem() {
    super();
  }

  public IllustrationItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudIllustrationItem);
    this.illustration = a.getDrawable(R.styleable.SudIllustrationItem_android_drawable);
    a.recycle();
  }

  public Drawable getIllustration() {
    return illustration;
  }

  public void setIllustration(Drawable value) {
    this.illustration = value;
    this.notifyItemChanged();
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.sud_illustration_item;
  }

  @Override
  public void onBindView(@Nullable View view) {
    if (view != null) {
      view.setContentDescription(getContentDescription());
      ImageView imageView = view.findViewById(R.id.sud_item_illustration);
      imageView.setImageDrawable(getIllustration());
    }

  }

  /**
   * IllustrationItem is set as GroupDivider to remove the default item background that are set in
   * ListView and RecyclerViews for all the items that are not group divider.
   */
  @Override
  public boolean isGroupDivider() {
    return true;
  }

  /**
   * This is disabled to remove the touch feedback for imageView items. If there is any touch event
   * for the IllustrationItem, override this method to set isEnabled() true
   */
  @Override
  public boolean isEnabled() {
    return false;
  }
}
