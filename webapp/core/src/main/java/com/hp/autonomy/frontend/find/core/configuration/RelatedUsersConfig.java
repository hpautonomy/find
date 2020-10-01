/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.BooleanUtils;

@Getter
@Builder(toBuilder = true)
@JsonDeserialize(builder = RelatedUsersConfig.RelatedUsersConfigBuilder.class)
public class RelatedUsersConfig extends SimpleComponent<RelatedUsersConfig>
    implements OptionalConfigurationComponent<RelatedUsersConfig>
{
    private final Boolean enabled;
    private final RelatedUsersViewConfig relatedUsers;
    private final RelatedUsersViewConfig experts;

    @Override
    public void basicValidate(final String configSection) throws ConfigException {
        if (BooleanUtils.isTrue(enabled)) {
            if (relatedUsers == null) {
                throw new ConfigException(configSection, "relatedUsers must be provided");
            }
            relatedUsers.basicValidate(configSection + ".relatedUsers");
            if (experts == null) {
                throw new ConfigException(configSection, "experts must be provided");
            }
            experts.basicValidate(configSection + ".experts");
        }
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class RelatedUsersConfigBuilder {
        private RelatedUsersViewConfig relatedUsers = RelatedUsersViewConfig.builder().build();
        private RelatedUsersViewConfig experts = RelatedUsersViewConfig.builder()
            .namedArea("experts")
            .build();
    }

}
