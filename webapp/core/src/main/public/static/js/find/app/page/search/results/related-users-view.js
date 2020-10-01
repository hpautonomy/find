/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * List users related to the current search.
 *
 * Options:
 *  - queryModel
 *  - previewModeModel
 *  - config: com.hp.autonomy.frontend.find.core.configuration.RelatedUsersViewConfig
 */

define([
    'underscore',
    'find/app/model/related-users-collection',
    'find/app/page/search/results/users-view'
], function(_, RelatedUsersCollection, UsersView) {
    'use strict';

    const MAX_USERS = 30;

    return UsersView.extend({
        initialize: function (options) {
            this.relatedUsersCollection = new RelatedUsersCollection();
            this.queryModel = options.queryModel;
            this.ruConfig = options.config;

            UsersView.prototype.initialize.call(this, _.defaults({
                usersCollection: this.relatedUsersCollection,
                config: {
                    userDetailsFields: this.ruConfig.userDetailsFields
                }
            }, options));

            this.previousQueryText = null;
            this.listenTo(this.queryModel, 'change', this.update);
        },

        render: function () {
            UsersView.prototype.render.call(this);
            this.update();
        },

        update: function () {
            const queryText = this.queryModel.get('queryText');
            if (!this.$el.is(':visible') || queryText === this.previousQueryText) {
                return;
            }

            if (queryText === '' || queryText === '*') {
                this.showNoQuery();
                return;
            }

            this.showLoading();
            this.relatedUsersCollection.fetch({ reset: true, data: {
                agentStoreProfilesDatabase: this.ruConfig.agentStoreProfilesDatabase,
                namedArea: this.ruConfig.namedArea,
                searchText: queryText,
                maxUsers: MAX_USERS
            } })
                .done(_.bind(function () {
                    this.previousQueryText = queryText;
                }, this))
                .fail(_.bind(function (xhr) {
                    this.showError(xhr, 'fetchUsers');
                }, this));
        },

    });

});
