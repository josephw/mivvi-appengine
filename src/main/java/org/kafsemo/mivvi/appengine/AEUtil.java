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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Central location for the blob key storage definition.
 * 
 * @author joe
 */
public class AEUtil
{
    public static final String PROP_BLOBKEY = "blobKey";
    
    static Key dataConfigKey()
    {
        return KeyFactory.createKey("Data", 1);
    }
    
    public static BlobKey getMivviDataBlobKey(DatastoreService ds)
    {
        /* Get hold of the current blob to use */
        Key dk = dataConfigKey();

        try {
            Entity de = ds.get(dk);
            Object o = de.getProperty(PROP_BLOBKEY);
            if (o != null) {
                String bk = o.toString();
                return new BlobKey(bk);
            } else {
                return null;
            }
        } catch (EntityNotFoundException e) {
            return null;
        }
    }
}
