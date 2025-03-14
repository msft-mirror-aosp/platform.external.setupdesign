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
import android.util.AttributeSet;
import android.view.View;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.setupdesign.R;
import org.jetbrains.annotations.Nullable;

/** An item that is displayed with a Lottie animation. */
public class LottieIllustrationItem extends Item {
  private int animationId;
  @Nullable private AnimationViewListener animationViewListener;

  /** Listener for the state of the LottieAnimationView. */
  public interface AnimationViewListener {
    /**
     * Called when a LottieAnimationView is bound to the item. This enables applying any additional
     * customizations to the animation view.
     */
    void onAnimationViewBound(LottieAnimationView animationView);
  }

  LottieIllustrationItem() {
    super();
  }

  public LottieIllustrationItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudIllustrationItem);
    this.animationId = a.getResourceId(R.styleable.SudIllustrationItem_sudAnimationId, 0);
    a.recycle();
  }

  /**
   * Sets the animation and prepares to play it once the view is bound.
   *
   * @param animationId the resource id of the Lottie animation.
   * @param animationViewListener see {@link AnimationViewListener}.
   */
  public void setAnimation(int animationId, AnimationViewListener animationViewListener) {
    this.animationId = animationId;
    this.animationViewListener = animationViewListener;
    this.notifyItemChanged();
  }

  public int getAnimationId() {
    return animationId;
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.sud_lottie_illustration_item;
  }

  @Override
  public void onBindView(@Nullable View view) {
    if (view != null) {
      view.setContentDescription(getContentDescription());

      LottieAnimationView animationView = view.findViewById(R.id.sud_item_lottie_illustration);
      if (animationId != 0) {
        animationView.setAnimation(animationId);
      }
      if (animationViewListener != null) {
        animationViewListener.onAnimationViewBound(animationView);
      }
      animationView.playAnimation();
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
