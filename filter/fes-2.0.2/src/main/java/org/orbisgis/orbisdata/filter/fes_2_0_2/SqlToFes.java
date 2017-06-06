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
import net.opengis.wfs._2_1.PropertyType;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class change a String of SQL parameter in an object JaxB.
 * @Author Vincent QUILLIEN
 */
public class SqlToFes {

    /**
     * This method take an String object which have the form of a request SQL and return and object jaxB :
     * FilterType or SortByType.
     *
     * @param objectFromSQL
     * @return the list of JaxBElement
     */
    public static HashMap<String,JAXBElement> sqlToXml(Object objectFromSQL) {
        HashMap<String,JAXBElement> listCommandSql = new HashMap<>();

        if (objectFromSQL != null) {
            if (objectFromSQL instanceof String) {
                Pattern FilterSql =
                        Pattern.compile("(SELECT.*)(FROM.*)(WHERE.*)(ORDER BY.*)|" +
                                "(SELECT.*)(FROM.*)(WHERE.*)|(SELECT.*)(FROM.*)(ORDER BY.*)|(SELECT.*)(FROM.*)", Pattern.MULTILINE);
                String sqlObject = new String((String) objectFromSQL);
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
        }
        return listCommandSql;
    }

    /**
     *
     * @param lineWhereOrOrderBy
     * @return
     */
    private static JAXBElement separatorWhereOrderBy(String lineWhereOrOrderBy){
        ObjectFactory factory = new ObjectFactory();
        JAXBElement returnXml = null;
        JAXBElement<FilterType> filterElement = null;
        JAXBElement<SortByType> sortByElement = null;
        //test the structure of the Object

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
     *
     * @param requestWhere
     * @return
     */
    private static FilterType createFilter(String requestWhere) {
        ObjectFactory factory = new ObjectFactory();
        FilterType filterElement = null;

        Pattern filterComparison = Pattern.compile("([\\w,( )]+)(LIKE|BETWEEN|IS NULL|<|>|>=|<=|!=|=)([\\w,( )%]+)");

        Pattern filterSpatial = Pattern.compile("(!\\( ST_Disjoint\\(|ST_DWithin\\(|ST_Equals\\(|ST_Disjoint\\(|" +
                "ST_Touches\\(|ST_Overlaps\\(|ST_Crosses\\(|ST_Intersects\\(|ST_Contains\\(|ST_Within\\()([\\w,( )]+)\\)");

        Pattern filterLogical = Pattern.compile("(.*)(NOT|AND|OR)(.+)");

        Matcher matcherComparison = filterComparison.matcher(requestWhere);
        Matcher matcherSpatial = filterSpatial.matcher(requestWhere);
        Matcher matcherLogical = filterLogical.matcher(requestWhere);

        if(matcherComparison.matches()) {
            createFilterComparison(matcherComparison);

        }else if(matcherSpatial.matches()){
            createFilterSpatial(matcherSpatial);

        }else if(matcherLogical.matches()){
            createFilterLogical(matcherLogical);

        }else{
            //LOGGER
        }
        return filterElement ;
    }




//------------------------------------------------Operator Comparison-------------------------------------------------



    private static FilterType  createFilterComparison(Matcher matcherComparison){
        ObjectFactory factory = new ObjectFactory();
        FilterType filterElement = null;

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
                String[] listBoundary = matcherComparison.group(3).split(" ");
                upperBoundary.setExpression(getExpressionObject(listBoundary[0].trim(),true));
                lowerBoundary.setExpression(getExpressionObject(listBoundary[1].trim(),true));
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
                propertyIsLessThan.getExpression().add(getExpressionObject(matcherComparison.group(1).trim(), false));
                propertyIsLessThan.getExpression().add(getExpressionObject(matcherComparison.group(3).trim(), true));
                JAXBElement<BinaryComparisonOpType> propertyIsLessThanElement =
                        factory.createPropertyIsLessThan(propertyIsLessThan);
                filterElement.setComparisonOps(propertyIsLessThanElement);
                break;
            case">":
                BinaryComparisonOpType propertyIsGreaterThan = factory.createBinaryComparisonOpType();
                propertyIsGreaterThan.getExpression().add(getExpressionObject(matcherComparison.group(1).trim(), false));
                propertyIsGreaterThan.getExpression().add(getExpressionObject(matcherComparison.group(3).trim(), true));
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

                JAXBElement<BinaryComparisonOpType> propertyIsEqualToToElement =
                        factory.createPropertyIsEqualTo(propertyIsEqualTo);

                filterElement.setComparisonOps(propertyIsEqualToToElement);
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
    private static void createFilterSpatial(Matcher matcherSpatial) {
        ObjectFactory factory = new ObjectFactory();
        FilterType filterElement = null;

        switch (matcherSpatial.group(1)){
            case "ST_DWithin(":
                break;
            case "NOT ST_Disjoint("://BBOX
                break;
            case "ST_Equals(":
                break;
            case "ST_Disjoint(":
                break;
            case "ST_Touches(":
                break;
            case "ST_Overlaps(":
                break;
            case "ST_Crosses(":
                break;
            case "ST_Intersects(":
                break;
            case "ST_Contains(":
                break;
            case "ST_Within(":
                break;
        }
    }
//------------------------------------------------Operator Logical------------------------------------------------------
    private static void createFilterLogical(Matcher matcherSpatial) {
        switch (matcherSpatial.group(1)) {
            case "NOT":
                break;
            case "AND":
                break;
            case "OR":
                break;
        }
    }
//----------------------------------------------------Expression--------------------------------------------------------

    /**
     *
     * @param element
     * @param valueLiteral
     * @return
     */
    private static JAXBElement getExpressionObject(String element, Boolean valueLiteral){
        ObjectFactory factory = new ObjectFactory();
        JAXBElement jaxBElement = null;

        Pattern FilterDigit= Pattern.compile("[\\d,]*");
        Pattern FilterFunctionOrValueRef = Pattern.compile("([\\w ]*\\([\\w( )']*\\))|([\\w ]*)");

        Matcher matcherFunctionOrValueRef = FilterFunctionOrValueRef.matcher(element);
        Matcher matcherDigit = FilterDigit.matcher(element);

        if(matcherDigit.matches() | valueLiteral) {//Literal type

            LiteralType literal = factory.createLiteralType();
            literal.getContent().add(matcherDigit.group());
            JAXBElement<LiteralType> literalElement = factory.createLiteral(literal);
            jaxBElement = literalElement;

        }else if(matcherFunctionOrValueRef.matches() && !valueLiteral){//Function Type

            if(matcherFunctionOrValueRef.group(1)!=null) {
                JAXBElement expressionElement = getExpressionObject(matcherFunctionOrValueRef.group(1),false);
                FunctionType function = factory.createFunctionType();
                function.getExpression().add(expressionElement);
                JAXBElement<FunctionType> functionElement = factory.createFunction(function);
                jaxBElement = functionElement;
            }
        }else{//ValueRef Reference
                JAXBElement<String> valueRefElement = factory.createValueReference(matcherFunctionOrValueRef.group(2));
                jaxBElement = valueRefElement;
        }
        return jaxBElement;
    }
}
