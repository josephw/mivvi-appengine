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

package org.kafsemo.mivvi.rest;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;

/**
 * An index listing including all the series we know about.
 * 
 * @author joe
 */
public class SeriesServlet extends MivviBaseServlet
{
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        populateData();
        
        Graph g = new GraphImpl();

        try {
            for (Resource r : sd.getAllSeries()) {
                sd.exportStatementsAbout(g, r);
            }
        } catch (RepositoryException re) {
            throw new ServletException("Unable to query for all series", re);
        }
        
        try {
            writeGraphAsRdfXml(g, resp);
        } catch (RDFHandlerException e) {
            throw new ServletException("Unable to write output graph", e);
        }
    }
}
