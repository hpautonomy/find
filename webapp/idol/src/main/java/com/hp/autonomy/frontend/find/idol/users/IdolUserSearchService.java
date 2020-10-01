package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.idol.responses.UserDetails;

import java.util.List;

public interface IdolUserSearchService {
    UserDetails searchUser(String searchString, int startUser, int maxUsers);

    User getUserFromUid(Long uid);

    User getUserFromUsername(String username);

    /**
     * Get users with profiles similar to the search text.
     *
     * @param agentStoreProfilesDatabase database containing profiles in Community's AgentStore
     * @param namedArea named area containing profiles within the database
     * @param searchText
     * @param maxUsers maximum number of users to return
     * @return
     */
    List<User> getRelatedToSearch(
        String agentStoreProfilesDatabase, String namedArea, String searchText, int maxUsers);

}
