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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Serves up the most recent data as a downloadable blob.
 * 
 * @author joe
 */
public class MivviDataServlet extends HttpServlet
{
    private final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    private final BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        BlobKey k = AEUtil.getMivviDataBlobKey(ds);
        if (k == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        bs.serve(k, resp);
    }
}
