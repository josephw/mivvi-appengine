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

import javax.servlet.ServletException;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

/**
 * Something that knows how to fill a repository
 * with Mivvi data.
 * 
 * @author joe
 */
public interface MivviDataPopulator
{
    void populate(Repository sr) throws ServletException, RepositoryException, IOException;
}
