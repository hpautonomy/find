/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/util/popover',
    './edit-concept-view',
    'text!find/templates/app/page/search/selected-concepts/selected-concept.html',
    'jquery',
    'bootstrap'
], function(Backbone, _, popover, EditConceptView, template, $) {

    /**
     * Attributes of a concept group model.
     * @typedef {Object} ConceptGroupModelAttributes
     * @property {string[]} concepts
     */
    return Backbone.View.extend({
        template: _.template(template),
        className: 'selected-concept-container',

        render: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');

            this.$content = $('<div class="inline-block"></div>');

            this.$el.empty()
                .append(this.$content);

            this.updateConcepts();
            this.createPopover();
        },

        remove: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');
            this.$('.popover').popover('destroy');
            Backbone.View.prototype.remove.call(this);
        },

        // Called from outside whenever the model's concepts are changed
        updateConcepts: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');

            const concepts = this.model.get('concepts');

            this.$content.html(this.template({clusterCid: this.model.cid, concepts: concepts}));

            this.$('[data-toggle="tooltip"]').tooltip({
                container: 'body',
                placement: 'top'
            });
        },

        createPopover: function () {
            var $popover;
            var $popoverControl = this.$content;

            var clickHandler = _.bind(function (e) {
                var $target = $(e.target);
                var notPopover = !$target.is($popover) && !$.contains($popover[0], $target[0]);
                var notPopoverControl = !$target.is($popoverControl) && !$.contains($popoverControl[0], $target[0]);

                if (notPopover && notPopoverControl) {
                    this.$content.popover('hide');
                }
            }, this);

            popover($popoverControl, 'click', _.bind(function (content) {
                content.html('<div class="edit-concept-container"></div>');
                this.renderEditConcept();
                $popover = content.closest('.popover');
                $(document.body).on('click', clickHandler);
            }, this), _.bind(function () {
                $(document.body).off('click', clickHandler);
            }, this));
        },

        renderEditConcept: function() {
            this.editConceptView = new EditConceptView({
                model: this.model
            });

            this.$('.edit-concept-container').append(this.editConceptView.$el);
            this.editConceptView.render();

            this.listenTo(this.editConceptView, 'remove', function () {
                this.$content.popover('hide');
            });
        }
    });

});