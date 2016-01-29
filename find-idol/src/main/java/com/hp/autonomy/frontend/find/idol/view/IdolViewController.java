/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.view;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.view.ViewController;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.logging.Markers;
import com.hp.autonomy.searchcomponents.core.view.ViewServerService;
import com.hp.autonomy.searchcomponents.idol.view.ReferenceFieldBlankException;
import com.hp.autonomy.searchcomponents.idol.view.ViewDocumentNotFoundException;
import com.hp.autonomy.searchcomponents.idol.view.ViewNoReferenceFieldException;
import com.hp.autonomy.searchcomponents.idol.view.ViewServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(IdolViewController.VIEW_PATH)
@Slf4j
public class IdolViewController extends ViewController<ViewServerService<String, AciErrorException>, String, AciErrorException> {
    public static final String VIEW_PATH = "/api/public/view";
    public static final String VIEW_DOCUMENT_PATH = "/viewDocument";
    public static final String REFERENCE_PARAM = "reference";
    public static final String INDEX_PARAM = "index";

    private final ConfigService<IdolFindConfig> configService;
    private final ControllerUtils controllerUtils;

    @Autowired
    public IdolViewController(final ViewServerService<String, AciErrorException> viewServerService, final ConfigService<IdolFindConfig> configService, final ControllerUtils controllerUtils) {
        super(viewServerService);
        this.configService = configService;
        this.controllerUtils = controllerUtils;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleViewDocumentNotFoundException(
            final ViewDocumentNotFoundException e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        final String reference = e.getReference();

        log.info(Markers.AUDIT, "TRIED TO VIEW NON EXISTENT DOCUMENT WITH REFERENCE {}", reference);

        return controllerUtils.buildErrorModelAndView(request, "error.documentNotFound", "error.referenceDoesNotExist", new Object[]{reference}, HttpStatus.NOT_FOUND.value(), true);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleViewNoReferenceFieldException(
            final ViewNoReferenceFieldException e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        final String reference = e.getReference();
        final String referenceField = configService.getConfig().getViewConfig().getReferenceField();

        log.info(Markers.AUDIT, "TRIED TO VIEW DOCUMENT WITH REFERENCE {} BUT THE REFERENCE FIELD {} WAS MISSING", reference, referenceField);

        return controllerUtils.buildErrorModelAndView(request, "error.documentNoReferenceField", "error.documentNoReferenceFieldExtended", new Object[]{reference, referenceField}, HttpStatus.BAD_REQUEST.value(), true);
    }

    @ExceptionHandler(ReferenceFieldBlankException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleReferenceFieldBlankException(
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        log.info(Markers.AUDIT, "TRIED TO VIEW A DOCUMENT USING A BLANK REFERENCE FIELD");

        return controllerUtils.buildErrorModelAndView(request, "error.referenceFieldBlankMain", "error.referenceFieldBlankSub", null, HttpStatus.BAD_REQUEST.value(), true);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleViewServerErrorException(
            final ViewServerErrorException e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        final String reference = e.getReference();

        log.info(Markers.AUDIT, "TRIED TO VIEW DOCUMENT WITH REFERENCE {} BUT VIEW SERVER RETURNED AN ERROR PAGE", reference);

        return controllerUtils.buildErrorModelAndView(request, "error.viewServerErrorMain", "error.viewServerErrorSub", new Object[]{reference}, HttpStatus.INTERNAL_SERVER_ERROR.value(), true);
    }
}
