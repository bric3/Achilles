/*
 * Copyright (C) 2012-2016 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.archinnov.achilles.internals.query.dsl.select;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ExecutionInfo;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.util.concurrent.Uninterruptibles;

import info.archinnov.achilles.internals.metamodel.AbstractEntityProperty;
import info.archinnov.achilles.internals.options.Options;
import info.archinnov.achilles.internals.query.StatementProvider;
import info.archinnov.achilles.internals.query.action.SelectAction;
import info.archinnov.achilles.internals.query.options.AbstractOptionsForSelect;
import info.archinnov.achilles.internals.query.raw.TypedMapAware;
import info.archinnov.achilles.internals.runtime.RuntimeEngine;
import info.archinnov.achilles.internals.statements.BoundStatementWrapper;
import info.archinnov.achilles.internals.statements.OperationType;
import info.archinnov.achilles.internals.statements.StatementWrapper;
import info.archinnov.achilles.internals.types.EntityIteratorWrapper;
import info.archinnov.achilles.internals.types.TypedMapIteratorWrapper;
import info.archinnov.achilles.type.TypedMap;
import info.archinnov.achilles.type.interceptor.Event;
import info.archinnov.achilles.type.tuples.Tuple2;

public abstract class AbstractSelectWhere<T extends AbstractSelectWhere<T, ENTITY>, ENTITY>
        extends AbstractOptionsForSelect<T>
        implements SelectAction<ENTITY>, StatementProvider, TypedMapAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSelectWhere.class);

    protected final Select.Where where;

    protected AbstractSelectWhere(Select.Where where) {
        this.where = where;
    }

    protected abstract List<Object> getBoundValuesInternal();

    protected abstract List<Object> getEncodedValuesInternal();

    protected abstract AbstractEntityProperty<ENTITY> getMetaInternal();

    protected abstract Class<ENTITY> getEntityClass();

    protected abstract RuntimeEngine getRte();

    @Override
    public Iterator<ENTITY> iterator() {

        final RuntimeEngine rte = getRte();
        final AbstractEntityProperty<ENTITY> meta = getMetaInternal();
        final Options options = getOptions();

        final StatementWrapper statementWrapper = getInternalBoundStatementWrapper();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Generate iterator for select : %s",
                    statementWrapper.getBoundStatement().preparedStatement().getQueryString()));
        }

        CompletableFuture<ResultSet> futureRS = rte.execute(statementWrapper);
        return new EntityIteratorWrapper<>(futureRS, meta, statementWrapper, options);
    }

    public Iterator<TypedMap> typedMapIterator() {
        final RuntimeEngine rte = getRte();
        final Options options = getOptions();
        final StatementWrapper statementWrapper = getInternalBoundStatementWrapper();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Execute native query async with execution info : %s",
                    statementWrapper.getBoundStatement().preparedStatement().getQueryString()));
        }

        CompletableFuture<ResultSet> futureRS = rte.execute(statementWrapper);

        return new TypedMapIteratorWrapper(futureRS, statementWrapper, options);
    }

    public CompletableFuture<Tuple2<List<ENTITY>, ExecutionInfo>> getListAsyncWithStats() {

        final RuntimeEngine rte = getRte();
        final AbstractEntityProperty<ENTITY> meta = getMetaInternal();
        final Options options = getOptions();

        final StatementWrapper statementWrapper = getInternalBoundStatementWrapper();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Select async with execution info : %s",
                    statementWrapper.getBoundStatement().preparedStatement().getQueryString()));
        }

        CompletableFuture<ResultSet> futureRS = rte.execute(statementWrapper);

        return futureRS
                .thenApply(options::resultSetAsyncListener)
                .thenApply(statementWrapper::logReturnResults)
                .thenApply(statementWrapper::logTrace)
                .thenApply(rs -> Tuple2.of(rs
                                .all()
                                .stream()
                                .map(row -> {
                                    options.rowAsyncListener(row);
                                    return meta.createEntityFrom(row);
                                })
                                .collect(toList()),
                        rs.getExecutionInfo()))
                .thenApply(tuple2 -> {
                    for (ENTITY entity : tuple2._1()) {
                        meta.triggerInterceptorsForEvent(Event.POST_LOAD, entity);
                    }
                    return tuple2;
                });
    }

    /***************************************************************************************
     * TypedMap API                                                                        *
     ***************************************************************************************/
    public CompletableFuture<Tuple2<List<TypedMap>, ExecutionInfo>> getTypeMapsAsyncWithStats() {
        final RuntimeEngine rte = getRte();
        final Options options = getOptions();

        final StatementWrapper statementWrapper = getInternalBoundStatementWrapper();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Select async with execution info : %s",
                    statementWrapper.getBoundStatement().preparedStatement().getQueryString()));
        }
        CompletableFuture<ResultSet> futureRS = rte.execute(statementWrapper);

        return futureRS
            .thenApply(options::resultSetAsyncListener)
                    .thenApply(statementWrapper::logReturnResults)
                    .thenApply(statementWrapper::logTrace)
                    .thenApply(x -> Tuple2.of(mapResultSetToTypedMaps(x), x.getExecutionInfo()));
    }

    public CompletableFuture<List<TypedMap>> getTypeMapsAsync() {
        return getTypeMapsAsyncWithStats()
                .thenApply(Tuple2::_1);
    }

    public Tuple2<List<TypedMap>, ExecutionInfo> getTypeMapsWithStats() {
        try {
            return Uninterruptibles.getUninterruptibly(getTypeMapsAsyncWithStats());
        } catch (ExecutionException e) {
            throw extractCauseFromExecutionException(e);
        }
    }

    public List<TypedMap> getTypedMaps() {
        try {
            return Uninterruptibles.getUninterruptibly(getTypeMapsAsync());
        } catch (ExecutionException e) {
            throw extractCauseFromExecutionException(e);
        }
    }

    public CompletableFuture<Tuple2<TypedMap, ExecutionInfo>> getTypedMapAsyncWithStats() {
        final RuntimeEngine rte = getRte();
        final Options options = getOptions();

        final StatementWrapper statementWrapper = getInternalBoundStatementWrapper();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Execute native query async with execution info : %s",
                    statementWrapper.getBoundStatement().preparedStatement().getQueryString()));
        }

        CompletableFuture<ResultSet> cfutureRS = rte.execute(statementWrapper);

        return cfutureRS
                .thenApply(options::resultSetAsyncListener)
                .thenApply(statementWrapper::logReturnResults)
                .thenApply(statementWrapper::logTrace)
                .thenApply(x -> Tuple2.of(mapRowToTypedMap(x.one()), x.getExecutionInfo()));
    }

    public CompletableFuture<TypedMap> getTypedMapAsync() {
        return getTypedMapAsyncWithStats()
                .thenApply(Tuple2::_1);
    }

    public Tuple2<TypedMap, ExecutionInfo> getTypedMapWithStats() {
        try {
            return Uninterruptibles.getUninterruptibly(getTypedMapAsyncWithStats());
        } catch (ExecutionException e) {
            throw extractCauseFromExecutionException(e);
        }
    }

    public TypedMap getTypedMap() {
        try {
            return Uninterruptibles.getUninterruptibly(getTypedMapAsync());
        } catch (ExecutionException e) {
            throw extractCauseFromExecutionException(e);
        }
    }

    /***************************************************************************************
     * Utility API                                                                         *
     ***************************************************************************************/
    @Override
    public BoundStatement generateAndGetBoundStatement() {
        return getInternalBoundStatementWrapper().getBoundStatement();
    }

    @Override
    public String getStatementAsString() {
        return where.getQueryString();
    }

    @Override
    public List<Object> getBoundValues() {
        return getBoundValuesInternal();
    }

    @Override
    public List<Object> getEncodedBoundValues() {
        return getEncodedValuesInternal();
    }

    private StatementWrapper getInternalBoundStatementWrapper() {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("Get bound statement wrapper"));
        }

        final RuntimeEngine rte = getRte();
        final AbstractEntityProperty<ENTITY> meta = getMetaInternal();
        final Options options = getOptions();

        final PreparedStatement ps = rte.prepareDynamicQuery(where);

        final StatementWrapper statementWrapper = new BoundStatementWrapper(OperationType.SELECT,
                meta, ps,
                getBoundValuesInternal().toArray(),
                getEncodedValuesInternal().toArray());

        statementWrapper.applyOptions(options);
        return statementWrapper;
    }
}
