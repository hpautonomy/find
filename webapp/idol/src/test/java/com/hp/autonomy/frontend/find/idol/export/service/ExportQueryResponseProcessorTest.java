/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export.service;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportStrategy;
import com.hp.autonomy.frontend.find.core.export.service.MetadataNode;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.core.test.CoreTestContext;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.hp.autonomy.searchcomponents.core.test.CoreTestContext.CORE_CLASSES_PROPERTY;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreTestContext.class, properties = CORE_CLASSES_PROPERTY, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ExportQueryResponseProcessorTest {
    @Autowired
    private FieldPathNormaliser fieldPathNormaliser;
    @Mock
    private PlatformDataExportStrategy exportStrategy;

    private ByteArrayOutputStream outputStream;
    private ExportQueryResponseProcessor processor;

    @Before
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        processor = new ExportQueryResponseProcessor(exportStrategy, outputStream, Collections.emptyList());
        final List<String> fieldNames = Arrays.asList("Reference", "Database", "Summary", "Date", "categories");
        when(exportStrategy.getFieldNames(any(MetadataNode[].class), eq(Collections.emptyList()))).thenReturn(fieldNames);
        when(exportStrategy.getFieldInfoForNode(anyString())).thenReturn(Optional.empty());
        when(exportStrategy.getFieldInfoForNode("CATEGORY")).thenReturn(Optional.of(FieldInfo.<String>builder()
                .id("categories")
                .name(fieldPathNormaliser.normaliseFieldPath("CATEGORY"))
                .build()));

    }

    @Test
    public void export() throws IOException {
        processor.process(new MockAciResponseInputStream(IdolPlatformDataExportServiceTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/idol/export/query-response.xml")));
        verify(exportStrategy, times(6)).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    @Test
    public void exportEmptyResultSetWithoutHeader() throws IOException {
        processor.process(new MockAciResponseInputStream(IOUtils.toInputStream("<?xml version='1.0' encoding='UTF-8' ?>\n<autnresponse><response/></autnresponse>")));
        verify(exportStrategy, never()).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    @Test(expected = AciErrorException.class)
    public void errorResponse() {
        processor.process(new MockAciResponseInputStream(IdolPlatformDataExportServiceTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/idol/export/error-response.xml")));
    }

    @Test(expected = ProcessorException.class)
    public void unexpectedError() {
        processor.process(new MockAciResponseInputStream(IOUtils.toInputStream("")));
    }

    private static class MockAciResponseInputStream extends AciResponseInputStream {
        private MockAciResponseInputStream(final InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public int getStatusCode() {
            return 0;
        }

        @Override
        public String getHeader(final String name) {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public long getContentLength() {
            return 0;
        }

        @Override
        public String getContentType() {
            return "text/xml";
        }
    }
}