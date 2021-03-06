/*
 * Copyright (c) [2016-2018] [University of Minnesota]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.grouplens.samantha.server.tensorflow;

import com.fasterxml.jackson.databind.JsonNode;
import org.grouplens.samantha.modeler.tensorflow.TensorFlowModel;
import org.grouplens.samantha.server.common.JsonHelpers;
import org.grouplens.samantha.server.common.ModelService;
import org.grouplens.samantha.server.config.ConfigKey;
import org.grouplens.samantha.server.config.SamanthaConfigService;
import org.grouplens.samantha.server.indexer.Indexer;
import org.grouplens.samantha.server.indexer.IndexerConfig;
import org.grouplens.samantha.server.io.RequestContext;
import play.Configuration;
import play.inject.Injector;

public class TensorFlowBatchIndexerConfig implements IndexerConfig {
    private final Injector injector;
    private final Configuration config;
    private final Configuration daoConfigs;
    private final String daoConfigKey;
    private final String indexerName;
    private final String predictorName;
    private final String modelName;
    private final int batchSize;
    private final String batchSizeKey;
    private final String timestampField;

    private TensorFlowBatchIndexerConfig(Injector injector, Configuration config,
                                         Configuration daoConfigs, String daoConfigKey,
                                         String indexerName, String predictorName, String modelName,
                                         int batchSize, String batchSizeKey, String timestampField) {
        this.injector = injector;
        this.config = config;
        this.daoConfigKey = daoConfigKey;
        this.daoConfigs = daoConfigs;
        this.indexerName = indexerName;
        this.predictorName = predictorName;
        this.modelName = modelName;
        this.batchSize = batchSize;
        this.batchSizeKey = batchSizeKey;
        this.timestampField = timestampField;
    }

    public static IndexerConfig getIndexerConfig(Configuration indexerConfig,
                                                 Injector injector) {
        Integer batchSize = indexerConfig.getInt("batchSize");
        if (batchSize == null) {
            batchSize = 128;
        }
        String batchSizeKey = indexerConfig.getString("batchSizeKey");
        if (batchSizeKey == null) {
            batchSizeKey = "batchSize";
        }
        String timestampField = indexerConfig.getString("timestampField");
        if (timestampField == null) {
            timestampField = "timestamp";
        }
        String daoConfigKey = indexerConfig.getString("daoConfigKey");
        if (daoConfigKey == null) {
            daoConfigKey = "daoConfig";
        }
        return new TensorFlowBatchIndexerConfig(injector, indexerConfig,
                indexerConfig.getConfig(ConfigKey.ENTITY_DAOS_CONFIG.get()),
                daoConfigKey,
                indexerConfig.getString("dependedIndexer"),
                indexerConfig.getString("tensorFlowPredictor"),
                indexerConfig.getString("tensorFlowModel"),
                batchSize, batchSizeKey, timestampField);
    }

    public Indexer getIndexer(RequestContext requestContext) {
        SamanthaConfigService configService = injector.instanceOf(SamanthaConfigService.class);
        ModelService modelService = injector.instanceOf(ModelService.class);
        Indexer indexer = configService.getIndexer(indexerName, requestContext);
        configService.getPredictor(predictorName, requestContext);
        TensorFlowModel model = (TensorFlowModel) modelService.getModel(
                requestContext.getEngineName(), modelName);
        JsonNode reqBody = requestContext.getRequestBody();
        int batchSize = JsonHelpers.getOptionalInt(reqBody, batchSizeKey, this.batchSize);
        return new TensorFlowBatchIndexer(configService, config, injector,
                daoConfigs, daoConfigKey, indexer, model, batchSize, timestampField, requestContext);
    }
}
