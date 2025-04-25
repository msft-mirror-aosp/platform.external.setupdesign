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

package com.google.android.setupdesign.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Outline;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.widget.CardBackgroundDrawable;

/** An ImageView that displays an app icon according to the icon uniformity spec. */
public class IconUniformityAppImageView extends ImageView
    implements IconUniformityAppImageViewBindable {
  // Scaling factor for inset on each side of legacy icon.
  private static final Float LEGACY_SIZE_SCALE_FACTOR = 0.75f;

  private static final Float LEGACY_SIZE_SCALE_MARGIN_FACTOR = (1f - LEGACY_SIZE_SCALE_FACTOR) / 2f;

  // Apps & games radius is 20% of icon height.
  private static final Float APPS_ICON_RADIUS_MULTIPLIER = 0.20f;

  private boolean useCircleIcon;
  @ColorRes private int backdropColorResId = 0;

  private static final boolean ON_L_PLUS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

  private CardBackgroundDrawable cardBackgroundDrawable;
  /** Drawable used as background after the actual image data is visible. */
  private final GradientDrawable backdropDrawable = new GradientDrawable();

  public IconUniformityAppImageView(Context context) {
    super(context);
  }

  public IconUniformityAppImageView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public IconUniformityAppImageView(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(23)
  public IconUniformityAppImageView(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    backdropColorResId = R.color.sud_uniformity_backdrop_color;
    backdropDrawable.setColor(ContextCompat.getColor(getContext(), backdropColorResId));
  }

  @Override
  public void bindView(IconUniformityAppImageViewData viewData) {
    useCircleIcon = viewData.useCircleIcon;
    float radius = getLayoutParams().height * APPS_ICON_RADIUS_MULTIPLIER;

    if (Build.VERSION.SDK_INT <= 17) {
      // clipPath is not supported on hardware accelerated canvas so won't take effect unless we
      // manually set to software.
      setLayerType(LAYER_TYPE_SOFTWARE, /* paint= */ null);
    }

    setLegacyTransformationMatrix(
        viewData.icon.getMinimumWidth(),
        viewData.icon.getMinimumHeight(),
        getLayoutParams().width,
        getLayoutParams().height);

    if (ON_L_PLUS) {
      setBackgroundColor(ContextCompat.getColor(getContext(), backdropColorResId));
      setElevation(getContext().getResources().getDimension(R.dimen.sud_icon_uniformity_elevation));
      setClipToOutline(true);
      setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
              if (useCircleIcon) {
                setCircleIconTransformation(viewData, outline);

              } else {
                backdropDrawable.setCornerRadius(radius);
                outline.setRoundRect(
                    /* left= */ 0,
                    /* top= */ 0,
                    /* right= */ getLayoutParams().width,
                    /* bottom= */ getLayoutParams().height,
                    /* radius= */ radius);
              }
            }
          });
    } else {
      cardBackgroundDrawable =
          new CardBackgroundDrawable(
              ContextCompat.getColor(getContext(), backdropColorResId),
              /* radius= */ radius,
              /* inset= */ 0f);
      cardBackgroundDrawable.setBounds(
          /* left= */ 0,
          /* top= */ 0,
          /* right= */ getLayoutParams().width,
          /* bottom= */ getLayoutParams().height);
    }

    setImageDrawable(viewData.icon);
  }

  @Override
  public void onRecycle() {
    setImageDrawable(null);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (!ON_L_PLUS && cardBackgroundDrawable != null) {
      cardBackgroundDrawable.draw(canvas);
    }
    super.onDraw(canvas);
  }

  /**
   * Sets the color of backdrop drawable.
   *
   * @param backdropColorResId the resource id of the backdrop color
   */
  public void setBackdropDrawableColor(int backdropColorResId) {
    this.backdropColorResId = backdropColorResId;
    backdropDrawable.setColor(ContextCompat.getColor(getContext(), backdropColorResId));
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  private void setCircleIconTransformation(
      IconUniformityAppImageViewData viewData, Outline outline) {
    float drawableWidth = viewData.icon.getMinimumWidth();
    float drawableHeight = viewData.icon.getMinimumHeight();
    float imageViewHeight = getLayoutParams().height;
    float imageViewWidth = getLayoutParams().width;
    float outlineWidth;
    float outlineHeight;
    float widthInset;
    float heightInset;

    final float drawableAspectRatio = drawableHeight / drawableWidth;
    final float imageViewAspectRatio = imageViewHeight / imageViewWidth;
    if (drawableAspectRatio > imageViewAspectRatio) {
      // Fill height first
      outlineHeight = imageViewHeight;
      outlineWidth = outlineHeight / drawableAspectRatio;
      widthInset = (imageViewWidth - outlineWidth) / 2;
      heightInset = 0;
    } else if (drawableAspectRatio < imageViewAspectRatio) {
      // Fill width first
      outlineWidth = imageViewWidth;
      outlineHeight = outlineWidth * drawableAspectRatio;
      widthInset = 0;
      heightInset =
          getScaleType() == ScaleType.FIT_START ? 0 : (imageViewHeight - outlineHeight) / 2;
    } else {
      // Equal aspect ratio
      outlineHeight = imageViewHeight;
      outlineWidth = imageViewWidth;
      widthInset = 0;
      heightInset = 0;
    }
    setScaleType(ScaleType.FIT_CENTER);

    outline.setOval(
        Math.round(widthInset),
        Math.round(heightInset),
        Math.round(widthInset + outlineWidth),
        Math.round(heightInset + outlineHeight));
  }

  private void setLegacyTransformationMatrix(
      float drawableWidth, float drawableHeight, float imageViewWidth, float imageViewHeight) {
    Matrix scaleMatrix = new Matrix();
    float verticalMargin = imageViewHeight * LEGACY_SIZE_SCALE_MARGIN_FACTOR;
    float horizontalMargin = imageViewWidth * LEGACY_SIZE_SCALE_MARGIN_FACTOR;
    RectF scrRectF = new RectF(0f, 0f, drawableWidth, drawableHeight);
    RectF destRectF =
        new RectF(
            horizontalMargin,
            verticalMargin,
            imageViewWidth - horizontalMargin,
            imageViewHeight - verticalMargin);

    scaleMatrix.setRectToRect(scrRectF, destRectF, ScaleToFit.FILL);

    setScaleType(ScaleType.MATRIX);
    setImageMatrix(scaleMatrix);
  }
}
