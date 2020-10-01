/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Test;

import java.util.Arrays;

public class RelatedUsersViewConfigTest {

    @Test
    public void testBasicValidate_defaults() throws ConfigException {
        final RelatedUsersViewConfig config = RelatedUsersViewConfig.builder().build();
        config.basicValidate("users");
    }

    @Test
    public void testBasicValidate_valid() throws ConfigException {
        final RelatedUsersViewConfig config = RelatedUsersViewConfig.builder()
            .agentStoreProfilesDatabase("db")
            .namedArea("db")
            .userDetailsFields(Arrays.asList(
                UserDetailsFieldConfig.builder().name("a").build(),
                UserDetailsFieldConfig.builder().name("b").build()
            ))
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_dbMissing() throws ConfigException {
        final RelatedUsersViewConfig config = RelatedUsersViewConfig.builder()
            .agentStoreProfilesDatabase(null)
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_namedAreaMissing() throws ConfigException {
        final RelatedUsersViewConfig config = RelatedUsersViewConfig.builder()
            .namedArea(null)
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_fieldMissing() throws ConfigException {
        final RelatedUsersViewConfig config = RelatedUsersViewConfig.builder()
            .userDetailsFields(Arrays.asList(
                UserDetailsFieldConfig.builder().name("a").build(),
                null
            ))
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_fieldInvalid() throws ConfigException {
        final RelatedUsersViewConfig config = RelatedUsersViewConfig.builder()
            .userDetailsFields(Arrays.asList(
                UserDetailsFieldConfig.builder().name("a").build(),
                UserDetailsFieldConfig.builder().build()
            ))
            .build();
        config.basicValidate("users");
    }

}
