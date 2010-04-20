package org.kafsemo.mivvi.rest;

import java.io.IOException;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

public interface MivviDataPopulator
{
    void populate(Repository sr) throws RepositoryException, IOException;
}
