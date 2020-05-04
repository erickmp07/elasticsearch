/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.bucket.histogram;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Rounding;
import org.elasticsearch.index.mapper.RangeType;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.AggregationExecutionException;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.support.AggregatorSupplier;
import org.elasticsearch.search.aggregations.support.CoreValuesSourceType;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.aggregations.support.ValuesSourceRegistry;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class DateHistogramAggregatorFactory extends ValuesSourceAggregatorFactory {

    public static void registerAggregators(ValuesSourceRegistry.Builder builder) {
        builder.register(DateHistogramAggregationBuilder.NAME,
            List.of(CoreValuesSourceType.DATE, CoreValuesSourceType.NUMERIC, CoreValuesSourceType.BOOLEAN),
            (DateHistogramAggregationSupplier) (String name,
                                                AggregatorFactories factories,
                                                Rounding rounding,
                                                Rounding shardRounding,
                                                BucketOrder order,
                                                boolean keyed,
                                                long minDocCount,
                                                @Nullable ExtendedBounds extendedBounds,
                                                @Nullable ValuesSource valuesSource,
                                                DocValueFormat formatter,
                                                SearchContext aggregationContext,
                                                Aggregator parent,
                                                Map<String, Object> metadata) -> new DateHistogramAggregator(name,
                factories, rounding, shardRounding, order, keyed, minDocCount, extendedBounds, (ValuesSource.Numeric) valuesSource,
                formatter, aggregationContext, parent, metadata));

        builder.register(DateHistogramAggregationBuilder.NAME,
            CoreValuesSourceType.RANGE,
            (DateHistogramAggregationSupplier) (String name,
                                                AggregatorFactories factories,
                                                Rounding rounding,
                                                Rounding shardRounding,
                                                BucketOrder order,
                                                boolean keyed,
                                                long minDocCount,
                                                @Nullable ExtendedBounds extendedBounds,
                                                @Nullable ValuesSource valuesSource,
                                                DocValueFormat formatter,
                                                SearchContext aggregationContext,
                                                Aggregator parent,
                                                Map<String, Object> metadata) -> {

                ValuesSource.Range rangeValueSource = (ValuesSource.Range) valuesSource;
                if (rangeValueSource.rangeType() != RangeType.DATE) {
                    throw new IllegalArgumentException("Expected date range type but found range type [" + rangeValueSource.rangeType().name
                        + "]");
                }
                return new DateRangeHistogramAggregator(name,
                    factories, rounding, shardRounding, order, keyed, minDocCount, extendedBounds, rangeValueSource, formatter,
                    aggregationContext, parent, metadata); });
    }

    private final BucketOrder order;
    private final boolean keyed;
    private final long minDocCount;
    private final ExtendedBounds extendedBounds;
    private final Rounding rounding;
    private final Rounding shardRounding;

    public DateHistogramAggregatorFactory(String name, ValuesSourceConfig config,
            BucketOrder order, boolean keyed, long minDocCount,
            Rounding rounding, Rounding shardRounding, ExtendedBounds extendedBounds, QueryShardContext queryShardContext,
            AggregatorFactory parent, AggregatorFactories.Builder subFactoriesBuilder,
            Map<String, Object> metadata) throws IOException {
        super(name, config, queryShardContext, parent, subFactoriesBuilder, metadata);
        this.order = order;
        this.keyed = keyed;
        this.minDocCount = minDocCount;
        this.extendedBounds = extendedBounds;
        this.rounding = rounding;
        this.shardRounding = shardRounding;
    }

    public long minDocCount() {
        return minDocCount;
    }

    @Override
    protected Aggregator doCreateInternal(ValuesSource valuesSource,
                                            SearchContext searchContext,
                                            Aggregator parent,
                                            boolean collectsFromSingleBucket,
                                            Map<String, Object> metadata) throws IOException {
        if (collectsFromSingleBucket == false) {
            return asMultiBucketAggregator(this, searchContext, parent);
        }
        AggregatorSupplier aggregatorSupplier = queryShardContext.getValuesSourceRegistry().getAggregator(config.valueSourceType(),
            DateHistogramAggregationBuilder.NAME);
        if (aggregatorSupplier instanceof DateHistogramAggregationSupplier == false) {
            throw new AggregationExecutionException("Registry miss-match - expected DateHistogramAggregationSupplier, found [" +
                aggregatorSupplier.getClass().toString() + "]");
        }
        return ((DateHistogramAggregationSupplier) aggregatorSupplier).build(name, factories, rounding, shardRounding, order, keyed,
            minDocCount, extendedBounds, valuesSource, config.format(), searchContext, parent, metadata);
    }

    @Override
    protected Aggregator createUnmapped(SearchContext searchContext,
                                            Aggregator parent,
                                            Map<String, Object> metadata) throws IOException {
        return new DateHistogramAggregator(name, factories, rounding, shardRounding, order, keyed, minDocCount, extendedBounds,
            null, config.format(), searchContext, parent, metadata);
    }
}
