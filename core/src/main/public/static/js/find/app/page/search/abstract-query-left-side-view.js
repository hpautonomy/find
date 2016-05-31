/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/filters/parametric/numeric-parametric-view',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/util/text-input',
    'find/app/page/search/filters/precision-recall/precision-recall-slider-view',
    'find/app/util/collapsible',
    'find/app/vent',
    'parametric-refinement/display-collection',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes'
], function(Backbone, $, _, DateView, NumericParametricView, ParametricView, TextInput, PrecisionRecallView, Collapsible, vent, ParametricDisplayCollection, configuration, i18n, i18nIndexes) {
    "use strict";

    var datesTitle = i18n['search.dates'];

    function searchMatches(text, search) {
        return text.toLowerCase().indexOf(search.toLowerCase()) > -1;
    }

    return Backbone.View.extend({
        // Abstract
        IndexesView: null,

        initialize: function(options) {
            this.filterModel = new Backbone.Model();

            this.filterInput = new TextInput({
                model: this.filterModel,
                modelAttribute: 'text',
                templateOptions: {
                    placeholder: i18n['search.filters.filter']
                }
            });

            this.indexesEmpty = false;

            //noinspection JSUnresolvedFunction
            var indexesView = new this.IndexesView({
                queryModel: options.queryModel,
                indexesCollection: options.indexesCollection,
                selectedDatabasesCollection: options.queryState.selectedIndexes,
                filterModel: this.filterModel,
                visibleIndexesCallback: _.bind(function(indexes) {
                    this.indexesEmpty = indexes.length === 0;
                    this.updateIndexesVisibility();
                    this.updateEmptyMessage();
                }, this)
            });

            var dateView = new DateView({
                datesFilterModel: options.queryState.datesFilterModel,
                savedSearchModel: options.savedSearchModel
            });

            if (configuration().hasBiRole) {
                this.precisionSlider = new PrecisionRecallView({
                    queryModel: options.queryModel,
                    queryState: options.queryState
                });
            }

            this.numericParametricCollection = options.numericParametricCollection;

            this.parametricDisplayCollection = new ParametricDisplayCollection([], {
                parametricCollection: options.parametricCollection,
                selectedParametricValues: options.queryState.selectedParametricValues,
                filterModel: this.filterModel
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.parametricDisplayCollection, 'update reset', function() {
                this.updateParametricVisibility();
                this.updateEmptyMessage();
            });

            this.numericParametricView = new NumericParametricView({
                queryModel: options.queryModel,
                queryState: options.queryState,
                numericParametricCollection: this.numericParametricCollection
            });
            
            this.listenTo(vent, 'vent:resize', function () {
                this.numericParametricView.render();
            });

            this.parametricView = new ParametricView({
                queryModel: options.queryModel,
                queryState: options.queryState,
                filterModel: this.filterModel,
                indexesCollection: options.indexesCollection,
                parametricCollection: options.parametricCollection,
                displayCollection: this.parametricDisplayCollection
            });

            this.indexesViewWrapper = new Collapsible({
                view: indexesView,
                collapsed: false,
                title: i18nIndexes['search.indexes']
            });

            this.dateViewWrapper = new Collapsible({
                view: dateView,
                collapsed: false,
                title: datesTitle
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.filterModel, 'change', function() {
                this.updateDatesVisibility();
                this.updateParametricVisibility();
                this.updateEmptyMessage();
            });

            this.$emptyMessage = $('<p class="hide">' + i18n['search.filters.empty'] + '</p>');
        },

        render: function() {
            //noinspection JSUnresolvedVariable
            this.$el.empty()
                .append(this.filterInput.$el)
                .append(this.$emptyMessage)
                .append(this.indexesViewWrapper.$el)
                .append(this.dateViewWrapper.$el)
                .append(this.numericParametricView.$el)
                .append(this.parametricView.$el);

            this.filterInput.render();
            this.indexesViewWrapper.render();
            this.numericParametricView.render();
            this.parametricView.render();
            this.dateViewWrapper.render();

            if (this.precisionSlider) {
                this.$el.prepend(this.precisionSlider.$el);
                this.precisionSlider.render();
            }

            this.updateParametricVisibility();
            this.updateDatesVisibility();
            this.updateIndexesVisibility();
            this.updateEmptyMessage();

            return this;
        },

        remove: function() {
            //noinspection JSUnresolvedFunction
            _.invoke([
                this.numericParametricView,
                this.parametricView,
                this.indexesViewWrapper,
                this.dateViewWrapper
            ], 'remove');

            Backbone.View.prototype.remove.call(this);
        },

        updateEmptyMessage: function() {
            var noFiltersMatched = !(this.indexesEmpty && this.hideDates && this.parametricDisplayCollection.length === 0);

            this.$emptyMessage.toggleClass('hide', noFiltersMatched);
        },

        updateParametricVisibility: function() {
            this.numericParametricView.$el.toggleClass('hide', this.numericParametricCollection.length === 0 && Boolean(this.filterModel.get('text')));
            this.parametricView.$el.toggleClass('hide', this.parametricDisplayCollection.length === 0 && Boolean(this.filterModel.get('text')));
        },

        updateDatesVisibility: function() {
            var search = this.filterModel.get('text');
            this.hideDates = !(!search || searchMatches(datesTitle, search));

            this.dateViewWrapper.$el.toggleClass('hide', this.hideDates);
        },

        updateIndexesVisibility: function() {
            this.indexesViewWrapper.$el.toggleClass('hide', this.indexesEmpty);
        }
    });

});