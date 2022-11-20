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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

class GitHubRepositoryAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GitHubRepositoryAutoConfiguration.class,EnablePropertiesConfiguration.class));

    @Test
    public void testConfiguration_whenConfigured_thenAllBeansAddedToContext() {
        this.contextRunner.run((context) -> {
            Assertions.assertThat(context).hasSingleBean(AttributeType.class);
            Assertions.assertThat(context).hasSingleBean(AttributeRetriever.class);
            Assertions.assertThat(context).hasSingleBean(GitHubServiceProperties.class);
            Assertions.assertThat(context).hasSingleBean(WebClient.class);
            Assertions.assertThat(context).hasSingleBean(GitHubUserRepositoryService.class);
        });
    }

    @Test
    public void testConfiguration_whenGitHubPropertiesProvided_thenPropertiesOverridden() {
        this.contextRunner
                .withPropertyValues(
                        "github.service.base-url=baseURL",
                        "github.service.accept=ACCEPT",
                        "github.service.token-environment-variable-name=envNAME")
                .run((context) -> {
                    GitHubServiceProperties properties = context.getBean(GitHubServiceProperties.class);

                    Assertions.assertThat(properties.getBaseUrl()).isEqualTo("baseURL");
                    Assertions.assertThat(properties.getAccept()).isEqualTo("ACCEPT");
                    Assertions.assertThat(properties.getTokenEnvironmentVariableName()).isEqualTo("envNAME");
        });
    }

    @EnableConfigurationProperties
    static class EnablePropertiesConfiguration {
    }
}
