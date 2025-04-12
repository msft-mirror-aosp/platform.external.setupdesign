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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.ThemeHelper;

/**
 * An item that is displayed with a check box, with methods to manipulate and listen to the checked
 * state of the check box. Note that by default, only click on the check box will change the on-off
 * state. To change the check box state when tapping on the text, use the click handlers of list
 * view or RecyclerItemAdapter with {@link #toggle(View)}.
 */
public class CheckBoxItem extends Item
    implements CompoundButton.OnCheckedChangeListener, OnClickListener {

  /** Listener for check state changes of this check box item. */
  public interface OnCheckedChangeListener {

    /**
     * Callback when checked state of a {@link CheckBoxItem} is changed.
     *
     * @see #setOnCheckedChangeListener(OnCheckedChangeListener)
     */
    void onCheckedChange(CheckBoxItem item, boolean isChecked);
  }

  private boolean checked = false;
  private OnCheckedChangeListener listener;

  /** Creates a default check box item. */
  public CheckBoxItem() {
    super();
  }

  /**
   * Creates a check box item. This constructor is used for inflation from XML.
   *
   * @param context The context which this item is inflated in.
   * @param attrs The XML attributes defined on the item.
   */
  public CheckBoxItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudCheckBoxItem);
    checked = a.getBoolean(R.styleable.SudCheckBoxItem_android_checked, false);
    a.recycle();
  }

  /** Sets whether this item should be checked. */
  public void setChecked(boolean checked) {
    if (this.checked != checked) {
      this.checked = checked;
      notifyItemChanged();
      if (listener != null) {
        listener.onCheckedChange(this, checked);
      }
    }
  }

  /** Sets whether this item should be checked and does not notify the listener. */
  public void setCheckedWithoutNotify(boolean checked) {
    if (this.checked != checked) {
      this.checked = checked;
      notifyItemChanged();
    }
  }

  /** Returns true if this check box item is currently checked. */
  public boolean isChecked() {
    return checked;
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.sud_items_check_box;
  }

  /**
   * Toggle the checked state of the check box, without invalidating the entire item.
   *
   * @param view The root view of this item, typically from the argument of onItemClick.
   */
  public void toggle(View view) {
    checked = !checked;
    final CheckBox checkBoxView = (CheckBox) view.findViewById(R.id.sud_items_check_box);
    checkBoxView.setChecked(checked);
  }

  @Override
  public void onBindView(View view) {
    super.onBindView(view);
    view.setOnClickListener(this);
    final CheckBox checkBoxView = (CheckBox) view.findViewById(R.id.sud_items_check_box);
    if (ThemeHelper.shouldApplyGlifExpressiveStyle(view.getContext())) {
      checkBoxView.setClickable(false);
    }
    checkBoxView.setOnCheckedChangeListener(null);
    checkBoxView.setChecked(checked);
    checkBoxView.setOnCheckedChangeListener(this);
    checkBoxView.setEnabled(isEnabled());
  }

  /**
   * Sets a listener to listen for changes in checked state. This listener is invoked in both user
   * toggling the check box and calls to {@link #setChecked(boolean)}.
   */
  public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
    this.listener = listener;
  }

  @Override
  public void onClick(View v) {
    setChecked(!checked);
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    checked = isChecked;
    if (listener != null) {
      listener.onCheckedChange(this, isChecked);
    }
  }
}
