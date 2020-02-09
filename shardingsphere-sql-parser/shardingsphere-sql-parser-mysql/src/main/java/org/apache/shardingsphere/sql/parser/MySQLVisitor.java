/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sql.parser;

import lombok.AccessLevel;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AggregationFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BitExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BooleanLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BooleanPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CastFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CharFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConvertFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExtractFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FunctionCallContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GroupConcatFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IntervalExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OrderByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PositionFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PredicateContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SimpleExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SpecialFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SubstringFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UnreservedWord_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WeightStringFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WindowFunctionContext;
import org.apache.shardingsphere.sql.parser.core.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.core.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.predicate.PredicateBuilder;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.value.impl.BooleanValue;
import org.apache.shardingsphere.sql.parser.sql.value.impl.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.impl.LiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.impl.NumberValue;
import org.apache.shardingsphere.sql.parser.sql.value.impl.ParameterMarkerValue;
import org.apache.shardingsphere.sql.parser.util.SQLUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * MySQL visitor.
 *
 * @author panjuan
 */
@Getter(AccessLevel.PROTECTED)
public abstract class MySQLVisitor extends MySQLStatementBaseVisitor<ASTNode> {
    
    private int currentParameterIndex;
    
    @Override
    public final ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        return new ParameterMarkerValue(currentParameterIndex++);
    }
    
    @Override
    public final ASTNode visitLiterals(final LiteralsContext ctx) {
        if (null != ctx.stringLiterals()) {
            return visit(ctx.stringLiterals());
        }
        if (null != ctx.numberLiterals()) {
            return visit(ctx.numberLiterals());
        }
        if (null != ctx.booleanLiterals()) {
            return visit(ctx.booleanLiterals());
        }
        if (null != ctx.nullValueLiterals()) {
            // TODO deal with null value
            return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
        }
        // TODO deal with dateTimeLiterals, hexadecimalLiterals and bitValueLiterals
        return new LiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitStringLiterals(final StringLiteralsContext ctx) {
        String text = ctx.getText();
        return new LiteralValue(text.substring(1, text.length() - 1));
    }
    
    @Override
    public final ASTNode visitNumberLiterals(final NumberLiteralsContext ctx) {
        return new NumberValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitBooleanLiterals(final BooleanLiteralsContext ctx) {
        return new BooleanValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitIdentifier(final IdentifierContext ctx) {
        UnreservedWord_Context unreservedWord = ctx.unreservedWord_();
        return null != unreservedWord ? visit(unreservedWord) : new LiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitUnreservedWord_(final UnreservedWord_Context ctx) {
        return new LiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return visit(ctx.identifier());
    }
    
    @Override
    public final ASTNode visitTableName(final TableNameContext ctx) {
        LiteralValue tableName = (LiteralValue) visit(ctx.name());
        TableSegment result = new TableSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), tableName.getValue());
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            LiteralValue literalValue = (LiteralValue) visit(owner.identifier());
            result.setOwner(new SchemaSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), literalValue.getValue()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnName(final ColumnNameContext ctx) {
        LiteralValue columnName = (LiteralValue) visit(ctx.name());
        ColumnSegment result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnName.getValue());
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            LiteralValue literalValue = (LiteralValue) visit(owner.identifier());
            result.setOwner(new TableSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), literalValue.getValue()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitIndexName(final IndexNameContext ctx) {
        LiteralValue indexName = (LiteralValue) visit(ctx.identifier());
        return new IndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), indexName.getValue());
    }
    
    @Override
    public final ASTNode visitTableNames(final TableNamesContext ctx) {
        CollectionValue<TableSegment> result = new CollectionValue<>();
        for (TableNameContext each : ctx.tableName()) {
            result.getValue().add((TableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnNames(final ColumnNamesContext ctx) {
        Collection<ColumnSegment> segments = new LinkedList<>();
        for (ColumnNameContext each : ctx.columnName()) {
            segments.add((ColumnSegment) visit(each));
        }
        InsertColumnsSegment result = new InsertColumnsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.getColumns().addAll(segments);
        return result;
    }
    
    @Override
    public final ASTNode visitDataTypeName(final DataTypeNameContext ctx) {
        return visit(ctx.identifier(0));
    }
    
    @Override
    public final ASTNode visitExpr(final ExprContext ctx) {
        if (null != ctx.booleanPrimary()) {
            return visit(ctx.booleanPrimary());
        }
        if (null != ctx.logicalOperator()) {
            return new PredicateBuilder(visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.logicalOperator().getText()).mergePredicate();
        }
        // TODO deal with XOR
        return visit(ctx.expr().get(0));
    }
    
    @Override
    public final ASTNode visitBooleanPrimary(final BooleanPrimaryContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.subquery().getText());
        }
        if (null != ctx.comparisonOperator() || null != ctx.SAFE_EQ_()) {
            return createCompareSegment(ctx);
        }
        if (null != ctx.predicate()) {
            return visit(ctx.predicate());
        }
        //TODO deal with IS NOT? (TRUE | FALSE | UNKNOWN | NULL)
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    private ASTNode createCompareSegment(final BooleanPrimaryContext ctx) {
        ASTNode leftValue = visit(ctx.booleanPrimary());
        ASTNode rightValue = visit(ctx.predicate());
        if (!(leftValue instanceof ColumnSegment)) {
            return leftValue;
        }
        PredicateRightValue predicateRightValue = rightValue instanceof ColumnSegment
                ? (ColumnSegment) rightValue : new PredicateCompareRightValue(ctx.comparisonOperator().getText(), (ExpressionSegment) rightValue);
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ColumnSegment) leftValue, predicateRightValue);
    }
    
    @Override
    public final ASTNode visitPredicate(final PredicateContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.subquery().getText());
        }
        if (null != ctx.IN() && null == ctx.NOT()) {
            return createInSegment(ctx);
        }
        if (null != ctx.BETWEEN() && null == ctx.NOT()) {
            return createBetweenSegment(ctx);
        }
        if (1 == ctx.children.size()) {
            return visit(ctx.bitExpr(0));
        }
        return visitRemainPredicate(ctx);
    }
    
    private PredicateSegment createInSegment(final PredicateContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.bitExpr(0));
        Collection<ExpressionSegment> segments = new LinkedList<>();
        for (ExprContext each : ctx.expr()) {
            segments.add((ExpressionSegment) visit(each));
        }
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, new PredicateInRightValue(segments));
    }
    
    private PredicateSegment createBetweenSegment(final PredicateContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.bitExpr(0));
        ExpressionSegment between = (ExpressionSegment) visit(ctx.bitExpr(1));
        ExpressionSegment and = (ExpressionSegment) visit(ctx.predicate());
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, new PredicateBetweenRightValue(between, and));
    }
    
    private ASTNode visitRemainPredicate(final PredicateContext ctx) {
        for (BitExprContext each : ctx.bitExpr()) {
            visit(each);
        }
        for (ExprContext each : ctx.expr()) {
            visit(each);
        }
        for (SimpleExprContext each : ctx.simpleExpr()) {
            visit(each);
        }
        if (null != ctx.predicate()) {
            visit(ctx.predicate());
        }
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitBitExpr(final BitExprContext ctx) {
        SimpleExprContext simple = ctx.simpleExpr();
        if (null != simple) {
            return createExpressionSegment(visit(simple), ctx);
        }
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    private ASTNode createExpressionSegment(final ASTNode astNode, final ParserRuleContext context) {
        if (astNode instanceof LiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((LiteralValue) astNode).getValue());
        }
        if (astNode instanceof NumberValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((NumberValue) astNode).getValue());
        }
        if (astNode instanceof ParameterMarkerValue) {
            return new ParameterMarkerExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((ParameterMarkerValue) astNode).getValue());
        }
        return astNode;
    }
    
    @Override
    public final ASTNode visitSimpleExpr(final SimpleExprContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.subquery().getText());
        }
        if (null != ctx.parameterMarker()) {
            return visit(ctx.parameterMarker());
        }
        if (null != ctx.literals()) {
            return visit(ctx.literals());
        }
        if (null != ctx.intervalExpression()) {
            return visit(ctx.intervalExpression());
        }
        if (null != ctx.functionCall()) {
            return visit(ctx.functionCall());
        }
        if (null != ctx.columnName()) {
            return visit(ctx.columnName());
        }
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitIntervalExpression(final IntervalExpressionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitFunctionCall(final FunctionCallContext ctx) {
        if (null != ctx.aggregationFunction()) {
            return visit(ctx.aggregationFunction());
        }
        if (null != ctx.specialFunction()) {
            return visit(ctx.specialFunction());
        }
        if (null != ctx.regularFunction()) {
            return visit(ctx.regularFunction());
        }
        throw new IllegalStateException("FunctionCallContext must have aggregationFunction, regularFunction or specialFunction.");
    }
    
    @Override
    public final ASTNode visitAggregationFunction(final AggregationFunctionContext ctx) {
        String aggregationType = ctx.aggregationFunctionName_().getText();
        return AggregationType.isAggregationType(aggregationType)
                ? createAggregationSegment(ctx, aggregationType) : new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    private ASTNode createAggregationSegment(final AggregationFunctionContext ctx, final String aggregationType) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        int innerExpressionStartIndex = ((TerminalNode) ctx.getChild(1)).getSymbol().getStartIndex();
        if (null == ctx.distinct()) {
            return new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText(), type, innerExpressionStartIndex);
        }
        return new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText(), type, innerExpressionStartIndex, getDistinctExpression(ctx));
    }
    
    private String getDistinctExpression(final AggregationFunctionContext ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 3; i < ctx.getChildCount() - 1; i++) {
            result.append(ctx.getChild(i).getText());
        }
        return result.toString();
    }
    
    @Override
    public final ASTNode visitSpecialFunction(final SpecialFunctionContext ctx) {
        if (null != ctx.groupConcatFunction()) {
            return visit(ctx.groupConcatFunction());
        }
        if (null != ctx.windowFunction()) {
            return visit(ctx.windowFunction());
        }
        if (null != ctx.castFunction()) {
            return visit(ctx.castFunction());
        }
        if (null != ctx.convertFunction()) {
            return visit(ctx.convertFunction());
        }
        if (null != ctx.positionFunction()) {
            return visit(ctx.positionFunction());
        }
        if (null != ctx.substringFunction()) {
            return visit(ctx.substringFunction());
        }
        if (null != ctx.extractFunction()) {
            return visit(ctx.extractFunction());
        }
        if (null != ctx.charFunction()) {
            return visit(ctx.charFunction());
        }
        if (null != ctx.weightStringFunction()) {
            return visit(ctx.weightStringFunction());
        }
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitGroupConcatFunction(final GroupConcatFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitWindowFunction(final WindowFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitCastFunction(final CastFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitConvertFunction(final ConvertFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitPositionFunction(final PositionFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitSubstringFunction(final SubstringFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitExtractFunction(final ExtractFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitCharFunction(final CharFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitWeightStringFunction(final WeightStringFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitRegularFunction(final RegularFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    // TODO :FIXME, sql case id: insert_with_str_to_date
    private void calculateParameterCount(final Collection<ExprContext> exprContexts) {
        for (ExprContext each : exprContexts) {
            visit(each);
        }
    }
    
    @Override
    public final ASTNode visitOrderByClause(final OrderByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new OrderBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    @Override
    public final ASTNode visitOrderByItem(final OrderByItemContext ctx) {
        OrderDirection orderDirection = null != ctx.DESC() ? OrderDirection.DESC : OrderDirection.ASC;
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            return new ColumnOrderByItemSegment(column, orderDirection);
        }
        if (null != ctx.numberLiterals()) {
            return new IndexOrderByItemSegment(ctx.numberLiterals().getStart().getStartIndex(), ctx.numberLiterals().getStop().getStopIndex(),
                    SQLUtil.getExactlyNumber(ctx.numberLiterals().getText(), 10).intValue(), orderDirection);
        }
        return new ExpressionOrderByItemSegment(ctx.expr().getStart().getStartIndex(), ctx.expr().getStop().getStopIndex(), ctx.expr().getText(), orderDirection);
    }
}
