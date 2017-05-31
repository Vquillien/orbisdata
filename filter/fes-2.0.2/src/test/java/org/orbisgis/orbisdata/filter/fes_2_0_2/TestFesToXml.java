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


import net.opengis.fes._2_0_2.ObjectFactory;
import net.opengis.fes._2_0_2.SortByType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

import static org.orbisgis.orbisdata.filter.fes_2_0_2.FesToSql.XmlToSql;
import static org.orbisgis.orbisdata.filter.fes_2_0_2.FesToXml.sqlToXml;
import static org.orbisgis.orbisdata.filter.fes_2_0_2.JaxbContainer.JAXBCONTEXT;

/**
 * Test of the classFesToSql
 * @author Vincent QUILLIEN
 */
public class TestFesToXml {



    /**
     * Initialised the attributes from the class
     *
     * @throws JAXBException
     */
    @Before
    public void initialize() throws JAXBException {

    }


    /**
     * Test of the static method for SortBy
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSqlSortBy() throws JAXBException {
        //Branch SortBy
        String request = "ORDER BY column1 ASC, column2";
        Object sortByElement = sqlToXml(request);
        SortByType sortBy = (SortByType) ((JAXBElement) sortByElement).getValue();


        Assert.assertTrue(sortByElement instanceof JAXBElement);
        Assert.assertEquals(((JAXBElement) sortByElement).getName().getLocalPart(),"SortBy");
        Assert.assertEquals(sortBy.getSortProperty().get(0).getValueReference(),"column1");
        Assert.assertEquals(sortBy.getSortProperty().get(0).getSortOrder().value(),"ASC");
        Assert.assertEquals(sortBy.getSortProperty().get(1).getValueReference(),"column2");
       // Assert.assertEquals(sortBy.getSortProperty().get(1).getSortOrder().value(),"DESC");
    }


    /**
     * Test of the static method for the comparison operator
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSqlFilterComparison() throws JAXBException {

        //Branch Between

        //Branch Like

        //Branch Nil

        //Branch Null

        //Branch PropertyIsGreaterThan

    }

    /**
     * Test of the static method for the spatial operator
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSqlFilterSpatial() throws JAXBException {

        //Branch Equals

        //Branch Dwithin

        //Branch BBOX

    }

    /**
     * Test of the static method for the type Function
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSqFilterFunction() throws JAXBException {

        //Branch Equals
    }

    /**
     * Test of the static method for the type logical
     *
     * @throws JAXBException
     */
    @Test
    public void testXmlToSqlFilterLogical() throws JAXBException {

        //Branch AndOr

        //Branch Not

    }

}
