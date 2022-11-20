/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kirksc1.bootops.github.repository;

import com.github.kirksc1.bootops.core.AttributeRetriever;
import com.github.kirksc1.bootops.core.AttributeType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * GitHubRepositoryAutoConfiguration is the Spring Boot Configuration class for the GitHub Repository functionality.
 */
@Configuration
public class GitHubRepositoryAutoConfiguration {

    /**
     * AttributeType bean mapping to the GitHubUserRepository attribute.
     */
    @Bean
    public AttributeType gitHubUserRepositoryAttributeType() {
        return new AttributeType(GitHubUserRepository.ATTRIBUTE_NAME, GitHubUserRepository.class);
    }

    /**
     * AttributeRetriever for retrieving the GitHubUserRepository attribute from and Item.
     */
    @Bean
    public AttributeRetriever<GitHubUserRepository> gitHubUserRepositoryAttributeRetriever() {
        return new AttributeRetriever<>(GitHubUserRepository.ATTRIBUTE_NAME, GitHubUserRepository.class);
    }

    /**
     * Bean containing details regarding the GitHub Service.
     */
    @Bean
    public GitHubServiceProperties gitHubServiceProperties() {
        return new GitHubServiceProperties();
    }

    /**
     * A WebClient configured for use with GitHub Service.  Includes configuration of ACCEPT and AUTHORIZATION headers.
     * @param properties GitHub Service properties.
     * @param environment The Spring environment for the application.
     */
    @Bean
    public WebClient gitHubWebClient(GitHubServiceProperties properties, Environment environment) {
        WebClient webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + environment.getProperty(properties.getTokenEnvironmentVariableName()))
                .build();

        return webClient;
    }

    /**
     * Application Service for interacting with GitHub user repositories.
     * @param webClient The GitHub WebClient.
     */
    @Bean
    public GitHubUserRepositoryService gitHubUserRepositoryService(@Qualifier("gitHubWebClient") WebClient webClient) {
        return new GitHubUserRepositoryService(webClient);
    }
}
