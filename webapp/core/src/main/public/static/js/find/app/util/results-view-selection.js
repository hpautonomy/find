define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'text!find/templates/app/util/selector.html'
], function (Backbone, _, $, i18n, selectorTemplate) {

    return Backbone.View.extend({
        selectorTemplate: _.template(selectorTemplate, {variable: 'data'}),

        events: {
            'shown.bs.tab [data-tab-id]': function(event) {
                var selectedTab = $(event.target).attr('data-tab-id');
                this.model.set('selectedTab', selectedTab);
            }
        },

        initialize: function(options) {
            this.views = options.views;
            this.model = options.model;
        },

        render: function() {
            this.$el.html('<ul class="nav nav-tabs minimal-tab selector-list" role="tablist"></ul>');

            const $selectorList = this.$('.selector-list');
            const selectedTab = this.model.get('selectedTab');

            _.each(this.views, function(viewData) {
                $(this.selectorTemplate({
                    i18n: i18n,
                    id: viewData.id,
                    uniqueId: viewData.uniqueId,
                    selector: viewData.selector
                })).toggleClass('active', viewData.id === selectedTab)
                    .appendTo($selectorList);
            }, this);
        },

        switchTab: function(tab) {
            const $tab = this.$('[data-tab-id = "' + tab + '"]');
            if ($tab) {
                $tab.tab('show');
            }
        }
    });

});
