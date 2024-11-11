package io.agora.api.example.examples.advanced.beauty

import android.content.Context
import android.util.Log
import io.agora.beautyapi.aliyunqueen.AliyunQueenBeautyAPI

object AliyunQueenBeautySDK {

    private const val TAG = "AliyunQueenBeautySDK"

    private val LICENSE_NAME = "Agora_test_20240412_20240712_io.agora.entfull_4.5.0_1443.licbag"

    private val nodesLoaded = mutableListOf<String>()

    private var beautyAPI: AliyunQueenBeautyAPI? = null

    // 特效句柄

    fun initBeautySDK(context: Context): Boolean {

        return true
    }

    // GL Thread
    fun initEffect(context: Context) {

    }

    // GL Thread
    fun unInitEffect() {
        beautyAPI = null
        nodesLoaded.clear()
    }

    private fun checkResult(msg: String, ret: Int): Boolean {
        if (ret != 0 && ret != -11 && ret != 1) {
            val log = "$msg error: $ret"
            Log.e(TAG, log)
            return false
        }
        return true
    }

    internal fun setBeautyAPI(beautyAPI: AliyunQueenBeautyAPI?) {
        this.beautyAPI = beautyAPI
    }

    private fun runOnBeautyThread(run: () -> Unit) {
        beautyAPI?.runOnProcessThread(run) ?: run.invoke()
    }

}