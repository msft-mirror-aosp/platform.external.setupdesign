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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.google.android.setupdesign.R;

/**
 * An {@link ItemGroup} which also displays a header item if there are any visible child (ignoring
 * the header itself).
 */
public class SectionItem extends ItemGroup {

  private final Item header;

  public SectionItem() {
    super();
    header = new SectionHeaderItem();
    header.setVisible(false);
    addChild(header);
  }

  public SectionItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudSectionItem);
    CharSequence headerText = a.getText(R.styleable.SudSectionItem_android_title);
    a.recycle();
    header = new SectionHeaderItem();
    header.setTitle(headerText);
    header.setVisible(false);
    addChild(header);
  }

  public Item getHeader() {
    return header;
  }

  public void setHeaderTitle(CharSequence headerText) {
    header.setTitle(headerText);
    refreshHeader();
  }

  @Override
  public void addChild(ItemHierarchy child) {
    super.addChild(child);
    refreshHeader();
  }

  @Override
  public void onItemRangeRemoved(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    super.onItemRangeRemoved(itemHierarchy, positionStart, itemCount);
    refreshHeader();
  }

  @Override
  public void onItemRangeInserted(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    super.onItemRangeInserted(itemHierarchy, positionStart, itemCount);
    refreshHeader();
  }

  private void refreshHeader() {
    if (header.isVisible()) {
      if (getCount() == 1) {
        // The header is the only visible item in this group. Hide it so the entire group is not
        // shown.
        header.setVisible(false);
      }
    } else {
      if (getCount() > 0 && header.getTitle() != null) {
        // Header is not currently visible but there are children in this group. Show the header as
        // well.
        header.setVisible(true);
      }
    }
  }
}
