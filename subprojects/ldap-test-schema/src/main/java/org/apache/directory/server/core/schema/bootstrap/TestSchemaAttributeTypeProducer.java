package org.apache.directory.server.core.schema.bootstrap;

import java.util.ArrayList;
import javax.naming.NamingException;

import org.apache.directory.server.core.schema.bootstrap.AbstractBootstrapProducer;
import org.apache.directory.server.core.schema.bootstrap.BootstrapRegistries;
import org.apache.directory.server.core.schema.bootstrap.ProducerCallback;
import org.apache.directory.server.core.schema.bootstrap.ProducerTypeEnum;
import org.apache.directory.shared.ldap.schema.*;

public class TestSchemaAttributeTypeProducer extends AbstractBootstrapProducer
{

    public TestSchemaAttributeTypeProducer()
    {
        super( ProducerTypeEnum.ATTRIBUTE_TYPE_PRODUCER );
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
        ArrayList names = new ArrayList();
        BootstrapAttributeType attributeType;
        
        // Added by scott
        // attributeTypes: ( 1.3.6.1.4.1.5923.1.1.1.1 NAME 'eduPersonAffiliation' SYNTAX '1.3.6.1.4.1.1466.115.121.1.15' )
        attributeType = newAttributeType( "1.3.6.1.4.1.5923.1.1.1.1", registries );
        attributeType.setDescription( "eduPersonAffiliation created by scott" );
        attributeType.setCanUserModify( ! false );
        attributeType.setObsolete( false );
        attributeType.setLength( -1 );
        attributeType.setUsage( UsageEnum.getUsage( "userApplications" ) );
        attributeType.setSyntaxId( "1.3.6.1.4.1.1466.115.121.1.15" );
        names.clear();
        names.add( "eduPersonAffiliation" );
        attributeType.setNames( ( String[] ) names.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.3.6.1.4.1.5923.1.1.1.1", attributeType );
        
        
        // Added by scott
        // attributeTypes: ( 1.2.840.113556.1.4.1 NAME 'name' SYNTAX '1.3.6.1.4.1.1466.115.121.1.15' SINGLE-VALUE NO-USER-MODIFICATION )
        attributeType = newAttributeType( "1.2.840.113556.1.4.1", registries );
        attributeType.setDescription( "name created by scott" );
        attributeType.setCanUserModify( ! false );
        attributeType.setSingleValue( true );
        attributeType.setCollective( false );
        attributeType.setObsolete( false );
        attributeType.setLength( -1 );
        attributeType.setUsage( UsageEnum.getUsage( "userApplications" ) );
        attributeType.setEqualityId( "caseIgnoreMatch" );
        attributeType.setSubstrId( "caseIgnoreSubstringsMatch" );        attributeType.setSyntaxId( "1.3.6.1.4.1.1466.115.121.1.15" );
        names.clear();
        names.add( "name" );
        attributeType.setNames( ( String[] ) names.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.2.840.113556.1.4.1", attributeType );
        
        // Added by scott
        // attributeTypes: ( 1.2.840.113556.1.4.8 NAME 'userAccountControl' SYNTAX '1.3.6.1.4.1.1466.115.121.1.27' SINGLE-VALUE )
        attributeType = newAttributeType( "1.2.840.113556.1.4.8", registries );
        attributeType.setDescription( "userAccountControl created by scott" );
        attributeType.setCanUserModify( ! false );
        attributeType.setSingleValue( true );
        attributeType.setCollective( false );
        attributeType.setObsolete( false );
        attributeType.setLength( -1 );
        attributeType.setUsage( UsageEnum.getUsage( "userApplications" ) );
        attributeType.setEqualityId( "caseIgnoreMatch" );
        attributeType.setSubstrId( "caseIgnoreSubstringsMatch" );        attributeType.setSyntaxId( "1.3.6.1.4.1.1466.115.121.1.15" );
        names.clear();
        names.add( "userAccountControl" );
        attributeType.setNames( ( String[] ) names.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.2.840.113556.1.4.8", attributeType );
        
        
        // Added by scott
        // attributeTypes: ( 1.2.840.113556.1.4.782 NAME 'objectCategory' SYNTAX '1.3.6.1.4.1.1466.115.121.1.12' SINGLE-VALUE )
        attributeType = newAttributeType( "1.2.840.113556.1.4.782", registries );
        attributeType.setDescription( "objectCategory created by scott" );
        attributeType.setCanUserModify( ! false );
        attributeType.setSingleValue( true );
        attributeType.setCollective( false );
        attributeType.setObsolete( false );
        attributeType.setLength( -1 );
        attributeType.setUsage( UsageEnum.getUsage( "userApplications" ) );
        attributeType.setSyntaxId( "1.3.6.1.4.1.1466.115.121.1.12" );
        names.clear();
        names.add( "objectCategory" );
        attributeType.setNames( ( String[] ) names.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.2.840.113556.1.4.782", attributeType );
        
        
        // Added by scott
        // attributeTypes: ( 1.2.840.113556.1.2.141 NAME 'department' SYNTAX '1.3.6.1.4.1.1466.115.121.1.15' SINGLE-VALUE )
        attributeType = newAttributeType( "1.2.840.113556.1.2.141", registries );
        attributeType.setDescription( "department created by scott" );
        attributeType.setCanUserModify( ! false );
        attributeType.setSingleValue( false );
        attributeType.setCollective( false );
        attributeType.setObsolete( false );
        attributeType.setLength( -1 );
        attributeType.setUsage( UsageEnum.getUsage( "userApplications" ) );
        attributeType.setEqualityId( "caseIgnoreMatch" );
        attributeType.setSubstrId( "caseIgnoreSubstringsMatch" );
        attributeType.setSyntaxId( "1.3.6.1.4.1.1466.115.121.1.15" );
        names.clear();
        names.add( "department" );
        attributeType.setNames( ( String[] ) names.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "2.5.4.11", attributeType );
        
        
        // Added by scott
        // attributeTypes: ( 1.2.840.113556.1.2.13 NAME 'displayName' SYNTAX '1.3.6.1.4.1.1466.115.121.1.15' SINGLE-VALUE )
        attributeType = newAttributeType( "1.2.840.113556.1.2.13", registries );
        attributeType.setDescription( "displayName created by scott" );
        attributeType.setCanUserModify( ! false );
        attributeType.setSingleValue( false );
        attributeType.setCollective( false );
        attributeType.setObsolete( false );
        attributeType.setLength( -1 );
        attributeType.setUsage( UsageEnum.getUsage( "userApplications" ) );
        attributeType.setEqualityId( "caseIgnoreMatch" );
        attributeType.setSubstrId( "caseIgnoreSubstringsMatch" );
        attributeType.setSyntaxId( "1.3.6.1.4.1.1466.115.121.1.15" );
        names.clear();
        names.add( "displayName" );
        attributeType.setNames( ( String[] ) names.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.2.840.113556.1.2.13", attributeType );
        
        // Added by scott
        // attributeTypes: ( 1.2.840.113556.1.4.221 NAME 'sAMAccountName' SYNTAX '1.3.6.1.4.1.1466.115.121.1.15' SINGLE-VALUE )
        attributeType = newAttributeType( "1.2.840.113556.1.4.221", registries );
        attributeType.setDescription( "sAMAccountName created by scott" );
        attributeType.setCanUserModify( ! false );
        attributeType.setSingleValue( false );
        attributeType.setCollective( false );
        attributeType.setObsolete( false );
        attributeType.setLength( 256 );
        attributeType.setUsage( UsageEnum.getUsage( "userApplications" ) );
        attributeType.setEqualityId( "caseIgnoreMatch" );
        attributeType.setSubstrId( "caseIgnoreSubstringsMatch" );
        attributeType.setSyntaxId( "1.3.6.1.4.1.1466.115.121.1.15" );
        names.clear();
        names.add( "sAMAccountName" );
        attributeType.setNames( ( String[] ) names.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.2.840.113556.1.4.221", attributeType );
        
        // Added by scott
        // attributeTypes: ( 1.3.6.1.4.1.5923.1.1.1.2 NAME 'eduPersonNickname' SYNTAX '1.3.6.1.4.1.1466.115.121.1.15' )
        attributeType = newAttributeType( "1.3.6.1.4.1.5923.1.1.1.2", registries );
        attributeType.setDescription( "eduPersonNickname created by scott" );
        attributeType.setCanUserModify( ! false );
        attributeType.setSingleValue( true );
        attributeType.setCollective( false );
        attributeType.setObsolete( false );
        attributeType.setLength( -1 );
        attributeType.setUsage( UsageEnum.getUsage( "userApplications" ) );
        attributeType.setSyntaxId( "1.3.6.1.4.1.1466.115.121.1.15" );
        names.clear();
        names.add( "eduPersonNickname" );
        attributeType.setNames( ( String[] ) names.toArray( EMPTY ) );
        cb.schemaObjectProduced( this, "1.3.6.1.4.1.5923.1.1.1.2", attributeType );
        
    }
}
