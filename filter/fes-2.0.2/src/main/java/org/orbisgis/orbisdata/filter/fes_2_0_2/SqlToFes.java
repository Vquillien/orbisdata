/**
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 *
 * OrbisGIS is distributed under GPL 3 license.
 *
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.orbisdata.filter.fes_2_0_2;

import net.opengis.fes._2_0_2.*;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class changes a String of SQL parameter in a JaxB object .
 * @Author Vincent QUILLIEN
 */
public class SqlToFes {

    /**
     * This method take an String object which has the form of a SQL request and return a list of jaxB object :
     * FilterType or/and SortByType. There are two keys to get back the jaxB object from the list.
     *
     * @param sqlRequest String object which has the from of a sql request.
     * @return HashMap<String,JAXBElement>
     */
    public static HashMap<String,JAXBElement> sqlToXml(String sqlRequest) {
        HashMap<String,JAXBElement> listCommandSql = new HashMap<>();

        if (sqlRequest != null) {
            Pattern FilterSql =
                    Pattern.compile("(SELECT.*)(FROM.*)(WHERE.*)(ORDER BY.*)|(SELECT.*)(FROM.*)(WHERE.*)|" +
                            "(SELECT.*)(FROM.*)(ORDER BY.*)|(SELECT.*)(FROM.*)", Pattern.MULTILINE);
            String sqlObject = new String((String) sqlRequest);
            Matcher m = FilterSql.matcher(sqlObject);
            if (m.matches()) {
                for(int i = 1; i<=12;i++){
                    if(m.group(i)!=null){
                        if(i==3 | i==7 ){
                            listCommandSql.put("WHERE",separatorWhereOrderBy(m.group(i)));
                        }
                        if(i==4 | i==10 ){
                            listCommandSql.put("ORDER BY",separatorWhereOrderBy(m.group(i)));
                        }
                    }
                }
            }
        }
        return listCommandSql;
    }

    /**
     * This method separate the command WHERE and ORDER BY and create the jaxB object related.
     * @param lineWhereOrOrderBy The line of the command WHERE or ORDER BY.
     * @return JAXBElement
     */
    private static JAXBElement separatorWhereOrderBy(String lineWhereOrOrderBy){
        ObjectFactory factory = new ObjectFactory();
        JAXBElement returnXml = null;
        JAXBElement<FilterType> filterElement = null;
        JAXBElement<SortByType> sortByElement = null;

        Pattern FilterWhere = Pattern.compile("(WHERE).*|(ORDER BY)[\\w,( )]*");
        Matcher m = FilterWhere.matcher(lineWhereOrOrderBy);
        if(m.matches()) {

            if (lineWhereOrOrderBy.startsWith("WHERE")) {
                String parameter = lineWhereOrOrderBy.substring(5).trim();
                FilterType filter = createFilter(parameter);
                filterElement = factory.createFilter(filter);
                returnXml = filterElement;

            } else {
                String parameter = lineWhereOrOrderBy.substring(8).trim();
                String[] parameterSortProperty = parameter.split(", ");

                SortByType sortBy = factory.createSortByType();
                for (int i = 0; i < parameterSortProperty.length; i++) {
                    SortPropertyType sortProperty = factory.createSortPropertyType();
                    String[] elements = parameterSortProperty[i].split(" ");
                    sortProperty.setValueReference(elements[0].trim());

                    if (elements.length > 1) {
                        sortProperty.setSortOrder(SortOrderType.fromValue(elements[1].trim()));
                    }

                    sortBy.getSortProperty().add(sortProperty);
                }

                sortByElement = factory.createSortBy(sortBy);
                returnXml = sortByElement;
            }

        }
        return returnXml;
    }

    /**
     * This method build the filter object.
     * @param requestWhere the parameter of the request, line WHERE.
     * @return FilterType
     */
    private static FilterType createFilter(String requestWhere) {
        ObjectFactory factory = new ObjectFactory();
        FilterType filterElement = null;

        Pattern filterComparison = Pattern.compile("([\\w,( )]+)(LIKE|BETWEEN|IS NULL|<|>|>=|<=|=)([\\w,( )\'%]*)");

        Pattern filterSpatial = Pattern.compile("(ST_DWithin\\(|ST_Equals\\(|ST_Disjoint\\(|ST_Touches\\(|" +
                "ST_Overlaps\\(|ST_Crosses\\(|ST_Intersects\\(|ST_Contains\\(|ST_Within\\()([\\w,( )]+)\\)");

        Pattern filterLogical = Pattern.compile("(.*)(NOT|AND|OR)(.+)");

        Pattern filterFunction = Pattern.compile("([\\w ]*)\\(([\\w( )']*)\\)");


        Matcher matcherComparison = filterComparison.matcher(requestWhere);
        Matcher matcherSpatial = filterSpatial.matcher(requestWhere);
        Matcher matcherLogical = filterLogical.matcher(requestWhere);
        Matcher matcherFunction = filterFunction.matcher(requestWhere);


        if(matcherLogical.matches()){
            filterElement = createFilterLogical(matcherLogical);

        }else if(matcherComparison.matches()) {
            filterElement = createFilterComparison(matcherComparison);

        }  else if(matcherSpatial.matches()){
            filterElement = createFilterSpatial(matcherSpatial);

        }else if(matcherFunction.matches()){
            filterElement = createFilterFunction(matcherFunction);

        } else{
            //LOGGER
        }
        return filterElement ;
    }




//------------------------------------------------Operator Comparison-------------------------------------------------


    /**
     * This method create the jaxB object of the comparison operator and add it to a Filter Object.
     * @param matcherComparison An object which performed a match operations on the Pattern filterComparison.
     * @return FilterType
     */
    private static FilterType  createFilterComparison(Matcher matcherComparison){
        ObjectFactory factory = new ObjectFactory();
        FilterType filterElement = factory.createFilterType();

        switch (matcherComparison.group(2)){
            case"LIKE":
                PropertyIsLikeType propertyIsLike = factory.createPropertyIsLikeType();
                propertyIsLike.setEscapeChar("!");
                propertyIsLike.setWildCard("*");
                propertyIsLike.setSingleChar("#");
                propertyIsLike.getExpression().add(getExpressionObject(matcherComparison.group(1).trim(), false));
                propertyIsLike.getExpression().add(getExpressionObject(matcherComparison.group(3).trim(), true));
                JAXBElement<PropertyIsLikeType> propertyIsLikeElement = factory.createPropertyIsLike(propertyIsLike);
                filterElement.setComparisonOps(propertyIsLikeElement);
                break;

            case"BETWEEN":
                PropertyIsBetweenType propertyIsBetween = factory.createPropertyIsBetweenType();
                propertyIsBetween.setExpression(getExpressionObject(matcherComparison.group(1).trim(),false));
                UpperBoundaryType upperBoundary = factory.createUpperBoundaryType();
                LowerBoundaryType lowerBoundary = factory.createLowerBoundaryType();
                String[] listBoundary = matcherComparison.group(3).trim().split(" ");
                lowerBoundary.setExpression(getExpressionObject(listBoundary[0].trim(),true));
                upperBoundary.setExpression(getExpressionObject(listBoundary[1].trim(),true));
                propertyIsBetween.setLowerBoundary(lowerBoundary);
                propertyIsBetween.setUpperBoundary(upperBoundary);
                JAXBElement<PropertyIsBetweenType> propertyIsBetweenElement = factory.createPropertyIsBetween(propertyIsBetween);
                filterElement.setComparisonOps(propertyIsBetweenElement);
                break;

            case"IS NULL":
                PropertyIsNullType propertyIsNull = factory.createPropertyIsNullType();
                propertyIsNull.setExpression(getExpressionObject(matcherComparison.group(1).trim(), false));
                JAXBElement<PropertyIsNullType> propertyIsNullElement = factory.createPropertyIsNull(propertyIsNull);
                filterElement.setComparisonOps(propertyIsNullElement);
                break;

            case"<":
                BinaryComparisonOpType propertyIsLessThan = factory.createBinaryComparisonOpType();
                propertyIsLessThan.getExpression().add(
                        getExpressionObject(matcherComparison.group(1).trim(), false));

                propertyIsLessThan.getExpression().add(
                        getExpressionObject(matcherComparison.group(3).trim(), true));

                JAXBElement<BinaryComparisonOpType> propertyIsLessThanElement =
                        factory.createPropertyIsLessThan(propertyIsLessThan);
                filterElement.setComparisonOps(propertyIsLessThanElement);
                break;

            case">":
                BinaryComparisonOpType propertyIsGreaterThan = factory.createBinaryComparisonOpType();
                propertyIsGreaterThan.getExpression().add(
                        getExpressionObject(matcherComparison.group(1).trim(), false));

                propertyIsGreaterThan.getExpression().add(
                        getExpressionObject(matcherComparison.group(3).trim(), true));

                JAXBElement<BinaryComparisonOpType> propertyIsGreaterThanElement =
                        factory.createPropertyIsGreaterThan(propertyIsGreaterThan);

                filterElement.setComparisonOps(propertyIsGreaterThanElement);
                break;

            case">=":
                BinaryComparisonOpType propertyIsGreaterThanOrEqualTo = factory.createBinaryComparisonOpType();
                propertyIsGreaterThanOrEqualTo.getExpression().add(
                        getExpressionObject(matcherComparison.group(1).trim(), false));

                propertyIsGreaterThanOrEqualTo.getExpression().add(
                        getExpressionObject(matcherComparison.group(3).trim(), true));

                JAXBElement<BinaryComparisonOpType> propertyIsGreaterThanOrEqualToElement =
                        factory.createPropertyIsGreaterThanOrEqualTo(propertyIsGreaterThanOrEqualTo);

                filterElement.setComparisonOps(propertyIsGreaterThanOrEqualToElement);
                break;

            case"=":
                BinaryComparisonOpType propertyIsEqualTo = factory.createBinaryComparisonOpType();
                propertyIsEqualTo.getExpression().add(
                        getExpressionObject(matcherComparison.group(1).trim(), false));

                propertyIsEqualTo.getExpression().add(
                        getExpressionObject(matcherComparison.group(3).trim(), true));

                JAXBElement<BinaryComparisonOpType> propertyIsEqualToElement =
                        factory.createPropertyIsEqualTo(propertyIsEqualTo);

                filterElement.setComparisonOps(propertyIsEqualToElement);

                break;

            case"<=":
                BinaryComparisonOpType propertyIsLessThanOrEqualTo = factory.createBinaryComparisonOpType();
                propertyIsLessThanOrEqualTo.getExpression().add(
                        getExpressionObject(matcherComparison.group(1).trim(), false));

                propertyIsLessThanOrEqualTo.getExpression().add(
                        getExpressionObject(matcherComparison.group(3).trim(), true));

                JAXBElement<BinaryComparisonOpType> propertyIsLessThanOrEqualToToElement =
                        factory.createPropertyIsLessThanOrEqualTo(propertyIsLessThanOrEqualTo);

                filterElement.setComparisonOps(propertyIsLessThanOrEqualToToElement);
                break;
        }
        return filterElement;
    }


//-----------------------------------------------Operator Spatial-------------------------------------------------------

    /**
     * This method create the jaxB object of the Spatial operator and add it to a Filter Object.
     * @param matcherSpatial An object which performed a match operations on the Pattern filterSpatial.
     * @return FilterType
     */
    private static FilterType createFilterSpatial(Matcher matcherSpatial) {
        ObjectFactory factory = new ObjectFactory();
        FilterType filterElement = factory.createFilterType();
        String elements = matcherSpatial.group(2).replaceAll(",","");
        String[] listElement = elements.trim().split(" ");
        //not implemented yet
        switch (matcherSpatial.group(1)){
            case "ST_DWithin(":
                DistanceBufferType distanceBuffer = factory.createDistanceBufferType();
                MeasureType measure = factory.createMeasureType();
                measure.setValue(Double.parseDouble(listElement[2].trim()));
                distanceBuffer.setDistance(measure);
                distanceBuffer.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                distanceBuffer.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),false));
                JAXBElement<DistanceBufferType> distanceBufferElement = factory.createDWithin(distanceBuffer);
                filterElement.setSpatialOps(distanceBufferElement);
                break;
            case "NOT ST_Disjoint("://BBOX
                BBOXType bbox = factory.createBBOXType();
                bbox.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                bbox.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),true));
                bbox.getExpressionOrAny().add(getExpressionObject(listElement[2].trim(),true));
                JAXBElement<BBOXType> bboxElement = factory.createBBOX(bbox);
                filterElement.setSpatialOps(bboxElement);
                break;
            case "ST_Equals(":
                BinarySpatialOpType equals = factory.createBinarySpatialOpType();
                equals.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                equals.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),false));
                JAXBElement<BinarySpatialOpType> equalsElement = factory.createEquals(equals);
                filterElement.setSpatialOps(equalsElement);
                break;
            case "ST_Disjoint(":
                BinarySpatialOpType disjoint = factory.createBinarySpatialOpType();
                disjoint.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                disjoint.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),false));
                JAXBElement<BinarySpatialOpType> disjointElement = factory.createDisjoint(disjoint);
                filterElement.setSpatialOps(disjointElement);
                break;
            case "ST_Touches(":
                BinarySpatialOpType touches = factory.createBinarySpatialOpType();
                touches.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                touches.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),false));
                JAXBElement<BinarySpatialOpType> touchesElement = factory.createTouches(touches);
                filterElement.setSpatialOps(touchesElement);
                break;
            case "ST_Overlaps(":
                BinarySpatialOpType overlaps = factory.createBinarySpatialOpType();
                overlaps.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                overlaps.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),false));
                JAXBElement<BinarySpatialOpType> overlapsElement = factory.createOverlaps(overlaps);
                filterElement.setSpatialOps(overlapsElement);
                break;
            case "ST_Crosses(":
                BinarySpatialOpType crosses = factory.createBinarySpatialOpType();
                crosses.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                crosses.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),false));
                JAXBElement<BinarySpatialOpType> crossesElement = factory.createCrosses(crosses);
                filterElement.setSpatialOps(crossesElement);
                break;
            case "ST_Intersects(":
                BinarySpatialOpType intersects = factory.createBinarySpatialOpType();
                intersects.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                intersects.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),false));
                JAXBElement<BinarySpatialOpType> intersectsElement = factory.createIntersects(intersects);
                filterElement.setSpatialOps(intersectsElement);
                break;
            case "ST_Contains(":
                BinarySpatialOpType contains = factory.createBinarySpatialOpType();
                contains.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                contains.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),false));
                JAXBElement<BinarySpatialOpType> containsElement = factory.createContains(contains);
                filterElement.setSpatialOps(containsElement);
                break;
            case "ST_Within(":
                BinarySpatialOpType within = factory.createBinarySpatialOpType();
                within.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(),false));
                within.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(),false));
                JAXBElement<BinarySpatialOpType> withinElement = factory.createWithin(within);
                filterElement.setSpatialOps(withinElement);
                break;
        }
        return filterElement;
    }
//------------------------------------------------Operator Logical------------------------------------------------------

    /**
     * This method create the jaxB object of the Logical operator and add it to a Filter Object.
     * @param matcherLogical An object which performed a match operations on the Pattern filterLogical.
     * @return FilterType
     */
    private static FilterType createFilterLogical(Matcher matcherLogical) {
        ObjectFactory factory = new ObjectFactory();
        FilterType filterElement = factory.createFilterType();
        JAXBElement<BBOXType> bboxElement = null;
        //not implemented yet
        switch (matcherLogical.group(2)) {
            case "NOT":
                UnaryLogicOpType unaryLogic = factory.createUnaryLogicOpType();
                FilterType filterFirstParam = createFilter(matcherLogical.group(3).trim());

                if (filterFirstParam.isSetComparisonOps()) {
                    if(matcherLogical.group(3).trim().contains("=")){
                        Pattern patternNotEqualTo = Pattern.compile("([\\w,( )]+)(=)([\\w,( )\'%]*)");
                        Matcher matcherNotEqualTo = patternNotEqualTo.matcher(matcherLogical.group(3).trim());
                        if(matcherNotEqualTo.matches()) {
                            BinaryComparisonOpType propertyIsNotEqualTo = factory.createBinaryComparisonOpType();
                            propertyIsNotEqualTo.getExpression().add(
                                    getExpressionObject(matcherNotEqualTo.group(1).trim(), false));

                            propertyIsNotEqualTo.getExpression().add(
                                    getExpressionObject(matcherNotEqualTo.group(3).trim(), true));

                            JAXBElement<BinaryComparisonOpType> propertyIsEqualToElement =
                                    factory.createPropertyIsNotEqualTo(propertyIsNotEqualTo);

                            filterElement.setComparisonOps(propertyIsEqualToElement);
                        }
                    }else{
                        unaryLogic.setComparisonOps(filterFirstParam.getComparisonOps());
                    }

                } else if (filterFirstParam.isSetSpatialOps()) {
                    if(matcherLogical.group(3).trim().startsWith("ST_Disjoint")){
                        Pattern patternBBOX = Pattern.compile("(ST_Disjoint\\()([\\w,( )]+)\\)");
                        Matcher matcherBBOX = patternBBOX.matcher(matcherLogical.group(3).trim());
                        if(matcherBBOX.matches()) {
                            String elements = matcherBBOX.group(2).replaceAll(",","");
                            String[] listElement = elements.trim().split(" ");
                            BBOXType bbox = factory.createBBOXType();
                            bbox.getExpressionOrAny().add(getExpressionObject(listElement[0].trim(), false));
                            bbox.getExpressionOrAny().add(getExpressionObject(listElement[1].trim(), true));
                            bbox.getExpressionOrAny().add(getExpressionObject(listElement[2].trim(), true));
                            bboxElement = factory.createBBOX(bbox);
                        }
                    }else{
                        unaryLogic.setSpatialOps(filterFirstParam.getSpatialOps());
                    }

                } else if (filterFirstParam.isSetLogicOps()) {
                    unaryLogic.setLogicOps(filterFirstParam.getLogicOps());
                } else if (filterFirstParam.isSetFunction()) {
                    unaryLogic.setFunction(filterFirstParam.getFunction());
                }

                if(unaryLogic.isSetComparisonOps() | unaryLogic.isSetFunction() | unaryLogic.isSetLogicOps() |
                        unaryLogic.isSetSpatialOps()){
                    filterElement.setLogicOps(factory.createLogicOps(unaryLogic));
                }else{
                    if(bboxElement!=null){
                        filterElement.setSpatialOps(bboxElement);
                    }else{

                    }
                }


                break;
            case "AND":
                BinaryLogicOpType binaryLogicAnd = factory.createBinaryLogicOpType();
                filterFirstParam = createFilter(matcherLogical.group(1).trim());
                FilterType filterSecondParam = createFilter(matcherLogical.group(3).trim());

                if (filterFirstParam.isSetComparisonOps()) {
                    binaryLogicAnd.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterFirstParam.getComparisonOps());
                    binaryLogicAnd.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterSecondParam.getComparisonOps());
                } else if (filterFirstParam.isSetSpatialOps()) {
                    binaryLogicAnd.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterFirstParam.getSpatialOps());
                    binaryLogicAnd.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterSecondParam.getSpatialOps());
                } else if (filterFirstParam.isSetLogicOps()) {
                    binaryLogicAnd.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterFirstParam.getLogicOps());
                    binaryLogicAnd.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterSecondParam.getLogicOps());
                } else if (filterFirstParam.isSetFunction()) {
                    binaryLogicAnd.getComparisonOpsOrSpatialOpsOrTemporalOps().add(
                            factory.createFunction(filterFirstParam.getFunction()));
                    binaryLogicAnd.getComparisonOpsOrSpatialOpsOrTemporalOps().add(
                            factory.createFunction(filterSecondParam.getFunction()));
                }
                filterElement.setLogicOps(factory.createLogicOps(binaryLogicAnd));
                break;
            case "OR":
                BinaryLogicOpType binaryLogicOr = factory.createBinaryLogicOpType();
                filterFirstParam = createFilter(matcherLogical.group(1).trim());
                filterSecondParam = createFilter(matcherLogical.group(3).trim());

                if (filterFirstParam.isSetComparisonOps()) {
                    binaryLogicOr.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterFirstParam.getComparisonOps());
                    binaryLogicOr.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterSecondParam.getComparisonOps());
                } else if (filterFirstParam.isSetSpatialOps()) {
                    binaryLogicOr.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterFirstParam.getSpatialOps());
                    binaryLogicOr.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterSecondParam.getSpatialOps());
                } else if (filterFirstParam.isSetLogicOps()) {
                    binaryLogicOr.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterFirstParam.getLogicOps());
                    binaryLogicOr.getComparisonOpsOrSpatialOpsOrTemporalOps().add(filterSecondParam.getLogicOps());
                } else if (filterFirstParam.isSetFunction()) {
                    binaryLogicOr.getComparisonOpsOrSpatialOpsOrTemporalOps().add(
                            factory.createFunction(filterFirstParam.getFunction()));
                    binaryLogicOr.getComparisonOpsOrSpatialOpsOrTemporalOps().add(
                            factory.createFunction(filterSecondParam.getFunction()));
                }
                JAXBElement<LogicOpsType> jaxBelement = factory.createLogicOps(binaryLogicOr);
                filterElement.setLogicOps(jaxBelement);
                break;
        }
        return filterElement;
    }

//-----------------------------------------------Operator Function------------------------------------------------------
    /**
     * This method create the jaxB object of the Logical operator and add it to a Filter Object.
     * @param matcherFunction An object which performed a match operations on the Pattern filterLogical.
     * @return FilterType
     */
    private static FilterType createFilterFunction(Matcher matcherFunction) {
        ObjectFactory factory = new ObjectFactory();

        String elements = matcherFunction.group(2).replaceAll(",", "");
        String[] listElements = elements.split(" ");
        FilterType filterElement = factory.createFilterType();
        FunctionType function = factory.createFunctionType();
        function.setName(matcherFunction.group(1).trim());
        for(int i=0; i<listElements.length;i++) {
            function.getExpression().add(getExpressionObject(listElements[i].trim(), false));
        }
        return filterElement;
    }
//----------------------------------------------------Expression--------------------------------------------------------

    /**
     * This method create a jaxBElement with the type of one of the Expression : Literal, Function, ValueReference.
     * For this moment, all the parameter of the function type are under the type ValueReference.
     * @param element a parameter of an operator.
     * @param valueLiteral if this parameter is true, in this case, the jaxBElement type will be Literal.
     * @return JAXBElement
     */
    private static JAXBElement getExpressionObject(String element, Boolean valueLiteral){
        ObjectFactory factory = new ObjectFactory();
        JAXBElement jaxBElement = null;

        Pattern FilterDigit= Pattern.compile("[\\d,.]*");
        Pattern FilterFunctionOrValueRef = Pattern.compile("([\\w ]*\\([\\w( )']*\\))|([\\w' ]*)");

        Matcher matcherFunctionOrValueRef = FilterFunctionOrValueRef.matcher(element);
        Matcher matcherDigit = FilterDigit.matcher(element);

        if(matcherDigit.matches() | valueLiteral) {//Literal type

            LiteralType literal = factory.createLiteralType();
            if(matcherDigit.matches()) {
                literal.getContent().add(matcherDigit.group());
            }else{
                literal.getContent().add(element.trim());
            }
            JAXBElement<LiteralType> literalElement = factory.createLiteral(literal);
            jaxBElement = literalElement;

        }else if(matcherFunctionOrValueRef.matches()){

            //Function Type
            if(matcherFunctionOrValueRef.group(1)!=null) {
                JAXBElement expressionElement = getExpressionObject(matcherFunctionOrValueRef.group(1),false);
                FunctionType function = factory.createFunctionType();
                function.getExpression().add(expressionElement);
                JAXBElement<FunctionType> functionElement = factory.createFunction(function);
                jaxBElement = functionElement;

            }else {//Value Reference
                JAXBElement<String> valueRefElement = factory.createValueReference(matcherFunctionOrValueRef.group(2));
                jaxBElement = valueRefElement;
            }
        }
        return jaxBElement;
    }
}
