package org.apache.directory.server.core.schema.bootstrap;


import java.util.ArrayList;

import org.apache.directory.server.core.schema.bootstrap.AbstractBootstrapSchema;

public class TestSchema extends AbstractBootstrapSchema
{
    public TestSchema()
    {
        super( "uid=admin,ou=system", "TestSchema", "org.apache.directory.server.core.schema.bootstrap" );

        ArrayList list = new ArrayList();
        list.clear();
        list.add( "system" );
        list.add( "core" );
        list.add( "cosine" );
        list.add( "inetorgperson" );
        setDependencies( ( String[] ) list.toArray( DEFAULT_DEPS ) );
    }
}
