/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.idol.responses.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(IdolUserController.BASE_PATH)
public class IdolUserController {

    static final String BASE_PATH = "/api/bi/user";
    static final String SEARCH_PATH = "/search";
    static final String RELATED_PATH = "/related-to-search";
    static final String PARAMETER_AGENT_STORE_PROFILES_DATABASE = "agentStoreProfilesDatabase";
    static final String PARAMETER_NAMED_AREA = "namedArea";
    static final String PARAMETER_SEARCH_TEXT = "searchText";
    static final String PARAMETER_START_USER = "startUser";
    static final String PARAMETER_MAX_USERS = "maxUsers";
    private final IdolUserSearchService idolUserSearchService;

    @Autowired
    public IdolUserController(final IdolUserSearchService idolUserSearchService) {
        this.idolUserSearchService = idolUserSearchService;
    }

    @RequestMapping(value = SEARCH_PATH, method= RequestMethod.GET)
    @ResponseBody
    public UserDetails searchUsers(@RequestParam(PARAMETER_SEARCH_TEXT) final String searchText, @RequestParam(PARAMETER_START_USER) final int startUser, @RequestParam(PARAMETER_MAX_USERS) final int maxUsers) {
        return idolUserSearchService.searchUser(searchText, startUser, maxUsers);
    }

    @RequestMapping(value = RELATED_PATH, method= RequestMethod.GET)
    @ResponseBody
    public List<User> getRelatedToSearch(
        @RequestParam(PARAMETER_AGENT_STORE_PROFILES_DATABASE)
            final String agentStoreProfilesDatabase,
        @RequestParam(PARAMETER_NAMED_AREA) final String namedArea,
        @RequestParam(PARAMETER_SEARCH_TEXT) final String searchText,
        @RequestParam(PARAMETER_MAX_USERS) final int maxUsers
    ) {
        return idolUserSearchService.getRelatedToSearch(
            agentStoreProfilesDatabase, namedArea, searchText, maxUsers);
    }

}
