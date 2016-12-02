/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.fields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.fields.FieldAndValueDetails;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequest;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsService;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
class IdolFieldsController extends FieldsController<IdolFieldsRequest, AciErrorException, IdolQueryRestrictions, IdolParametricRequest> {
    private final ObjectFactory<IdolFieldsRequestBuilder> fieldsRequestBuilderFactory;
    private final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    IdolFieldsController(
            final IdolFieldsService fieldsService,
            final IdolParametricValuesService parametricValuesService,
            final ObjectFactory<IdolParametricRequestBuilder> parametricRequestBuilderFactory,
            final ConfigService<? extends FindConfig> configService,
            final ObjectFactory<IdolFieldsRequestBuilder> fieldsRequestBuilderFactory, final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory) {
        super(fieldsService, parametricValuesService, parametricRequestBuilderFactory, configService);
        this.fieldsRequestBuilderFactory = fieldsRequestBuilderFactory;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
    }

    @RequestMapping(value = GET_PARAMETRIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<TagName> getParametricFields() throws AciErrorException {
        return getParametricFields(fieldsRequestBuilderFactory.getObject().build());
    }

    @RequestMapping(value = GET_PARAMETRIC_NUMERIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<FieldAndValueDetails> getParametricNumericFields() throws AciErrorException {
        return getParametricNumericFields(fieldsRequestBuilderFactory.getObject().build());
    }

    @RequestMapping(value = GET_PARAMETRIC_DATE_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<FieldAndValueDetails> getParametricDateFields() throws AciErrorException {
        return getParametricDateFields(fieldsRequestBuilderFactory.getObject().build());
    }

    @Override
    protected IdolQueryRestrictions createValueDetailsQueryRestrictions(final IdolFieldsRequest request) {
        return queryRestrictionsBuilderFactory.getObject()
                .queryText("*")
                .build();
    }
}
