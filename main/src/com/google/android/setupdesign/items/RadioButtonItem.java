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
import android.widget.CompoundButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.setupdesign.R;

/**
 * An item that is displayed with a radio button, with methods to manipulate and listen to the checked
 * state of the radio button. Note that by default, only click on the radio button will change the on-off
 * state. To change the radio button state when tapping on the text, use the click handlers of list
 * view or RecyclerItemAdapter with {@link #toggle(View)}.
 */
public class RadioButtonItem extends Item implements CompoundButton.OnCheckedChangeListener {

  /** Listener for check state changes of this radio button item. */
  public interface OnCheckedChangeListener {

    /**
     * Callback when checked state of a {@link RadioButtonItem} is changed.
     *
     * @see #setOnCheckedChangeListener(OnCheckedChangeListener)
     */
    void onCheckedChange(RadioButtonItem item, boolean isChecked);
  }

  private boolean checked = false;
  private OnCheckedChangeListener listener;

  /** Creates a default radio button item. */
  public RadioButtonItem() {
    super();
  }

  /**
   * Creates a radio button item. This constructor is used for inflation from XML.
   *
   * @param context The context which this item is inflated in.
   * @param attrs The XML attributes defined on the item.
   */
  public RadioButtonItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudRadioButtonItem);
    checked = a.getBoolean(R.styleable.SudRadioButtonItem_android_checked, false);
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

  /** Returns true if this radio button item is currently checked. */
  public boolean isChecked() {
    return checked;
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.sud_items_radio_button;
  }

  /**
   * Toggle the checked state of the radio button, without invalidating the entire item.
   *
   * @param view The root view of this item, typically from the argument of onItemClick.
   */
  public void toggle(View view) {
    checked = !checked;
    final MaterialRadioButton radioButtonView = (MaterialRadioButton) view.findViewById(R.id.sud_items_radio_button);
    radioButtonView.setChecked(checked);
  }

  @Override
  public void onBindView(View view) {
    super.onBindView(view);
    final MaterialRadioButton radioButtonView =
        (MaterialRadioButton) view.findViewById(R.id.sud_items_radio_button);
    radioButtonView.setOnCheckedChangeListener(null);
    radioButtonView.setChecked(checked);
    radioButtonView.setOnCheckedChangeListener(this);
    radioButtonView.setEnabled(isEnabled());
  }

  /**
   * Sets a listener to listen for changes in checked state. This listener is invoked in both user
   * toggling the radio button and calls to {@link #setChecked(boolean)}.
   */
  public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
    this.listener = listener;
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    checked = isChecked;
    if (listener != null) {
      listener.onCheckedChange(this, isChecked);
    }
  }
}