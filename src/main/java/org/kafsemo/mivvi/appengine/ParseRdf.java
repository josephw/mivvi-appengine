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
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Diagnostic servlet to force a parse of the current data.
 * 
 * @author joe
 */
public class ParseRdf extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        try {
            resp.setContentType("text/plain");
            
            PrintWriter pw = resp.getWriter();
            
            pw.println(new Date());
            
            MemoryStore ms = new MemoryStore();
            
            SailRepository rep = new SailRepository(ms);
            
            rep.initialize();

//            new AEMivviDataPopulator().populate(rep);
            new EmbeddedMivviDataPopulator().populate(rep);

            RepositoryConnection mviRepCn = rep.getConnection();
            
            pw.println(new Date());
            
            int count = 0;
            RepositoryResult<Statement> stmts = mviRepCn.getStatements(null, null, null, false);
            while (stmts.hasNext()) {
                Statement s = stmts.next();
                if (s.getObject().toString().startsWith("jar:")) {
                    pw.println(s);
                }
                
                count++;
            }
            
            pw.println("Total statements: " + count);
            pw.println(new Date());
            pw.close();
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }
}
