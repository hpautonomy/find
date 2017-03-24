/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export.service;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.export.service.CsvExportStrategy;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportStrategy;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportService;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportServiceIT;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import com.hp.autonomy.searchcomponents.idol.configuration.AciServiceRetriever;
import com.hp.autonomy.searchcomponents.idol.configuration.IdolSearchCapable;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = {HavenSearchIdolConfiguration.class, IdolPlatformDataExportServiceIT.ExportConfiguration.class}, value = "export.it=true", webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdolPlatformDataExportServiceIT extends PlatformDataExportServiceIT<IdolQueryRequest, IdolQueryRestrictions, AciErrorException> {
    @Configuration
    @ConditionalOnProperty("export.it")
    public static class ExportConfiguration {
        @Bean
        public PlatformDataExportService<IdolQueryRequest, AciErrorException> exportService(
                final HavenSearchAciParameterHandler parameterHandler,
                final AciServiceRetriever aciServiceRetriever,
                final PlatformDataExportStrategy[] exportStrategies) {
            return new IdolPlatformDataExportService(parameterHandler, aciServiceRetriever, exportStrategies);
        }

        @Bean
        public PlatformDataExportStrategy csvExportStrategy(final ConfigService<IdolSearchCapable> configService, final FieldPathNormaliser fieldPathNormaliser) {
            return new CsvExportStrategy(configService, fieldPathNormaliser);
        }
    }
}