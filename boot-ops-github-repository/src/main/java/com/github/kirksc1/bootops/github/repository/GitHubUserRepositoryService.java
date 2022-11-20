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

import com.github.kirksc1.bootops.core.BootOpsException;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * GitHubUserRepositoryService is an application service that provides the ability converge a
 * GitHubUserRepository to the GitHub service.
 */
public class GitHubUserRepositoryService {

    private final WebClient webClient;

    /**
     * Construct a new instance with the provided WebClient.
     * @param webClient The WebClient to use when interacting with the GitHub service.
     */
    public GitHubUserRepositoryService(WebClient webClient) {
        Assert.notNull(webClient, "The WebClient provided was null");

        this.webClient = webClient;
    }

    /**
     * Apply the configuration provided to the GitHub service.
     * @param repository The user repository details to apply.
     */
    public void applyState(GitHubUserRepository repository) {
        Optional<GitHubUserRepository> serverRepoOpt = getServerRepository(repository.getOwner(), repository.getName());
        if (serverRepoOpt.isEmpty()) {
            createUserRepository(repository);
        } else {
            updateRepository(repository);
        }
    }

    /**
     * Retrieve the GitHub service details for the specified repository.
     * @param owner The repository owner.
     * @param name The name of the repository to retrieve.
     * @return Optionally, A GitHubUserRepository instance containing details from the GitHub service if found, otherwise empty.
     */
    private Optional<GitHubUserRepository> getServerRepository(String owner, String name) {
        Optional <GitHubUserRepository> retVal = Optional.empty();
        try {
            retVal = webClient.get()
                    .uri("/repos/" + owner + "/" + name)
                    .retrieve()
                    .bodyToMono(GitHubUserRepository.class)
                    .blockOptional();
        } catch (WebClientResponseException e) {
            if (!e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new BootOpsException("Unable to retrieve the GitHub repository /repos/" + owner + "/" + name, e);
            }
        }
        return retVal;
    }

    /**
     * Create a new user repository in the GitHub service with the provided details.
     * @param repository The details of the user repository to create.
     * @return Optionally, The service view of the created repository if repository was created, otherwise empty.
     */
    private Optional<GitHubUserRepository> createUserRepository(GitHubUserRepository repository) {
        try {
        return webClient.post()
                .uri("/user/repos")
                .body(Mono.just(repository), GitHubUserRepository.class)
                .retrieve()
                .bodyToMono(GitHubUserRepository.class)
                .blockOptional();
        } catch (WebClientResponseException e) {
            throw new BootOpsException("Unable to create the GitHub repository", e);
        }
    }

    /**
     * Update the details for the provided user repository in the GitHub service.
     * @param repository The details of the user repository to create.
     * @return Optionally, The service view of the created repository if repository was created, otherwise empty.
     */
    private Optional<GitHubUserRepository> updateRepository(GitHubUserRepository repository) {
        try {
        return webClient.patch()
                .uri("/repos/" + repository.getOwner() + "/" + repository.getName())
                .body(Mono.just(repository), GitHubUserRepository.class)
                .retrieve()
                .bodyToMono(GitHubUserRepository.class)
                .blockOptional();
        } catch (WebClientResponseException e) {
            throw new BootOpsException("Unable to update the GitHub repository /repos/" + repository.getOwner() + "/" + repository.getName(), e);
        }
    }
}
