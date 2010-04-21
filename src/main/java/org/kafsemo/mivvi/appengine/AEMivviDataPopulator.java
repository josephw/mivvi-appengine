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

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;

import org.kafsemo.mivvi.rest.MivviDataPopulator;
import org.kafsemo.mivvi.sesame.JarRDFXMLParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * A populator that knows how to read RDF data through the AppEngine
 * services. It will be taken from the most recent blob, treated
 * as a zip of RDF/XML.
 * 
 * @author joe
 */
public class AEMivviDataPopulator implements MivviDataPopulator
{
    private final String publicMivviDataUrl = "http://mivvi.net/data/mivvi-data.zip";
    
    public void populate(Repository sr) throws RepositoryException, ServletException, IOException
    {
        DatastoreService dss = DatastoreServiceFactory.getDatastoreService();
        BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
        
        BlobKey bk = AEUtil.getMivviDataBlobKey(dss);
        
        if (bk == null) {
            throw new ServletException("No data uploaded.");
        }

        RepositoryConnection mviRepCn = sr.getConnection();
        
        String base = publicMivviDataUrl;
        
        InputStream in = new BlobInputStream(bs, bk);
        
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in, BlobstoreService.MAX_BLOB_FETCH_SIZE));
        ZipEntry ze;
        
        while ((ze = zis.getNextEntry()) != null) {
            if (ze.isDirectory()) {
                continue;
            }
            
            if (!ze.getName().endsWith(".rdf")) {
                continue;
            }
            
            String uri = "jar:" + base + "!/" + ze.getName();
            
            RDFInserter inserter = new RDFInserter(mviRepCn);
            
            JarRDFXMLParser parser = new JarRDFXMLParser();
            parser.setRDFHandler(inserter);

            try {
                parser.parse(new UnclosingInputStream(zis), uri);
            } catch (RDFParseException rpe) {
                throw new ServletException("Unable to parse " + uri, rpe);
            } catch (RDFHandlerException e) {
                throw new ServletException("Unable to parse " + uri, e);
            }
        }
        
        mviRepCn.close();
    }
    
    /**
     * RDF parsing tries to close the stream when it's done; we wrap
     * that so the underlying zip stays open.
     * 
     * @author joe
     */
    private static class UnclosingInputStream extends FilterInputStream
    {
        public UnclosingInputStream(InputStream in)
        {
            super(in);
        }
        
        @Override
        public void close() throws IOException
        {
        }
    }
}
