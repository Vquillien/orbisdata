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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Container class with the JAXB contexts
 *
 * @author Vincent QUILLIEN
 */
public class JaxbContainer {
    //Make the JaxbContainer constructor private to avoid its instantiation.
    private JaxbContainer() {}
    //The JaxbContext
    public static final JAXBContext JAXBCONTEXT;
    static {
        try {
            JAXBCONTEXT = JAXBContext.newInstance(
                    net.opengis.wms.ObjectFactory.class,
                    net.opengis.se._2_0.core.ObjectFactory.class,
                    oasis.names.tc.ciq.xsdschema.xal._2.ObjectFactory.class,
                    net.opengis.fes._2_0_2.ObjectFactory.class);
        } catch (JAXBException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
