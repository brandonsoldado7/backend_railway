package neuro_activity.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configura STOMP sobre SockJS.
 *
 * Canales:
 *  /topic/diagram.{id}        → broadcast del modelo JSON (solo lectura)
 *  /topic/presence.{id}       → lista de usuarios activos en tiempo real
 *  /app/diagram.{id}.join     → un viewer se une
 *  /app/diagram.{id}.leave    → un viewer se va
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins);
    }
}