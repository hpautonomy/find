/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './updating-widget',
    'find/app/configuration',
    'find/app/page/search/results/map-view',
    'find/app/model/documents-collection',
    'find/idol/app/model/idol-indexes-collection'
], function(UpdatingWidget, configuration, MapView, DocumentsCollection, IdolIndexesCollection) {
    'use strict';

    return UpdatingWidget.extend({

        viewType: 'map',

        clickable: true,

        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);
            this.markers = [];
            this.locationFieldPair = options.widgetSettings.locationFieldPair;
            this.maxResults = options.widgetSettings.maxResults || 1000;
            this.documentsCollection = new DocumentsCollection();
            this.clusterMarkers = options.widgetSettings.clusterMarkers || false;

            this.mapView = new MapView({
                addControl: false,
                centerCoordinates: options.widgetSettings.centerCoordinates,
                initialZoom: options.widgetSettings.zoomLevel,
                removeZoomControl: true,
                disableInteraction: true
            });

            this.listenTo(this.documentsCollection, 'add', function (model) {
                const locations = model.get('locations');
                const location = _.findWhere(locations, {displayName: this.locationFieldPair});
                if (location) {
                    const longitude = location.longitude;
                    const latitude = location.latitude;
                    const title = model.get('title');
                    const marker = this.mapView.getMarker(latitude, longitude, this.getIcon(), title);
                    this.markers.push(marker);
                }
            });

            this.listenTo(this.documentsCollection, 'sync', _.bind(function () {
                if (!_.isEmpty(this.markers)) {
                    this.mapView.clearMarkers(true);
                    this.mapView.addMarkers(this.markers, this.clusterMarkers);
                    if(this.updateCallback) {
                        this.updateCallback();
                        delete this.updateCallback;
                    }
                }
            }, this));
        },

        render: function() {
            UpdatingWidget.prototype.render.apply(this, arguments);
            this.mapView.setElement(this.$content).render();

            this.fetchPromise.done(function() {
                this.queryModel = this.savedSearchModel.toQueryModel(IdolIndexesCollection, false);
                this.getData();
            }.bind(this));
        },

        doUpdate: function(done) {
            if (this.queryModel) {
                this.getData();
                this.updateCallback = done;
            }
        },

        getIcon: function () {
            const locationField = _.findWhere(configuration().map.locationFields, {displayName: this.locationFieldPair});
            return this.mapView.getIcon(locationField.iconName, locationField.iconColor, locationField.markerColor);
        },

        getData: function() {
            this.markers = [];
            this.mapView.clearMarkers(this.clusterMarkers);
            const locationField = _.findWhere(configuration().map.locationFields, {displayName: this.locationFieldPair});

            const latitudeFieldsInfo = configuration().fieldsInfo[locationField.latitudeField];
            const longitudeFieldsInfo = configuration().fieldsInfo[locationField.longitudeField];

            const latitudesFieldsString = latitudeFieldsInfo.names.join(':');
            const longitudeFieldsString = longitudeFieldsInfo.names.join(':');

            const exists = 'EXISTS{}:' + latitudesFieldsString + ' AND EXISTS{}:' + longitudeFieldsString;

            const newFieldText = this.queryModel.get('fieldText') ? this.queryModel.get('fieldText') + ' AND ' + exists : exists;

            this.updatePromise = this.documentsCollection.fetch({
                data: {
                    text: this.queryModel.get('queryText'),
                    max_results: this.maxResults,
                    indexes: this.queryModel.get('indexes'),
                    field_text: newFieldText,
                    min_date: this.queryModel.get('minDate'),
                    max_date: this.queryModel.get('maxDate'),
                    sort: 'relevance',
                    summary: 'context',
                    queryType: 'MODIFIED'
                },
                reset: false
            }).done(function() {
                delete this.updatePromise;
            }.bind(this));
        },

        onCancelled: function() {
            if (this.updatePromise && this.updatePromise.abort) {
                this.updatePromise.abort();
            }
        },

        exportPPTData: function(){
            return this.mapView.exportPPTData().then(function(data){
                return {
                    data: data,
                    type: 'map'
                }
            });
        }
    });
});