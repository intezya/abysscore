package com.intezya.abysscore.config

// TODO
// @Configuration
// @EnableWebSocket
// class WebSocketConfig(
//    private val jwtUtils: JwtUtils,
//    private val clientWebsocketHandler: ClientWebsocketHandler,
// ) : WebSocketConfigurer {
//    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
//        registry
//            .addHandler(clientWebsocketHandler, "/websocket/hubs/client")
//            .setAllowedOrigins("*")
//            .addInterceptors(
//                object : HandshakeInterceptor {
//                    override fun beforeHandshake(
//                        request: ServerHttpRequest,
//                        response: ServerHttpResponse,
//                        wsHandler: WebSocketHandler,
//                        attributes: MutableMap<String, Any>,
//                    ): Boolean {
//                        val token =
//                            request.headers.getFirst("Authorization")?.removePrefix("Bearer ")
//                                ?: return false
//
//                        // Validation skipped cause it autovalidates in WebSecurity (as middleware)
//                        val userInfo = jwtUtils.(token)
//                        attributes["user_info"] = userInfo
//                        return true
//                    }
//
//                    override fun afterHandshake(
//                        request: ServerHttpRequest,
//                        response: ServerHttpResponse,
//                        wsHandler: WebSocketHandler,
//                        exception: Exception?,
//                    ) {
//                        // No-op
//                    }
//                },
//            )
//    }
// }
