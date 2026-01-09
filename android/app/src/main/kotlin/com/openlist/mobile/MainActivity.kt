package com.openlist.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.openlist.mobile.bridge.AndroidBridge
import com.openlist.mobile.bridge.AppConfigBridge
import com.openlist.mobile.bridge.CommonBridge
import com.openlist.mobile.bridge.ServiceBridge
import com.openlist.mobile.model.ShortCuts
import com.openlist.mobile.model.openlist.Logger
import com.openlist.pigeon.GeneratedApi
import com.openlist.pigeon.GeneratedApi.VoidResult
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : FlutterActivity() {
    companion object {
        private const val TAG = "MainActivity"
        
        // 静态引用，供其他组件访问
        @Volatile
        var serviceBridge: ServiceBridge? = null
            private set
    }

    private val receiver by lazy { MyReceiver() }
    private var mEvent: GeneratedApi.Event? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ShortCuts.buildShortCuts(this)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(OpenListService.ACTION_STATUS_CHANGED))

        GeneratedPluginRegistrant.registerWith(this.flutterEngine!!)

        val binaryMessage = flutterEngine!!.dartExecutor.binaryMessenger
        GeneratedApi.AppConfig.setUp(binaryMessage, AppConfigBridge)
        GeneratedApi.Android.setUp(binaryMessage, AndroidBridge(this))
        GeneratedApi.NativeCommon.setUp(binaryMessage, CommonBridge(this))
        mEvent = GeneratedApi.Event(binaryMessage)

        // 设置服务桥接
        val serviceChannel = MethodChannel(binaryMessage, "com.openlist.mobile/service")
        serviceBridge = ServiceBridge(this, serviceChannel)

        Logger.addListener(object : Logger.Listener {
            override fun onLog(level: Int, time: String, msg: String) {
                GlobalScope.launch(Dispatchers.Main) {
                    mEvent?.onServerLog(level.toLong(), time, msg, object : VoidResult {
                        override fun success() {

                        }

                        override fun error(error: Throwable) {
                        }

                    })
                }
            }

        })
    }

    // REMOVIDO: onPause ya no hace sync
    // El sync constante en onPause causaba syncs excesivos cada vez que
    // cambias de app o bloqueas la pantalla
    
    // REMOVIDO: onStop ya no hace sync
    // El sync constante en onStop causaba syncs excesivos
    
    // REMOVIDO: onTrimMemory ya no hace sync
    // El sync por presión de memoria causaba syncs excesivos

    override fun onDestroy() {
        super.onDestroy()
        // Solo hacer sync cuando la Activity se destruye COMPLETAMENTE
        // (cuando el usuario cierra la app o el sistema la mata)
        Log.d(TAG, "MainActivity being destroyed - database sync handled by service")
        
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        
        // El sync se maneja automáticamente en OpenListService.onDestroy()
        // No necesitamos hacerlo aquí
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                OpenListService.ACTION_STATUS_CHANGED -> {
                    Log.d(TAG, "onReceive: ACTION_STATUS_CHANGED")

                    mEvent?.onServiceStatusChanged(OpenListService.isRunning, object : VoidResult {
                        override fun success() {}
                        override fun error(error: Throwable) {
                        }
                    })
                }
            }
        }
    }
}
