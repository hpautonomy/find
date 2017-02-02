/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.AbstractDocumentsControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodDocumentsControllerTest extends AbstractDocumentsControllerTest<HodQueryRequest, HodSuggestRequest, HodGetContentRequest, ResourceName, HodQueryRestrictions, HodGetContentRequestIndex, HodSearchResult, HodErrorException> {
    @Mock
    private HodDocumentsService hodDocumentsService;

    @Mock
    private ObjectFactory<HodQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Mock
    private HodQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private ObjectFactory<HodQueryRequestBuilder> queryRequestBuilderFactory;

    @Mock
    private HodQueryRequestBuilder queryRequestBuilder;

    @Mock
    private ObjectFactory<HodSuggestRequestBuilder> suggestRequestBuilderFactory;

    @Mock
    private HodSuggestRequestBuilder suggestRequestBuilder;

    @Mock
    private ObjectFactory<HodGetContentRequestBuilder> getContentRequestBuilderFactory;

    @Mock
    private HodGetContentRequestBuilder getContentRequestBuilder;

    @Mock
    private ObjectFactory<HodGetContentRequestIndexBuilder> getContentRequestIndexBuilderFactory;

    @Mock
    private HodGetContentRequestIndexBuilder getContentRequestIndexBuilder;

    @Before
    public void setUp() {
        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.queryText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.fieldText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.maxDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minScore(anyInt())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateMatchIds(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateDontMatchIds(any())).thenReturn(queryRestrictionsBuilder);

        when(queryRequestBuilderFactory.getObject()).thenReturn(queryRequestBuilder);
        mockSearchRequestBuilder(queryRequestBuilder);
        when(queryRequestBuilder.autoCorrect(anyBoolean())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryType(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.summary(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.sort(any())).thenReturn(queryRequestBuilder);

        when(suggestRequestBuilderFactory.getObject()).thenReturn(suggestRequestBuilder);
        when(suggestRequestBuilder.reference(any())).thenReturn(suggestRequestBuilder);
        mockSearchRequestBuilder(suggestRequestBuilder);
        when(suggestRequestBuilder.summary(any())).thenReturn(suggestRequestBuilder);
        when(suggestRequestBuilder.sort(any())).thenReturn(suggestRequestBuilder);

        when(getContentRequestBuilderFactory.getObject()).thenReturn(getContentRequestBuilder);
        when(getContentRequestIndexBuilderFactory.getObject()).thenReturn(getContentRequestIndexBuilder);
        when(getContentRequestIndexBuilder.index(any())).thenReturn(getContentRequestIndexBuilder);
        when(getContentRequestIndexBuilder.reference(any())).thenReturn(getContentRequestIndexBuilder);
        when(getContentRequestBuilder.indexAndReferences(any())).thenReturn(getContentRequestBuilder);
        when(getContentRequestBuilder.print(any())).thenReturn(getContentRequestBuilder);

        documentsController = new HodDocumentsController(hodDocumentsService, queryRestrictionsBuilderFactory, queryRequestBuilderFactory, suggestRequestBuilderFactory, getContentRequestBuilderFactory, getContentRequestIndexBuilderFactory);
        documentsService = hodDocumentsService;
        databaseType = ResourceName.class;
    }

    @Override
    protected HodSearchResult sampleResult() {
        return HodSearchResult.builder().build();
    }

    @Override
    protected String getSort() {
        return "relevance";
    }

    @Test(expected = HodErrorException.class)
    public void getDocumentContentNotFound() throws HodErrorException {
        documentsController.getDocumentContent("Some Reference", null);
    }
}
