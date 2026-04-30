package com.sakny.message.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /*
         * Enable a simple in-memory broker for:
         *   - /topic  → broadcast (one-to-many)
         *   - /queue  → personal queues (one-to-one), used with convertAndSendToUser
         *
         * For production consider replacing the in-memory broker with a
         * full-featured broker (RabbitMQ / ActiveMQ) using registry.enableStompBrokerRelay()
         */
        registry.enableSimpleBroker("/topic", "/queue");

        /*
         * Application-specific prefix. Messages sent to /app/... are
         * routed to @MessageMapping methods in @Controller classes.
         */
        registry.setApplicationDestinationPrefixes("/app");

        /*
         * Prefix used by convertAndSendToUser() when targeting a specific user.
         * e.g. convertAndSendToUser("42", "/queue/messages", payload)
         *      → client subscribes to /user/queue/messages
         */
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /*
         * The WebSocket handshake endpoint.
         * Clients connect via SockJS at ws://<host>/ws
         * SockJS fallback is enabled for browsers that don't support native WebSockets.
         */
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*") // tighten to specific origins in production
            .withSockJS();
    }
}
