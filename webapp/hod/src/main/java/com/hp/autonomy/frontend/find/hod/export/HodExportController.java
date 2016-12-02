/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
class HodExportController extends ExportController<HodQueryRequest, HodErrorException> {

    @Autowired
    public HodExportController(final ExportService<HodQueryRequest, HodErrorException> exportService,
                               final RequestMapper<HodQueryRequest> requestMapper,
                               final ControllerUtils controllerUtils) {
        super(exportService, requestMapper, controllerUtils);
    }
}
