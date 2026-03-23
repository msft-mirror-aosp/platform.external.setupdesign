/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.google.android.setupdesign.items

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.setupdesign.R
import com.google.android.setupdesign.view.InfoFooterView

/** An item that displays an [InfoFooterView]. */
class InfoFooterItem : Item {
  constructor() : super()

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  override fun getDefaultLayoutResource(): Int {
    return R.layout.sud_info_footer_item
  }

  override fun onBindView(view: View?) {
    if (view is InfoFooterView) {
      view.title = title
      view.icon = icon
    }
  }

  /**
   * InfoFooterItem is set as GroupDivider to remove the default item background that are set in
   * ListView and RecyclerViews for all the items that are not group divider.
   */
  override fun isGroupDivider(): Boolean {
    return true
  }

  /**
   * This is disabled to remove the touch feedback for info footer items. If there is any touch
   * event for the InfoFooterItem, override this method to set isEnabled() true
   */
  override fun isEnabled(): Boolean {
    return false
  }
}
