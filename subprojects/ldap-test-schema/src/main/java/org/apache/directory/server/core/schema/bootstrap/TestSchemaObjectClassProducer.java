package org.apache.directory.server.core.schema.bootstrap;


import java.util.ArrayList;
import javax.naming.NamingException;

import org.apache.directory.server.core.schema.bootstrap.AbstractBootstrapProducer;
import org.apache.directory.server.core.schema.bootstrap.BootstrapRegistries;
import org.apache.directory.server.core.schema.bootstrap.ProducerCallback;
import org.apache.directory.server.core.schema.bootstrap.ProducerTypeEnum;
import org.apache.directory.shared.ldap.schema.ObjectClassTypeEnum;


public class TestSchemaObjectClassProducer extends AbstractBootstrapProducer
{

    public TestSchemaObjectClassProducer()
    {
        super( ProducerTypeEnum.OBJECT_CLASS_PRODUCER );
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
        ArrayList array = new ArrayList();
        BootstrapObjectClass objectClass;

        // Made a new schema OID
        objectClass = newObjectClass( "1.scott-person-oid", registries );
        objectClass.setObsolete( false );

        objectClass.setDescription( "Scott's make it work schema for persons" );
        // set the objectclass type
        objectClass.setType( ObjectClassTypeEnum.STRUCTURAL );
        
        // set superior objectClasses
        array.clear();
        array.add( "top" );
        objectClass.setSuperClassIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set must list
        array.clear();
        objectClass.setMustListIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set may list
        array.clear();
        array.add( "eduPersonAffiliation" ); 
        array.add( "name" );
        array.add( "userAccountControl" );
        array.add( "uid" );
        array.add( "mail" ); 
        array.add( "sn" );
        array.add( "cn" );
        array.add( "givenName" );
        array.add( "department" );
        array.add( "objectCategory" );
        array.add( "description" );
        array.add( "displayName" );
        array.add( "eduPersonNickname" );
        array.add( "sAMAccountName" );
        array.add( "userPassword" );
        objectClass.setMayListIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set names
        array.clear();
        array.add( "person" );
        objectClass.setNames( ( String[] ) array.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.scott-person-oid", objectClass );
        
        // ----------------------------------------------------------------
        // ObjectClass: OrganizationalUnit
        // ----------------------------------------------------------------
        
        array = new ArrayList();

        // Made a new schema OID
        objectClass = newObjectClass( "1.scott-organizationalunit-oid", registries );
        objectClass.setObsolete( false );

        objectClass.setDescription( "Scott's make it work schema for organizational units" );
        // set the objectclass type
        objectClass.setType( ObjectClassTypeEnum.STRUCTURAL );
        
        // set superior objectClasses
        array.clear();
        array.add( "top" );
        objectClass.setSuperClassIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set must list
        array.clear();
        objectClass.setMustListIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set may list
        array.clear();
        array.add( "name" );
        array.add( "ou" );
        array.add( "objectCategory" );
        array.add( "sAMAccountName" );
        array.add( "eduPersonNickname" );
        objectClass.setMayListIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set names
        array.clear();
        array.add( "organizationalUnit" );
        objectClass.setNames( ( String[] ) array.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.scott-organizationalunit-oid", objectClass );
        
        
        
        // ----------------------------------------------------------------
        // ObjectClass: user
        // ----------------------------------------------------------------
        
        array = new ArrayList();

        // Made a new schema OID
        objectClass = newObjectClass( "1.scott-user-oid", registries );
        objectClass.setObsolete( false );

        objectClass.setDescription( "Scott's make it work schema for user" );
        // set the objectclass type
        objectClass.setType( ObjectClassTypeEnum.STRUCTURAL );
        
        // set superior objectClasses
        array.clear();
        array.add( "top" );
        objectClass.setSuperClassIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set must list
        array.clear();
        objectClass.setMustListIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set may list
        array.clear();
        objectClass.setMayListIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set names
        array.clear();
        array.add( "user" );
        objectClass.setNames( ( String[] ) array.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.scott-user-oid", objectClass );
        
        
        // ----------------------------------------------------------------
        // ObjectClass: group
        // ----------------------------------------------------------------
        
        array = new ArrayList();

        // Made a new schema OID
        objectClass = newObjectClass( "1.scott-group-oid", registries );
        objectClass.setObsolete( false );

        objectClass.setDescription( "Scott's make it work schema for user" );
        // set the objectclass type
        objectClass.setType( ObjectClassTypeEnum.STRUCTURAL );
        
        // set superior objectClasses
        array.clear();
        array.add( "top" );
        objectClass.setSuperClassIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set must list
        array.clear();
        objectClass.setMustListIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set may list
        array.clear();
        array.add( "cn" );
        array.add( "displayName" );
        array.add( "mail" );
        array.add( "name" );
        array.add( "objectCategory" );
        array.add( "sAMAccountName" );
        array.add( "eduPersonNickname" );
        objectClass.setMayListIds( ( String[] ) array.toArray( EMPTY ) );
        
        // set names
        array.clear();
        array.add( "group" );
        objectClass.setNames( ( String[] ) array.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.scott-group-oid", objectClass );
        

    }
}
