package com.hp.autonomy.frontend.find.idol.indexes;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.indexes.IndexesService;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
import com.hp.autonomy.types.idol.Database;
import com.hp.autonomy.types.idol.GetStatusResponseData;
import com.hp.autonomy.types.requests.idol.actions.status.StatusActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IdolIndexesService implements IndexesService<Database, AciErrorException> {
    private final AciService contentAciService;
    private final Processor<GetStatusResponseData> responseProcessor;

    @Autowired
    public IdolIndexesService(final AciService contentAciService, final AciResponseProcessorFactory aciResponseProcessorFactory) {
        this.contentAciService = contentAciService;

        responseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(GetStatusResponseData.class);
    }

    @Override
    public List<Database> listVisibleIndexes() throws AciErrorException {
        final GetStatusResponseData responseData = contentAciService.executeAction(new AciParameters(StatusActions.GetStatus.name()), responseProcessor);
        return responseData.getDatabases().getDatabase();
    }
}
