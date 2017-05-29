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

import net.opengis.fes._2_0_2.FilterType;
import net.opengis.fes._2_0_2.ObjectFactory;
import net.opengis.fes._2_0_2.SortByType;
import net.opengis.fes._2_0_2.SortPropertyType;

import javax.xml.bind.JAXBElement;
import java.util.List;

/**
 * This class change a String of SQL parameter in an object JaxB.
 * @Author Vincent QUILLIEN
 */
public class FesToXml {

    /**
     * This method take an object generated by JaxB from a xml file and give un return the SQL parameter.
     *
     * @param objectFromFilterSQL
     * @return The XML object
     */
    public static Object sqlToXml(Object objectFromFilterSQL) {
        ObjectFactory factory = new ObjectFactory();
        JAXBElement returnXml = null;
        JAXBElement<FilterType> filterElement = null;
        JAXBElement<SortByType> sortByElement = null;
        if (objectFromFilterSQL != null) {
            if (objectFromFilterSQL instanceof String) {
                String sqlObject = new String((String) objectFromFilterSQL);
                sqlObject.toUpperCase();//All the String in uppercase

                if (sqlObject.indexOf(" ") != -1) {
                    String command = sqlObject.substring(0, sqlObject.indexOf(" "));
                    String parameter = sqlObject.substring(sqlObject.indexOf(" "));
                    int size = (parameter.split(",")).length;
                    switch (command) {
                        case "WHERE":
                            FilterType filter = createFilter();
                            filterElement = factory.createFilter(filter);
                            returnXml = filterElement;
                            break;
                        case "ORDER BY":
                            SortByType sortBy = factory.createSortByType();
                            for(int i = 0; i<size;i++)
                            sortBy.getSortProperty().add();
                            sortByElement = factory.createSortBy(sortBy);
                            returnXml = sortByElement;
                            break;
                    }
                }
            }
        }
    return returnXml;
    }

    private static FilterType createFilter() {
        FilterType filterElement = null;

        return filterElement ;
    }
}
