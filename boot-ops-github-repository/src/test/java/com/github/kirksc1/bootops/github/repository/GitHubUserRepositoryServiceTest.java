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
import com.github.kirksc1.bootops.core.BootOpsException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(properties = { "github.service.base-url=http://localhost:8089/" })
@ContextConfiguration(initializers = {GitHubUserRepositoryServiceTest.TestApplicationContextInitializer.class})
@AutoConfigureWireMock(port = 8089)
class GitHubUserRepositoryServiceTest {

    @Autowired
    GitHubUserRepositoryService service;

    @AfterAll
    static void afterAll() {
        System.clearProperty("GITHUB_TOKEN");
    }

    @Test
    public void testConstructor_whenWebClientNull_thenThrowIllegalArgumentException() {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new GitHubUserRepositoryService(null);
        });

        Assertions.assertEquals("The WebClient provided was null", thrown.getMessage());
    }

    @Test
    public void testApplyState_whenRepositoryNotFound_thenCreateRepository() {
        String user = "myuser";
        String name = "myrepo";

        WireMock
                .stubFor(get("/repos/myuser/myrepo")
                        .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                        .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                        .willReturn(notFound()));
        WireMock.stubFor(post("/user/repos")
                    .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                    .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                    .withRequestBody(equalToJson("{\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}", true,true))
                    .willReturn(
                            created()
                                    .withHeader("Content-Type", "application/vnd.github+json")
                                    .withResponseBody(
                                            Body.fromJsonBytes("{\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}".getBytes()))));

        GitHubUserRepository repository = new GitHubUserRepository();
        repository.setUser(user);
        repository.setName(name);
        repository.setDescription("my new repo");
        repository.setPrivate(false);

        service.applyState(repository);

        WireMock.verify(WireMock.getRequestedFor(
                urlEqualTo("/repos/myuser/myrepo"))
                    .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                    .withHeader("AUTHORIZATION", equalTo("Bearer test-token")));
        WireMock.verify(WireMock.postRequestedFor(
                urlEqualTo("/user/repos"))
                    .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                    .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                    .withRequestBody(equalToJson("{\"user\":\"myuser\",\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}", true,true)));
    }

    @Test
    public void testApplyState_whenRepositoryFound_thenUpdateRepository() {
        String user = "myuser";
        String name = "myrepo";

        WireMock
                .stubFor(get("/repos/myuser/myrepo")
                        .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                        .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                        .willReturn(ok()
                                .withHeader("Content-Type", "application/vnd.github+json")
                                .withResponseBody(
                                        Body.fromJsonBytes("{\"user\":\"myuser\",\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}".getBytes()))));
        WireMock.stubFor(patch(urlEqualTo("/repos/myuser/myrepo"))
                .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                .withRequestBody(equalToJson("{\"user\":\"myuser\",\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}", true,true))
                .willReturn(
                        created()
                                .withHeader("Content-Type", "application/vnd.github+json")
                                .withResponseBody(
                                        Body.fromJsonBytes("{\"user\":\"myuser\",\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}".getBytes()))));

        GitHubUserRepository repository = new GitHubUserRepository();
        repository.setUser(user);
        repository.setName(name);
        repository.setDescription("my new repo");
        repository.setPrivate(false);

        service.applyState(repository);

        WireMock.verify(WireMock.getRequestedFor(
                        urlEqualTo("/repos/myuser/myrepo"))
                .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                .withHeader("AUTHORIZATION", equalTo("Bearer test-token")));
        WireMock.verify(WireMock.patchRequestedFor(
                        urlEqualTo("/repos/myuser/myrepo"))
                .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                .withRequestBody(equalToJson("{\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}", true,true)));
    }

    @Test
    public void testApplyState_whenTokenInvalidOnGet_thenThrowBootOpsException() {
        String user = "myuser";
        String name = "myrepo";

        WireMock
                .stubFor(get("/repos/myuser/myrepo")
                        .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                        .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                        .willReturn(unauthorized()));

        GitHubUserRepository repository = new GitHubUserRepository();
        repository.setUser(user);
        repository.setName(name);
        repository.setDescription("my new repo");
        repository.setPrivate(false);

        BootOpsException thrown = Assertions.assertThrows(BootOpsException.class, () -> {
            service.applyState(repository);
        });

        Assertions.assertEquals("Unable to retrieve the GitHub repository /repos/myuser/myrepo", thrown.getMessage());
    }

    @Test
    public void testApplyState_whenTokenInvalidOnCreate_thenThrowBootOpsException() {
        String user = "myuser";
        String name = "myrepo";

        WireMock
                .stubFor(get("/repos/myuser/myrepo")
                        .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                        .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                        .willReturn(notFound()));

        WireMock.stubFor(post("/user/repos")
                .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                .withRequestBody(equalToJson("{\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}", true,true))
                .willReturn(unauthorized()));

        GitHubUserRepository repository = new GitHubUserRepository();
        repository.setUser(user);
        repository.setName(name);
        repository.setDescription("my new repo");
        repository.setPrivate(false);

        BootOpsException thrown = Assertions.assertThrows(BootOpsException.class, () -> {
            service.applyState(repository);
        });

        Assertions.assertEquals("Unable to create the GitHub repository", thrown.getMessage());
    }

    //TODO add failure on patch
    @Test
    public void testApplyState_whenTokenInvalidOnUpdate_thenThrowBootOpsException() {
        String user = "myuser";
        String name = "myrepo";

        WireMock
                .stubFor(get("/repos/myuser/myrepo")
                        .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                        .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                        .willReturn(ok()
                                .withHeader("Content-Type", "application/vnd.github+json")
                                .withResponseBody(
                                        Body.fromJsonBytes("{\"user\":\"myuser\",\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}".getBytes()))));
        WireMock.stubFor(patch(urlEqualTo("/repos/myuser/myrepo"))
                .withHeader("ACCEPT", equalTo("application/vnd.github+json"))
                .withHeader("AUTHORIZATION", equalTo("Bearer test-token"))
                .withRequestBody(equalToJson("{\"user\":\"myuser\",\"name\":\"myrepo\",\"description\":\"my new repo\",\"private\":false}", true,true))
                .willReturn(unauthorized()));

        GitHubUserRepository repository = new GitHubUserRepository();
        repository.setUser(user);
        repository.setName(name);
        repository.setDescription("my new repo");
        repository.setPrivate(false);

        BootOpsException thrown = Assertions.assertThrows(BootOpsException.class, () -> {
            service.applyState(repository);
        });

        Assertions.assertEquals("Unable to update the GitHub repository /repos/myuser/myrepo", thrown.getMessage());
    }

    @SpringBootApplication
    static class TestApplication {

    }

    static class TestApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
    {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext)
        {
            System.setProperty("GITHUB_TOKEN", "test-token");
        }
    }
}