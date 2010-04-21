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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

/**
 * <p>Refreshes the datastore with the most recent Mivvi data from
 * a remote URL.</p>
 * <p>This servlet is a <b>GET with side effects</b> to allow use
 * from the cron facility.</p>
 * 
 * @author joe
 */
public class RefreshDatastoreServlet extends HttpServlet
{
    /* HTTP headers */
    private static final String HDR_IF_MODIFIED_SINCE = "If-Modified-Since";
    private static final String HDR_LAST_MODIFIED = "Last-Modified";
    private static final String HDR_IF_NONE_MATCH = "If-None-Match";
    private static final String HDR_ETAG = "ETag";

    static final String KIND = "mivvidatafile";
    
    String resource;
    
    URLFetchService uf = URLFetchServiceFactory.getURLFetchService();
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        
        resource = config.getServletContext().getInitParameter("publicMivviDataUrl");
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        PrintWriter pw = resp.getWriter();

        Key lrk = KeyFactory.createKey("lastresponseheaders", 1);
        
        Entity lrh;
        try {
            lrh = ds.get(lrk);
        } catch (EntityNotFoundException enfe) {
            lrh = new Entity(lrk);
        }
        
        URL url = new URL(resource);
        HTTPRequest hr = new HTTPRequest(url);

        if (resource.equals(lrh.getProperty("resource"))) {
            if (lrh.hasProperty(HDR_ETAG)) {
                hr.setHeader(new HTTPHeader(HDR_IF_NONE_MATCH, (String) lrh.getProperty(HDR_ETAG)));
            }
            
            if (lrh.hasProperty(HDR_LAST_MODIFIED)) {
                hr.setHeader(new HTTPHeader(HDR_IF_MODIFIED_SINCE, (String) lrh.getProperty(HDR_LAST_MODIFIED)));
            }
        }
        
        HTTPResponse x = uf.fetch(hr);
        
        if (x.getResponseCode() == HttpServletResponse.SC_NOT_MODIFIED) {
            pw.println("Not modified.");
            pw.close();
            return;
        }

        lrh.removeProperty(HDR_ETAG);
        lrh.removeProperty(HDR_LAST_MODIFIED);
        lrh.setProperty("resource", resource);
        
        for (HTTPHeader h : x.getHeaders()) {
            if (h.getName().equalsIgnoreCase(HDR_ETAG)) {
                lrh.setProperty(HDR_ETAG, h.getValue());
            } else if (h.getName().equalsIgnoreCase(HDR_LAST_MODIFIED)) {
                lrh.setProperty(HDR_LAST_MODIFIED, h.getValue());
            }
        }
        
        byte[] content = x.getContent();
        
        pw.println("Response bytes: " + content.length);

        /* Ideally, the whole thing would be in a transaction;
         * but, I Don't Understand the Datastore yet.
         */
        
        /* Check for existing storage */
        long nextFreeId = 1;
        Set<Key> existingEntities = new HashSet<Key>();
        Map<String, Entity> entsByFilename = new HashMap<String, Entity>();
        Query q = new Query(KIND);
        PreparedQuery pq = ds.prepare(q);
        
        int count = 0;
        for (Entity e : pq.asIterable()) {
            existingEntities.add(e.getKey());
            Object filename = e.getProperty("name");
            if (filename instanceof String) {
                entsByFilename.put((String) filename, e);
            }
            if (e.getKey().getId() >= nextFreeId) {
                nextFreeId = e.getKey().getId() + 1;
            }
            count++;
        }
        
        pw.println("Initial entities: " + pq.countEntities());
        

        Collection<Entity> things = new ArrayList<Entity>();
        
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(content));

        byte[] buf = new byte[1024 * 1024];
        ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
            if (ze.isDirectory()) {
                continue;
            }
            
            String name = ze.getName();
            
            Entity ent = entsByFilename.get(name);
            if (ent == null) {
                ent = new Entity(KeyFactory.createKey(KIND, nextFreeId++));
                ent.setProperty("name", name);
            } else {
                existingEntities.remove(ent.getKey());
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStream gz = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION));
            
            int l;
            while ((l = zin.read(buf)) >= 0) {
                gz.write(buf, 0, l);
            }
            gz.close();
            
            ent.setProperty("data", new Blob(out.toByteArray()));
            things.add(ent);
        }
        
        ds.put(things);
        ds.delete(existingEntities);

        /* Record conditional request data after processing */
        ds.put(lrh);
        
        pw.println("Things stored: " + things.size());
        pw.close();
    }
}
