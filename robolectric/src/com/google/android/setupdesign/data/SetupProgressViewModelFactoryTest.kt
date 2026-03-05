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

package com.google.android.setupdesign.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SetupProgressViewModelFactoryTest {

  private lateinit var context: Context
  private lateinit var factory: SetupProgressViewModelFactory

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    factory = SetupProgressViewModelFactory(context)
  }

  @Test
  fun create_whenSetupProgressViewModelClass_shouldReturnSetupProgressViewModel() {
    val viewModel = factory.create(SetupProgressViewModel::class.java)

    assertThat(viewModel).isInstanceOf(SetupProgressViewModel::class.java)
  }

  @Test
  fun create_whenOtherViewModelClass_shouldThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException::class.java) {
      factory.create(OtherViewModel::class.java)
    }
  }

  @Test
  fun createWithExtras_whenSetupProgressViewModelClass_shouldReturnSetupProgressViewModel() {
    val viewModel = factory.create(SetupProgressViewModel::class.java, CreationExtras.Empty)

    assertThat(viewModel).isInstanceOf(SetupProgressViewModel::class.java)
  }

  @Test
  fun createWithExtras_whenOtherViewModelClass_shouldThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException::class.java) {
      factory.create(OtherViewModel::class.java, CreationExtras.Empty)
    }
  }

  private class OtherViewModel : ViewModel()
}
