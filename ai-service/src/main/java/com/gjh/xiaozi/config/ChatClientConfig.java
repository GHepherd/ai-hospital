package com.gjh.xiaozi.config;

import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import com.gjh.xiaozi.tool.AppointmentTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Autowired
    private AppointmentTools appointmentTools;

    @Bean
    public RedisChatMemoryRepository RedisChatMemoryRepository(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port
    ) {
        return new RedisChatMemoryRepository.RedisBuilder()
                .host(host)
                .port(port)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(RedisChatMemoryRepository redisChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory, DocumentRetriever documentRetriever) {
        return ChatClient
                .builder(chatModel)
                .defaultTools(appointmentTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new DocumentRetrievalAdvisor(documentRetriever)
                )
                .build();
    }

}
