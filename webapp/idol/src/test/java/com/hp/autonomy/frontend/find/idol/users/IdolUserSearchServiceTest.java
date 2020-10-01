/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldValue;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.searchcomponents.idol.search.QueryResponseParser;
import com.hp.autonomy.types.idol.responses.DocContent;
import com.hp.autonomy.types.idol.responses.Hit;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class IdolUserSearchServiceTest {
    @Mock private UserService userService;
    @Mock private QueryResponseParser queryResponseParser;
    private IdolUserSearchService service;

    /**
     * @param name User field name
     * @param value User field value
     * @return AgentStore user profile document hit
     */
    private Hit makeUserProfile(final String name, final String value) {
        final FieldInfo<String> fieldValue = FieldInfo.<String>builder()
            .value(new FieldValue<>(value, value))
            .build();
        final Map<String, FieldInfo<String>> fields = Collections.singletonMap(name, fieldValue);
        final IdolSearchResult searchResult = IdolSearchResult.builder().fieldMap(fields).build();
        final DocContent content = Mockito.mock(DocContent.class);
        Mockito.when(content.getContent()).thenReturn(Collections.singletonList(searchResult));
        final Hit hit = new Hit();
        hit.setContent(content);
        return hit;
    }

    /**
     * Mock services so that user profiles are returned as given, and processed correctly.
     *
     * @param profileHits AgentStore user profile document hits, ordered, across all pages
     */
    private void mockProfiles(final List<Hit> profileHits) {
        Mockito.when(userService.getRelatedToSearch(any(), any(), any(), anyInt(), anyInt()))
            .thenAnswer(inv -> {
                final int start = Math.min((int) inv.getArguments()[3] - 1, profileHits.size());
                final int maxResults = Math.min((int) inv.getArguments()[4], profileHits.size());
                final QueryResponseData profiles = new QueryResponseData();
                profiles.getHits().addAll(profileHits.subList(start, maxResults));
                return profiles;
            });

        Mockito.when(queryResponseParser.parseQueryHits(any())).thenAnswer(inv -> {
            final List<Hit> hits = (List<Hit>) inv.getArguments()[0];
            return hits.stream()
                .map(hit -> (IdolSearchResult) hit.getContent().getContent().get(0))
                .collect(Collectors.toList());
        });

        Mockito.when(userService.getUsersDetails(any())).thenAnswer(inv -> {
            return ((List<String>) inv.getArguments()[0]).stream()
                .map(username -> {
                    final User user = new User();
                    user.setUsername(username);
                    return user;
                })
                .collect(Collectors.toList());
        });
    }

    @Before
    public void setUp() {
        service = new IdolUserSearchServiceImpl(userService, queryResponseParser);
    }

    @Test
    public void testGetRelatedToSearch() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u3")
        ));

        final List<User> users = service.getRelatedToSearch("db", "area", "text", 30);
        Assert.assertEquals(3, users.size());
        Assert.assertEquals("u1", users.get(0).getUsername());
        Assert.assertEquals("u2", users.get(1).getUsername());
        Assert.assertEquals("u3", users.get(2).getUsername());

        Mockito.verify(userService)
            .getRelatedToSearch(eq("db"), eq("area"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_nameField() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("NAME", "u2")
        ));

        final List<User> users = service.getRelatedToSearch("db", "area", "text", 30);
        Assert.assertEquals(2, users.size());
        Assert.assertEquals("u1", users.get(0).getUsername());
        Assert.assertEquals("should extract the username correctly",
            "u2", users.get(1).getUsername());
    }

    @Test
    public void testGetRelatedToSearch_noField() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("OTHER", "value")
        ));

        final List<User> users = service.getRelatedToSearch("db", "area", "text", 30);
        Assert.assertEquals("should ignore the profile", 1, users.size());
        Assert.assertEquals("u1", users.get(0).getUsername());
    }

    @Test
    public void testGetRelatedToSearch_duplicateUsers() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u2")
        ));

        final List<User> users = service.getRelatedToSearch("db", "area", "text", 30);
        Assert.assertEquals("should merge duplicates", 2, users.size());
        Assert.assertEquals("u1", users.get(0).getUsername());
        Assert.assertEquals("u2", users.get(1).getUsername());
    }

    @Test
    public void testGetRelatedToSearch_multiplePages() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u3"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u2")
        ));

        final List<User> users = service.getRelatedToSearch("db", "area", "text", 4);
        Assert.assertEquals(3, users.size());
        Assert.assertEquals("u1", users.get(0).getUsername());
        Assert.assertEquals("u2", users.get(1).getUsername());
        Assert.assertEquals("u3", users.get(2).getUsername());

        Mockito.verify(userService, Mockito.times(2))
            .getRelatedToSearch(eq("db"), eq("area"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_multiplePages_fullLastPage() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u1")
        ));

        final List<User> users = service.getRelatedToSearch("db", "area", "text", 3);
        Assert.assertEquals(2, users.size());
        Assert.assertEquals("u1", users.get(0).getUsername());
        Assert.assertEquals("u2", users.get(1).getUsername());

        // should request a 3rd page since the 2nd was full
        Mockito.verify(userService, Mockito.times(3))
            .getRelatedToSearch(eq("db"), eq("area"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_enoughUsersBeforeLastPage() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u3"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u3")
        ));

        final List<User> users = service.getRelatedToSearch("db", "area", "text", 3);
        Assert.assertEquals(3, users.size());
        Assert.assertEquals("u1", users.get(0).getUsername());
        Assert.assertEquals("u2", users.get(1).getUsername());
        Assert.assertEquals("u3", users.get(2).getUsername());

        // shouldn't get the last page
        Mockito.verify(userService, Mockito.times(2))
            .getRelatedToSearch(eq("db"), eq("area"), eq("text"), anyInt(), anyInt());
    }

    @Test
    public void testGetRelatedToSearch_extraUsers() {
        mockProfiles(Arrays.asList(
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u1"),
            makeUserProfile("USERNAME", "u2"),
            makeUserProfile("USERNAME", "u3")
        ));

        final List<User> users = service.getRelatedToSearch("db", "area", "text", 2);
        Assert.assertEquals("should discard users beyond maxUsers", 2, users.size());
        Assert.assertEquals("u1", users.get(0).getUsername());
        Assert.assertEquals("u2", users.get(1).getUsername());

        // shouldn't get the last page
        Mockito.verify(userService, Mockito.times(2))
            .getRelatedToSearch(eq("db"), eq("area"), eq("text"), anyInt(), anyInt());
    }

}
