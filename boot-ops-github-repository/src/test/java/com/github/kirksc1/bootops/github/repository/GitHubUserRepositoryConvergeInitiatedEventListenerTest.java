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

import com.github.kirksc1.bootops.converge.ItemConvergeInitiatedEvent;
import com.github.kirksc1.bootops.core.AttributeRetriever;
import com.github.kirksc1.bootops.core.Item;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

class GitHubUserRepositoryConvergeInitiatedEventListenerTest {

    private ItemConvergeInitiatedEvent event = mock(ItemConvergeInitiatedEvent.class);
    private Item item = mock(Item.class);
    private Map<String,Object> attributes = new HashMap<>();

    private GitHubUserRepositoryService service = mock(GitHubUserRepositoryService.class);
    private AttributeRetriever<GitHubUserRepository> retriever = mock(AttributeRetriever.class);

    private GitHubUserRepositoryConvergeInitiatedEventListener listener;

    @BeforeEach
    public void beforeEach() {
        reset(event, item);
        reset(service, retriever);
        attributes.clear();

        listener = new GitHubUserRepositoryConvergeInitiatedEventListener(service, retriever);

        when(event.getItem()).thenReturn(item);
        when(item.getAttributes()).thenReturn(attributes);
    }

    @Test
    public void testConstructor_whenRepositoryServiceNull_thenThrowIllegalArgumentException() {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new GitHubUserRepositoryConvergeInitiatedEventListener(null, new AttributeRetriever<GitHubUserRepository>("test", GitHubUserRepository.class));
        });

        Assertions.assertEquals("The GitHubUserRepositoryService provided was null", thrown.getMessage());
    }

    @Test
    public void testConstructor_whenAttributeRetrieverNull_thenThrowIllegalArgumentException() {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new GitHubUserRepositoryConvergeInitiatedEventListener(mock(GitHubUserRepositoryService.class), null);
        });

        Assertions.assertEquals("The AttributeRetriever provided was null", thrown.getMessage());
    }

    @Test
    public void testOnApplicationEvent_whenAttributeMissing_thenDoNotCallService() {
        listener.onApplicationEvent(event);

        verifyNoInteractions(service);
    }

    @Test
    public void testOnApplicationEvent_whenAttributePresent_thenCallService() {
        GitHubUserRepository repository = new GitHubUserRepository();
        when(retriever.retrieve(item)).thenReturn(Optional.of(repository));

        listener.onApplicationEvent(event);

        verify(service, times(1)).applyState(same(repository));
    }

}