package com.intezya.abysscore.service

class EventPublisher

// TODO
// @Component
// class EventPublisher(
//    private val kafkaTemplate: KafkaTemplate<String, String>,
//    private val objectMapper: ObjectMapper,
// ) {
//    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
//
//    fun sendActionEvent(
//        event: Any,
//        eventKey: String,
//        topic: String,
//    ) {
//        coroutineScope.launch {
//            val message = objectMapper.writeValueAsString(event)
//            kafkaTemplate.send(topic, eventKey, message)
//        }
//    }
// }
