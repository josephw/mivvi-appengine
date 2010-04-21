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

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Allows new data to be uploaded, replacing the existing data.
 * 
 * @author joe
 */
public class DataUploadServlet extends HttpServlet
{
    private final BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        req.setAttribute("uploadURL", bs.createUploadUrl("/upload"));

        RequestDispatcher dispatcher = 
            req.getRequestDispatcher("WEB-INF/templates/upload.jsp");
        dispatcher.forward(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        Map<String, BlobKey> blobs = bs.getUploadedBlobs(req);

        /* Throw this away unless admin */
        if (!req.isUserInRole("admin")) {
            for (Map.Entry<String, BlobKey> e : blobs.entrySet()) {
                bs.delete(e.getValue());
            }
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        String bk = null;
        
        for (Map.Entry<String, BlobKey> e : blobs.entrySet()) {
            bk = e.getValue().getKeyString();
        }

        if (bk != null) {
            DatastoreService dss = DatastoreServiceFactory.getDatastoreService();
            
            BlobKey previous = AEUtil.getMivviDataBlobKey(dss);
            
            Key k = AEUtil.dataConfigKey();
            
            Entity ent = new Entity(k);
            ent.setProperty(AEUtil.PROP_BLOBKEY, bk);
            dss.put(ent);
            
            if (previous != null) {
                bs.delete(previous);
            }
        }
        
        resp.sendRedirect("/parserdf");
    }
}
