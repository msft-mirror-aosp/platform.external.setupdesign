/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.setupdesign.transition.support;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.errorprone.annotations.InlineMe;

/** Helper class for apply the transition to the pages which uses support library. */
public class TransitionHelper {

  private static final String TAG = "TransitionHelper";

  private TransitionHelper() {}

  /**
   * Apply the transition for going forward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link
   * com.google.android.setupdesign.transition.TransitionHelper#CONFIG_TRANSITION_NONE}. The timing
   * to apply the transition is going forward from the previous {@link Fragment} to this, or going
   * forward from this {@link Fragment} to the next.
   *
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @Deprecated
  public static void applyForwardTransition(Fragment fragment) {
    Log.w(TAG, "Not apply the forward transition for support lib's fragment.");
  }

  /**
   * Apply the transition for going backward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link
   * com.google.android.setupdesign.transition.TransitionHelper#CONFIG_TRANSITION_NONE}. The timing
   * to apply the transition is going backward from the next {@link Fragment} to this, or going
   * backward from this {@link Fragment} to the previous.
   *
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @Deprecated
  public static void applyBackwardTransition(Fragment fragment) {
    Log.w(TAG, "Not apply the backward transition for support lib's fragment.");
  }

  /**
   * A wrapper method, create an {@link ActivityOptionsCompat} to transition between activities as
   * the {@link ActivityOptionsCompat} parameter of {@link
   * androidx.activity.result.ActivityResultLauncher#launch(I, ActivityOptionsCompat)} method.
   *
   * @deprecated Deprecated to use CONFIG_TRANSITION_SHARED_X_AXIS transition, so it never have
   *     activity options input, should start the activity directly.
   */
  @Deprecated
  @InlineMe(replacement = "null")
  @Nullable
  public static ActivityOptionsCompat makeActivityOptionsCompat(Activity activity) {
    return null;
  }
}
