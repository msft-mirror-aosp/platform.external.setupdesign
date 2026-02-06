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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.google.android.setupcompat.restore.restoreprogress.IRestoreProgressCallback
import com.google.android.setupcompat.restore.restoreprogress.IRestoreProgressService
import com.google.android.setupcompat.restore.restoreprogress.RestoreProgressUiData
import com.google.android.setupcompat.util.Logger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min
import kotlin.math.pow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.retryWhen

/** Exception used to signal that the service connection was lost and needs a retry. */
private class ServiceConnectionException(message: String) : Exception(message)

/**
 * Repository to manage connection to [IRestoreProgressService] and provide progress updates. This
 * implementation uses cold flows to manage service connection and provide [RestoreProgressUiData]
 * updates.
 */
class RestoreProgressRepository(private val context: Context) {

  private val currentService = AtomicReference<IRestoreProgressService?>(null)

  private val serviceFlow: Flow<IRestoreProgressService> = callbackFlow {
    val intent = Intent(IRestoreProgressService.INTENT_ACTION).setPackage(SERVICE_PACKAGE)

    val connection =
      object : ServiceConnection {

        fun signalRetry(message: String) {
          logger.atInfo("Signaling retry: $message")
          close(ServiceConnectionException(message))
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
          logger.atInfo("RestoreProgressService connected")
          val binder = IRestoreProgressService.Stub.asInterface(service)
          if (binder != null) {
            currentService.set(binder)
            val unused = trySend(binder)
          } else {
            logger.e("Service binder was null. Closing flow gracefully.")
            close()
          }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
          signalRetry("Service disconnected (transient)")
        }

        override fun onBindingDied(name: ComponentName?) {
          signalRetry("Binding died (permanent)")
        }

        override fun onNullBinding(name: ComponentName?) {
          logger.e("Service rejected intent. Closing flow gracefully.")
          close()
        }
      }

    logger.atInfo("Attempting bindService")
    try {
      val success = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
      if (!success) {
        connection.signalRetry("Failed to bind to service")
      }
    } catch (e: SecurityException) {
      logger.e("SecurityException during bindService", e)
      close(e)
    }

    awaitClose {
      logger.atInfo("Unbinding from RestoreProgressService")
      try {
        context.unbindService(connection)
      } catch (e: IllegalArgumentException) {
        logger.w("Error while unbinding: ${e.message}")
      } finally {
        currentService.set(null)
      }
    }
  }

  /**
   * Flow of [RestoreProgressUiData] emitted by the [IRestoreProgressService].
   *
   * Collecting this flow triggers binding to the service, and retries if the connection fails or is
   * lost. The flow will emit a new value whenever the service sends a new progress update. Once the
   * flow collection is stopped, the service is unbound.
   *
   * Note: Each new collection will trigger a new connection attempt.
   */
  val progressUiDataFlow: Flow<RestoreProgressUiData?> =
    serviceFlow
      .flatMapLatest { service -> createRestoreProgressFlow(service) }
      .retryWhen { cause, attempt ->
        if (cause is ServiceConnectionException) {
          val delayMs =
            min(
              MAX_RETRY_DELAY_MS,
              (INITIAL_RETRY_DELAY_MS * EXPONENTIAL_BACKOFF_FACTOR.pow(attempt.toDouble())).toLong(),
            )
          logger.atInfo("Retrying connection in $delayMs ms (attempt $attempt)")
          delay(delayMs)
          true
        } else {
          false
        }
      }

  /** Calls the onProgressIndicatorClicked method on the connected service. */
  suspend fun notifyProgressIndicatorClicked() {
    val service = currentService.get()
    if (service == null) {
      logger.w("Cannot notify service of progress indicator click, service is not connected")
      return
    }

    try {
      service.notifyProgressIndicatorClicked()
      logger.atDebug("Notified service of progress indicator click")
    } catch (e: RemoteException) {
      logger.e("Failed to notify service of progress indicator click", e)
    }
  }

  private fun createRestoreProgressFlow(
    service: IRestoreProgressService
  ): Flow<RestoreProgressUiData?> = callbackFlow {
    val callback =
      object : IRestoreProgressCallback.Stub() {
        override fun onProgressUpdated(uiData: RestoreProgressUiData) {
          logger.atDebug("onProgressUpdated called with RestoreProgressUiData: $uiData")
          val unused = trySend(uiData)
        }
      }

    try {
      logger.atInfo("Registering AIDL callback")
      service.registerCallbackForProgressUpdates(callback)
    } catch (e: RemoteException) {
      logger.e("Failed to register AIDL callback", e)
      close(ServiceConnectionException("Failed to register AIDL callback"))
    }

    awaitClose {
      try {
        logger.atInfo("Unregistering AIDL callback")
        service.unregisterCallbackForProgressUpdates(callback)
      } catch (e: RemoteException) {
        logger.w("Failed to unregister AIDL callback: ${e.message}")
      }
    }
  }

  companion object {
    private val logger = Logger("RestoreProgressRepository")

    // TODO: b/472218697 - Fetch the package name from PartnerConfig to support OEM implementations
    // for IRestoreProgressService.
    /** Package name of the app that provides the [IRestoreProgressService]. */
    const val SERVICE_PACKAGE = "com.google.android.apps.restore"

    private const val INITIAL_RETRY_DELAY_MS = 1000L
    private const val MAX_RETRY_DELAY_MS = 60000L
    private const val EXPONENTIAL_BACKOFF_FACTOR = 2.0
  }
}
