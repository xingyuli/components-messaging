package com.mobisist.components.messaging.baidupush

fun main(args: Array<String>) {
    sendAndroidPush()
    sendIosPush()
}

private fun sendAndroidPush() {
    val sender = BaiduPushSender().apply {
        androidConfigProvider = {
            BaiduPushConfig.AndroidPushConfig().apply {
                apiKey = "gv0ft0gzCdO19FL5OgYIqYG9"
                secretKey = "zrRgdC2LlPancB3yxrmacyosdtLh3k92"
            }
        }
    }

    val msg = sender.buildMsgToAndroid {
        pushMsgToSingleDeviceRequest {
            channelId = "3478330428537063857"
            messageType = MessageType.NOTIFICATION.intValue
            // 5 mins
            msgExpires = 300

            title = "Title goes here"
            description = "Description goes here"

            // customized body when send to android devices
            custom_content = mapOf(
                    "myKey" to "myValue"
            )
        }
    }

    try {
        sender.send(msg)
    } catch (e: BaiduPushMessagingException) {
        // you can handle exception here
    }
}

private fun sendIosPush() {
    val sender = BaiduPushSender().apply {
        iosConfigProvider = {
            BaiduPushConfig.IOSPushConfig().apply {
                apiKey = "gv0ft0gzCdO19FL5OgYIqYG9"
                secretKey = "zrRgdC2LlPancB3yxrmacyosdtLh3k92"
                deployStatus = IOSDeployStatus.DEVELOPMENT
            }
        }
    }

    val msg = sender.buildMsgToIos {
        pushMsgToSingleDeviceRequest {
            channelId = "3478330428537063857"
            messageType = MessageType.NOTIFICATION.intValue
            // 5 mins
            msgExpires = 300

            alert = "Title goes here"
            sound = "default"

            "key1" setTo "value1"
            "key2" setTo "value2"
        }
    }
    try {
        sender.send(msg)
    } catch (e: BaiduPushMessagingException) {
        // you can handle exception here
    }
}
