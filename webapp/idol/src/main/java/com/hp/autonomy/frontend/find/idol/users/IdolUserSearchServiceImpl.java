/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.searchcomponents.idol.search.QueryResponseParser;
import com.hp.autonomy.types.idol.responses.Hit;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.idol.responses.UserDetails;
import com.hp.autonomy.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class IdolUserSearchServiceImpl implements IdolUserSearchService {

    private final UserService userService;
    private final QueryResponseParser queryResponseParser;

    @Autowired
    public IdolUserSearchServiceImpl(
        final UserService userService,
        final QueryResponseParser queryResponseParser
    ) {
        this.userService = userService;
        this.queryResponseParser = queryResponseParser;
    }

    @Override
    public UserDetails searchUser(final String searchString, final int startUser, final int maxUsers) {
        return userService.searchUsers(searchString, startUser, maxUsers);
    }

    @Override
    public User getUserFromUid(final Long uid) {
        return userService.getUserDetails(uid);
    }

    @Override
    public User getUserFromUsername(final String username) {
        return userService.getUserDetails(username);
    }

    @Override
    public List<User> getRelatedToSearch(
        final String agentStoreProfilesDatabase,
        final String namedArea,
        final String searchText,
        final int maxUsers
    ) {
        final Set<String> usernames = new HashSet<>();
        // preserve order, since it gives relevance
        final List<String> orderedUsernames = new ArrayList<>();
        final int pageSize = maxUsers;
        int pageStart = 1;
        int maxResults = pageSize;

        // we can get multiple profiles for the same user, so to fill up to maxUsers, we might need
        // to check multiple pages of results
        while (usernames.size() < maxUsers) {
            final QueryResponseData responseData = userService.getRelatedToSearch(
                agentStoreProfilesDatabase, namedArea, searchText, pageStart, maxResults);
            final List<IdolSearchResult> results =
                queryResponseParser.parseQueryHits(responseData.getHits());

            for (final IdolSearchResult result : results) {
                final FieldInfo<String> usernameField =
                    (FieldInfo<String>) result.getFieldMap().get("USERNAME");
                final FieldInfo<String> nameField =
                    (FieldInfo<String>) result.getFieldMap().get("NAME");
                final FieldInfo<String> field = usernameField != null ? usernameField : nameField;
                if (field != null && field.getValues().size() > 0) {
                    final String username = field.getValues().get(0).getValue();
                    if (usernames.size() < maxUsers && usernames.add(username)) {
                        orderedUsernames.add(username);
                    }
                }
            }

            if (results.size() < pageSize) {
                break;
            }
            pageStart += results.size();
            // maxResults is the total result set size up to this point, not the page size
            maxResults += results.size();
        }

        final List<User> users = userService.getUsersDetails(orderedUsernames);
        // getUsersDetails could reorder users
        final Map<String, User> usersByName = users.stream()
            .collect(Collectors.toMap(user -> user.getUsername(), user -> user));
        return orderedUsernames.stream().map(usersByName::get).collect(Collectors.toList());
    }

}
