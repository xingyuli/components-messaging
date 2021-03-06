package com.mobisist.components.messaging.baidupush

import com.baidu.yun.push.model.PushMsgToAllRequest
import com.baidu.yun.push.model.PushMsgToSingleDeviceRequest
import com.baidu.yun.push.model.PushMsgToTagRequest
import com.baidu.yun.push.model.PushRequest
import com.google.gson.Gson
import com.mobisist.components.messaging.Message

enum class MessageType(val rawValue: Int) {

    // 透传消息
    MESSAGE(0),

    // 通知
    NOTIFICATION(1)

}

enum class DeviceType(val rawValue: Int) {
    ANDROID(3),
    IOS(4)
}

enum class IOSDeployStatus(val rawValue: Int) {
    DEVELOPMENT(1),
    PRODUCT(2)
}

sealed class BaiduPushMessage(val config: String) : Message {

    lateinit var req: PushRequest

    class AndroidPushMessage @JvmOverloads constructor(config: String = "default") : BaiduPushMessage(config)
    class IOSPushMessage @JvmOverloads constructor(config: String = "default") : BaiduPushMessage(config)

    open class MsgBuilder<out C : BaiduPushConfig>(private val pushConfig: C) {

        internal val body = mutableMapOf<String, Any?>()
        private val gson = Gson()

        private val defaultSettings = mutableMapOf<Class<out PushRequest>, Any>()

        @JvmOverloads
        fun pushMsgToSingleDeviceRequest(generateMsg: Boolean = true,
                                         init: PushMsgToSingleDeviceRequest.() -> Unit): PushMsgToSingleDeviceRequest {
            val req = PushMsgToSingleDeviceRequest()
            req.applyDefaultSettings()
            req.init()
            if (generateMsg) {
                req.message = Gson().toJson(body)
            }
            return req
        }

        @JvmOverloads
        fun pushMsgToTagRequest(generateMsg: Boolean = true, init: PushMsgToTagRequest.() -> Unit): PushMsgToTagRequest {
            val req = PushMsgToTagRequest()
            req.init()
            if (generateMsg) {
                req.message = gson.toJson(body)
            }
            return req
        }


        @JvmOverloads
        fun pushMsgToAllRequest(generateMsg: Boolean = true, init: PushMsgToAllRequest.() -> Unit): PushMsgToAllRequest {
            val req = PushMsgToAllRequest()
            req.init()
            if (generateMsg) {
                req.message = gson.toJson(body)
            }
            return req
        }

        protected fun <T : PushRequest> defaultSettings(type: Class<T>, init: T.(C) -> Unit) {
            defaultSettings[type] = init
        }

        private fun <T : PushRequest> T.applyDefaultSettings() {
            defaultSettings.filter { it.key.isAssignableFrom(this.javaClass) }.values.forEach {
                @Suppress("UNCHECKED_CAST")
                val fn = it as T.(C) -> Unit
                this.fn(pushConfig)
            }
        }

    }

    class AndroidMsgBuilder(pushConfig: BaiduPushConfig.AndroidPushConfig) : MsgBuilder<BaiduPushConfig.AndroidPushConfig>(pushConfig) {
        var title: String by body
        var description: String by body
        var custom_content: Map<String, Any> by body

        init {
            defaultSettings(PushRequest::class.java) {
                setDeviceType(DeviceType.ANDROID.rawValue)
            }
        }
    }

    class IOSMsgBuilder(pushConfig: BaiduPushConfig.IOSPushConfig) : MsgBuilder<BaiduPushConfig.IOSPushConfig>(pushConfig) {

        private val aps: MutableMap<String, Any> = body.getOrPut("aps", { mutableMapOf<String, Any>() }) as MutableMap<String, Any>

        var alert: String by aps
        var sound: String by aps
        var badge: Int by aps

        var contentAvailable: String?
            get() = aps["content-available"] as String?
            set(value) {
                aps["content-available"] = value.toString()
            }

        init {
            defaultSettings(PushRequest::class.java) {
                setDeviceType(DeviceType.IOS.rawValue)
            }
            defaultSettings(PushMsgToSingleDeviceRequest::class.java) {
                deployStatus = it.deployStatus.rawValue
            }
        }

        infix fun String.setTo(value: Any) {
            body[this] = value
        }

    }

}
