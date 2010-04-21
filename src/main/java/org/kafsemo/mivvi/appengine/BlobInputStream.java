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
import java.io.InputStream;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreFailureException;
import com.google.appengine.api.blobstore.BlobstoreService;

/**
 * A wrapper around the {@link BlobstoreService} to allow reading
 * from blobs.
 * 
 * @author joe
 */
class BlobInputStream extends InputStream
{
    private final BlobstoreService bs;
    private final BlobKey k;
    private final long length;
    
    private long i;

    public BlobInputStream(BlobstoreService bs, BlobKey k)
    {
        this.bs = bs;
        this.k = k;
        this.length = new BlobInfoFactory().loadBlobInfo(k).getSize();
        
        this.i = 0;
    }
    
    @Override
    public int read(byte[] b, int offset, int readLength) throws IOException
    {
        if (readLength == 0) {
            return 0;
        }
        
        long remaining = length - i;

        if (remaining <= 0) {
            return -1;
        }
        
        int s = readLength;
        if (s > remaining) {
            s = (int) remaining;
        }
        if (s > BlobstoreService.MAX_BLOB_FETCH_SIZE) {
            s = BlobstoreService.MAX_BLOB_FETCH_SIZE;
        }

        byte[] data;
        
        try {
            // XXX Documentation says upper index is exclusive,
            //  behaviour and Python says inclusive
            data = bs.fetchData(k, i, i + s - 1);
        } catch (BlobstoreFailureException bfe) {
            throw new IOException(bfe);
        }

        int l = data.length;
        if (l > s) {
            l = s;
        }
        
        System.arraycopy(data, 0, b, offset, l);
        i += l;

        return l;
    }
    
    @Override
    public int read() throws IOException
    {
        byte[] ba = new byte[1];
        int l = read(ba, 0, 1);
        if (l == 1) {
            return ba[0] & 0xFF;
        } else if (l < 0) {
            return -1;
        } else {
            throw new IOException("Unexpected result for one-byte read: " + l);
        }
    }
    
    @Override
    public long skip(long n) throws IOException
    {
        long remaining = length - i;
        long skipped = Math.min(remaining, n);

        this.i += skipped;
        
        return skipped;
    }
}
