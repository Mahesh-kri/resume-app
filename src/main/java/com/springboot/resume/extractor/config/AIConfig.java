package com.springboot.resume.extractor.config;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String chatModel;

    @Value("${spring.ai.openai.chat.options.temperature}")
    private float temperature;

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(openAiBaseUrl, openAiApiKey); // Use Groq endpoint here
    }

    @Bean
    public OpenAiChatClient openAiChatClient(OpenAiApi openAiApi) {
        return new OpenAiChatClient(openAiApi, OpenAiChatOptions.builder()
            .withModel(chatModel) // Updated model configuration
            .withTemperature(temperature) // Updated temperature
            .build());
    }
}