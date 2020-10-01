/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/model/find-base-collection'
], function (Backbone, FindBaseCollection) {
    'use strict';

    return FindBaseCollection.extend({
        url: 'api/bi/user/related-to-search',

        model: Backbone.Model.extend({
            idAttribute: 'uid'
        })

    });
});
