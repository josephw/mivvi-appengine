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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.kafsemo.mivvi.rest.MivviDataPopulator;
import org.kafsemo.mivvi.sesame.JarRDFXMLParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/**
 * A populator that reads each file as a separate entity from the
 * datastore.
 * 
 * @author joe
 */
public class AEDatastorePopulator implements MivviDataPopulator
{
    private final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    private final String publicMivviDataUrl;
    
    public AEDatastorePopulator(ServletContext servletContext) throws ServletException
    {
        publicMivviDataUrl = servletContext.getInitParameter("publicMivviDataUrl");
    }
    
    public void populate(Repository sr) throws ServletException,
            RepositoryException, IOException
    {
        RepositoryConnection mviRepCn = sr.getConnection();
        
        Query q = new Query(RefreshDatastoreServlet.KIND);
        
        PreparedQuery pq = ds.prepare(q);
        
        for (Entity e : pq.asIterable()) {
            String name = (String) e.getProperty("name");

            if (!name.endsWith(".rdf")) {
                continue;
            }
            
            Blob data = (Blob) e.getProperty("data");

            String uri = "jar:" + publicMivviDataUrl + "!/" + name;
            InputStream in = new InflaterInputStream(new ByteArrayInputStream(data.getBytes()));
            
            RDFInserter inserter = new RDFInserter(mviRepCn);
            
            JarRDFXMLParser parser = new JarRDFXMLParser();
            parser.setRDFHandler(inserter);

            try {
                parser.parse(in, uri);
            } catch (RDFParseException rpe) {
                throw new ServletException("Unable to parse " + uri, rpe);
            } catch (RDFHandlerException rhe) {
                throw new ServletException("Unable to parse " + uri, rhe);
            }
        }
        
        mviRepCn.close();
    }
}
