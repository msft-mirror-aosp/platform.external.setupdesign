/*
 * Copyright (C) 2015 The Android Open Source Project
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
import android.util.AttributeSet;
import com.google.android.setupdesign.R;

/** Definition of an non-actionable item in an {@link ItemHierarchy}. */
public class NonActionableItem extends Item {

  public NonActionableItem() {
    super();
  }

  public NonActionableItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.sud_non_actionable_items_default;
  }

  @Override
  public boolean isEnabled() {
    // There will be no action corresponding to NonActionableItem, so it will always be disabled.
    return false;
  }

  @Override
  public boolean isActionable() {
    return false;
  }
}
