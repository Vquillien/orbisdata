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


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

import static org.orbisgis.orbisdata.filter.fes_2_0_2.FesToSql.*;
import static org.orbisgis.orbisdata.filter.fes_2_0_2.JaxbContainer.JAXBCONTEXT;

/**
 * Test of the classFesToSql
 * @author Vincent QUILLIEN
 */
public class TestFes2_0_2 {


    /**
     * Object for the process of deserializing XML data into newly created Java content trees.
     */
    Unmarshaller unmarshaller;

    /**
     * Object for the process of serializing Java content trees back into XML data.
     */
    Marshaller marshaller;

    /**
     * Object used for save the data from a file of resources.
     */
    InputStream xml;


    /**
     * Initialised the attributes from the class
     *
     * @throws JAXBException
     */
    @Before
    public void initialize() throws JAXBException {
        unmarshaller = JAXBCONTEXT.createUnmarshaller();
        marshaller = JAXBCONTEXT.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }


    /**
     * Test of the static method for SortBy
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSqlSortBy() throws JAXBException {
        //Branch SortBy
        xml = TestFes2_0_2.class.getResourceAsStream("filter_Sorting.xml");
        JAXBElement element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "depth, temperature DESC");
    }


    /**
     * Test of the static method for the comparison operator
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSqlFilterComparison() throws JAXBException {

        //Branch Between
        xml = TestFes2_0_2.class.getResourceAsStream("filter_PropertyIsBetween.xml");
        JAXBElement element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "depth BETWEEN 100 200 ");

        //Branch Like
        xml = TestFes2_0_2.class.getResourceAsStream("filter_PropertyIsLike.xml");
        element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "LAST_NAME LIKE JOHN* ");

        //Branch Nil
        xml = TestFes2_0_2.class.getResourceAsStream("filter_PropertyIsNil.xml");
        element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "value IS NIL");

        //Branch Null
        xml = TestFes2_0_2.class.getResourceAsStream("filter_PropertyIsNull.xml");
        element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "ValueRef IS NULL");

        //Branch PropertyIsGreaterThan
        xml = TestFes2_0_2.class.getResourceAsStream("filter_PropertyIsGreaterThan.xml");
        element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "DEPTH > 30 ");
    }

    /**
     * Test of the static method for the spatial operator
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSalFilterSpatial() throws JAXBException {

        //Branch Equals
        xml = TestFes2_0_2.class.getResourceAsStream("filter_Equals.xml");
        JAXBElement element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "ST_Equals( 1Parameter , 2Parameter )");

        //Branch Dwithin
        xml = TestFes2_0_2.class.getResourceAsStream("filter_DWithIn.xml");
        element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "ST_DWithin( London , Paris , 344.0 )");


        //Branch BBOX
        xml = TestFes2_0_2.class.getResourceAsStream("filter_BBOX.xml");
        element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "!( ST_Disjoint( element , 100 , 200 )");

    }

    /**
     * Test of the static method for the type Function
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSalFilterFunction() throws JAXBException {

        //Branch Equals
        xml = TestFes2_0_2.class.getResourceAsStream("filter_Function.xml");
        JAXBElement element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "TheFunction( 1Parameter , 2Parameter )");
    }

    /**
     * Test of the static method for the type logical
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSalFilterLogical() throws JAXBException {

        //Branch AndOr
        xml = TestFes2_0_2.class.getResourceAsStream("filter_OrAnd.xml");
        JAXBElement element = (JAXBElement) unmarshaller.unmarshal(xml);
        Assert.assertEquals(XmlToSql(element).toString(), "( ( depth > 80 And depth < 200 ) Or depth BETWEEN 100 200 ) ");


        //Branch Not
        xml = TestFes2_0_2.class.getResourceAsStream("filter_Not.xml");
        element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "!( ( depth > 80 And !( depth < 200 ) ) ) ");

        //Branch Not
        xml = TestFes2_0_2.class.getResourceAsStream("filter_NotRecur.xml");
        element = (JAXBElement) unmarshaller.unmarshal(xml);

        Assert.assertEquals(XmlToSql(element).toString(), "!( !( depth < 200 ) ) ");
    }

}
