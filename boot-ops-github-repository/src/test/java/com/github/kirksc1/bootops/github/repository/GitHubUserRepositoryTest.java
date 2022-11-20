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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GitHubUserRepositoryTest {

    @Test
    public void testConstructor_whenCreated_thenAllNull() {
        GitHubUserRepository repository = new GitHubUserRepository();

        assertNull(repository.getUser());
        assertNull(repository.getOwner());
        assertNull(repository.getName());
        assertNull(repository.getDescription());
        assertNull(repository.isPrivate());
    }

    @Test
    public void testGetUser_whenValueSet_thenValueGettable() {
        GitHubUserRepository repository = new GitHubUserRepository();

        repository.setUser("myuser");
        assertEquals("myuser", repository.getUser());
    }

    @Test
    public void testGetName_whenValueSet_thenValueGettable() {
        GitHubUserRepository repository = new GitHubUserRepository();

        repository.setName("myname");
        assertEquals("myname", repository.getName());
    }

    @Test
    public void testGetOwner_whenUserSet_thenValueGettable() {
        GitHubUserRepository repository = new GitHubUserRepository();

        repository.setUser("myuser");
        assertEquals("myuser", repository.getOwner());
    }

    @Test
    public void testGetDescription_whenValueSet_thenValueGettable() {
        GitHubUserRepository repository = new GitHubUserRepository();

        repository.setDescription("mydescription");
        assertEquals("mydescription", repository.getDescription());
    }

    @Test
    public void testGetIsPrivate_whenValueSet_thenValueGettable() {
        GitHubUserRepository repository = new GitHubUserRepository();

        repository.setPrivate(TRUE);
        assertEquals(TRUE, repository.isPrivate());
    }

}