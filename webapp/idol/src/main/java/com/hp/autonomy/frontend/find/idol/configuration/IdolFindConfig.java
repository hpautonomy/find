/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.*;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.core.configuration.*;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.idol.answer.configuration.AnswerServerConfig;
import com.hp.autonomy.searchcomponents.idol.configuration.IdolSearchCapable;
import com.hp.autonomy.searchcomponents.idol.configuration.QueryManipulation;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import com.hp.autonomy.user.UserServiceConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@SuppressWarnings({"InstanceVariableOfConcreteClass", "DefaultAnnotationParam"})
@Data
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = IdolFindConfig.Builder.class)
public class IdolFindConfig extends AbstractConfig<IdolFindConfig> implements UserServiceConfig, AuthenticationConfig<IdolFindConfig>, IdolSearchCapable, FindConfig {
    private static final String SECTION = "Find Config Root";

    private final CommunityAuthentication login;
    private final ServerConfig content;
    private final QueryManipulation queryManipulation;
    private final ViewConfig view;
    private final AnswerServerConfig answerServer;
    @JsonProperty("savedSearches")
    private final SavedSearchConfig savedSearchConfig;
    private final MMAP mmap;
    private final UiCustomization uiCustomization;
    private final FieldsInfo fieldsInfo;
    private final MapConfiguration map;
    private final Integer minScore;
    private final StatsServerConfig statsServer;
    private final Integer topicMapMaxResults;
    private final Set<ParametricDisplayValues> parametricDisplayValues;

    public IdolFindConfig(final Builder builder) {
        login = builder.login;
        content = builder.content;
        queryManipulation = builder.queryManipulation;
        view = builder.view;
        answerServer = builder.answerServer;
        savedSearchConfig = builder.savedSearchConfig;
        mmap = builder.mmap;
        uiCustomization = builder.uiCustomization;
        fieldsInfo = builder.fieldsInfo;
        map = builder.map;
        minScore = builder.minScore;
        statsServer = builder.statsServer;
        topicMapMaxResults = builder.topicMapMaxResults;
        parametricDisplayValues = builder.parametricDisplayValues;
    }

    @Override
    public IdolFindConfig merge(final IdolFindConfig other) {
        if (other == null) {
            return this;
        }

        return new IdolFindConfig.Builder()
                .setContent(content == null ? other.content : content.merge(other.content))
                .setLogin(login == null ? other.login : login.merge(other.login))
                .setQueryManipulation(queryManipulation == null ? other.queryManipulation : queryManipulation.merge(other.queryManipulation))
                .setView(view == null ? other.view : view.merge(other.view))
                .setAnswerServer(answerServer == null ? other.answerServer : answerServer.merge(other.answerServer))
                .setSavedSearchConfig(savedSearchConfig == null ? other.savedSearchConfig : savedSearchConfig.merge(other.savedSearchConfig))
                .setMmap(mmap == null ? other.mmap : mmap.merge(other.mmap))
                .setUiCustomization(uiCustomization == null ? other.uiCustomization : uiCustomization.merge(other.uiCustomization))
                .setFieldsInfo(fieldsInfo == null ? other.fieldsInfo : fieldsInfo.merge(other.fieldsInfo))
                .setMap(map == null ? other.map : map.merge(other.map))
                .setMinScore(minScore == null ? other.minScore : minScore)
                .setStatsServer(statsServer == null ? other.statsServer : statsServer.merge(other.statsServer))
                .setParametricDisplayValues(parametricDisplayValues == null ? other.parametricDisplayValues : parametricDisplayValues)
                .setTopicMapMaxResults(topicMapMaxResults == null ? other.topicMapMaxResults : topicMapMaxResults)
                .build();
    }

    @JsonIgnore
    @Override
    public AciServerDetails getCommunityDetails() {
        return login.getCommunity().toAciServerDetails();
    }

    @JsonIgnore
    @Override
    public Authentication<?> getAuthentication() {
        return login;
    }

    @Override
    public IdolFindConfig withoutDefaultLogin() {
        return new Builder(this)
                .setLogin(login.withoutDefaultLogin())
                .build();
    }

    @Override
    public IdolFindConfig generateDefaultLogin() {
        return new Builder(this)
                .setLogin(login.generateDefaultLogin())
                .build();
    }

    @Override
    public IdolFindConfig withHashedPasswords() {
        // no work to do yet
        return this;
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        login.basicValidate(SECTION);
        content.basicValidate("content");
        savedSearchConfig.basicValidate(SECTION);

        if (map != null) {
            map.basicValidate("map");
        }

        if (queryManipulation != null) {
            queryManipulation.basicValidate(SECTION);
        }

        if (answerServer != null) {
            answerServer.basicValidate("AnswerServer");
        }
    }

    @JsonIgnore
    @Override
    public AciServerDetails getContentAciServerDetails() {
        return content.toAciServerDetails();
    }

    @Override
    @JsonIgnore
    public ViewConfig getViewConfig() {
        return view;
    }

    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private CommunityAuthentication login;
        private ServerConfig content;
        private QueryManipulation queryManipulation;
        private ViewConfig view;
        private AnswerServerConfig answerServer;
        @JsonProperty("savedSearches")
        private SavedSearchConfig savedSearchConfig;
        private MMAP mmap;
        private UiCustomization uiCustomization;
        private FieldsInfo fieldsInfo;
        private MapConfiguration map;
        private Integer minScore;
        private StatsServerConfig statsServer;
        private Set<ParametricDisplayValues> parametricDisplayValues;
        private Integer topicMapMaxResults;

        public Builder(final IdolFindConfig config) {
            login = config.login;
            content = config.content;
            queryManipulation = config.queryManipulation;
            view = config.view;
            answerServer = config.answerServer;
            savedSearchConfig = config.savedSearchConfig;
            mmap = config.mmap;
            uiCustomization = config.uiCustomization;
            fieldsInfo = config.fieldsInfo;
            map = config.map;
            minScore = config.minScore;
            statsServer = config.statsServer;
            topicMapMaxResults = config.topicMapMaxResults;
            parametricDisplayValues = config.parametricDisplayValues;
        }

        public IdolFindConfig build() {
            return new IdolFindConfig(this);
        }
    }
}
