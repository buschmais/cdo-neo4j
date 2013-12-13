package com.buschmais.cdo.store.json.api;

import com.buschmais.cdo.api.CdoException;
import com.buschmais.cdo.spi.bootstrap.CdoDatastoreProvider;
import com.buschmais.cdo.spi.bootstrap.CdoUnit;
import com.buschmais.cdo.spi.datastore.Datastore;
import com.buschmais.cdo.store.json.impl.JsonFileDatastore;

import java.net.URL;

public class JsonFileStoreProvider implements CdoDatastoreProvider {

    @Override
    public Datastore<?> createDatastore(CdoUnit cdoUnit) {
        URL url = cdoUnit.getUrl();
        if (!"file".equals(url.getProtocol())) {
            throw new CdoException("Only file URLs are supported by this store.");
        }
        return new JsonFileDatastore(url.getPath());
    }
}
