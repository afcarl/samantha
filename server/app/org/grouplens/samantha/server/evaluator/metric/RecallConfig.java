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

package org.grouplens.samantha.server.evaluator.metric;

import org.grouplens.samantha.modeler.metric.Metric;
import org.grouplens.samantha.modeler.metric.Recall;
import org.grouplens.samantha.server.io.RequestContext;
import play.Configuration;
import play.inject.Injector;

import java.util.List;

public class RecallConfig implements MetricConfig {
    private final List<Integer> N;
    private final List<String> itemKeys;
    private final List<String> recKeys;
    private final String relevanceKey;
    private final String separator;
    private final double threshold;
    private final double minValue;

    private RecallConfig(List<Integer> N, List<String> itemKeys, List<String> recKeys, String relevanceKey,
                         double threshold, double minValue, String separator) {
        this.N = N;
        this.itemKeys = itemKeys;
        this.recKeys = recKeys;
        this.relevanceKey = relevanceKey;
        this.threshold = threshold;
        this.minValue = minValue;
        this.separator = separator;
    }

    public static MetricConfig getMetricConfig(Configuration metricConfig,
                                               Injector injector) {
        double threshold = 0.0;
        if (metricConfig.asMap().containsKey("threshold")) {
            threshold = metricConfig.getDouble("threshold");
        }
        double minValue = 0.0;
        if (metricConfig.asMap().containsKey("minValue")) {
            minValue = metricConfig.getDouble("minValue");
        }
        List<String> itemKeys = metricConfig.getStringList("itemKeys");
        List<String> recKeys = metricConfig.getStringList("recKeys");
        if (recKeys == null) {
            recKeys = itemKeys;
        }
        return new RecallConfig(metricConfig.getIntList("N"),
                itemKeys, recKeys, metricConfig.getString("relevanceKey"),
                threshold, minValue, metricConfig.getString("separator"));
    }

    public Metric getMetric(RequestContext requestContext) {
        return new Recall(N, itemKeys, recKeys, relevanceKey, separator, threshold, minValue);
    }
}
