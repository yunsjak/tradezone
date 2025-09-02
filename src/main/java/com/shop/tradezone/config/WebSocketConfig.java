package com.shop.tradezone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import com.shop.tradezone.websocket.TopicAuthInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final TopicAuthInterceptor interceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-stomp").setAllowedOriginPatterns("*") // 운영에서는 특정 도메인만!
				.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 구독(/topic/...) 지원
		registry.enableSimpleBroker("/topic", "/queue").setHeartbeatValue(new long[] { 10000, 10000 }) // 서버↔클라이언트 10초
																										// 하트비트
				.setTaskScheduler(heartbeatScheduler()); // 하트비트 스케줄러
		registry.setApplicationDestinationPrefixes("/app"); // 전송(/app/...)
		registry.setUserDestinationPrefix("/user"); // convertAndSendToUser 용
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(interceptor);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
		registry.setMessageSizeLimit(128 * 1024); // 128KB
		registry.setSendTimeLimit(20 * 1000); // 20s
		registry.setSendBufferSizeLimit(512 * 1024); // 512KB
	}

	@Bean
	public TaskScheduler heartbeatScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(1);
		scheduler.setThreadNamePrefix("ws-heartbeat-");
		scheduler.initialize();
		return scheduler;
	}

}
