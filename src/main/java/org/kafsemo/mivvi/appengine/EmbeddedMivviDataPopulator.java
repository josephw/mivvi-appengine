/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright (C) 2004, 2005, 2006, 2010  Joseph Walton
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.appengine;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A simple non-billable populator that uses a zipfile embedded in the
 * application distribution.
 * 
 * @author joe
 */
public class EmbeddedMivviDataPopulator extends AEBlobstorePopulator
{
    public EmbeddedMivviDataPopulator(ServletContext servletContext) throws ServletException
    {
        super(servletContext);
    }
    
    @Override
    InputStream getInputStream() throws ServletException
    {
        InputStream in = getClass().getResourceAsStream("mivvi-data.zip");
        if (in == null) {
            throw new ServletException("No embedded mivvi-data.zip available");
        }
        
        return in;
    }
}
