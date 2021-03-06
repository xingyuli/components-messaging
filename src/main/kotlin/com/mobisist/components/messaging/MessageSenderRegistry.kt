package com.mobisist.components.messaging

class MessageSenderRegistry {

    private val registry: MutableMap<String, MessageSender<Message, Any>> = mutableMapOf();

    fun setRegistry(registry: Map<String, MessageSender<Message, Any>>) {
        this.registry.putAll(registry)
    }

    @Throws(MessagingException::class)
    @JvmOverloads
    fun accept(msg: Message, responseHandler: ((Any)->Unit)? = null) {
        for ((name, sender) in registry) {
            if (sender.canHandle(msg)) {
                val result = sender.send(msg)
                responseHandler?.invoke(result)
                return
            }
        }
        throw MessagingException("no corresponding sender for message with type ${msg.javaClass}")
    }

}