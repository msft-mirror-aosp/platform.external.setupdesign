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

import static java.lang.Math.max;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.VisibleForTesting;

/**
 * A TextView that, when its width is wrap_content, will repeatedly measure until we get a width
 * that actually wraps its text label.
 */
public class WrapTextView extends AppCompatTextView {

  public WrapTextView(Context context) {
    super(context);
  }

  public WrapTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public WrapTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int newWidthSpec = wrapMeasure(widthMeasureSpec);
    if (newWidthSpec != widthMeasureSpec) {
      super.onMeasure(newWidthSpec, heightMeasureSpec);
    }
  }

  @VisibleForTesting
  int wrapMeasure(int widthMeasureSpec) {
    if (View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.AT_MOST) {
      final Layout layout = getLayout();
      final int lineCount = layout.getLineCount();
      if (lineCount > 1) {
        float maxLineWidth = 0;
        for (int i = 0; i < lineCount; i++) {
          // Find the longest line width
          maxLineWidth = max(maxLineWidth, layout.getLineWidth(i));
        }
        final int newTotalWidth =
            (int) Math.ceil(maxLineWidth) + getTotalPaddingLeft() + getTotalPaddingRight();
        if (newTotalWidth < getMeasuredWidth()) {
          // Re-measure with the longest line length if it has changed.
          return View.MeasureSpec.makeMeasureSpec(newTotalWidth, View.MeasureSpec.AT_MOST);
        }
      }
    }
    return widthMeasureSpec;
  }
}
