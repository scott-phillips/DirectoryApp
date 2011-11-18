package org.apache.directory.server.core.schema.bootstrap;


import javax.naming.NamingException;

import org.apache.directory.server.core.schema.bootstrap.AbstractBootstrapProducer;
import org.apache.directory.server.core.schema.bootstrap.BootstrapRegistries;
import org.apache.directory.server.core.schema.bootstrap.ProducerCallback;
import org.apache.directory.server.core.schema.bootstrap.ProducerTypeEnum;


public class TestSchemaNormalizerProducer extends AbstractBootstrapProducer
{
    public TestSchemaNormalizerProducer()
    {
        super( ProducerTypeEnum.NORMALIZER_PRODUCER );
    }


    // ------------------------------------------------------------------------
    // BootstrapProducer Methods
    // ------------------------------------------------------------------------


    /**
     * @see BootstrapProducer#produce(BootstrapRegistries, ProducerCallback)
     */
    public void produce( BootstrapRegistries registries, ProducerCallback cb )
        throws NamingException
    {
    }
}