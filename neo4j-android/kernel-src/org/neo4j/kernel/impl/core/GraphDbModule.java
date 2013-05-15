/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.core;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.impl.cache.AdaptiveCacheManager;
import org.neo4j.kernel.impl.core.NodeManager.CacheType;
import org.neo4j.kernel.impl.nioneo.store.PropertyIndexData;
import org.neo4j.kernel.impl.nioneo.store.RelationshipTypeData;
import org.neo4j.kernel.impl.persistence.EntityIdGenerator;
import org.neo4j.kernel.impl.persistence.PersistenceManager;
import org.neo4j.kernel.impl.transaction.LockManager;

public class GraphDbModule
{
    private static final CacheType DEFAULT_CACHE_TYPE = CacheType.soft;
    private static Logger log = Logger.getLogger( GraphDbModule.class.getName() );

    private boolean startIsOk = true;

    private static final int INDEX_COUNT = 2500;

    private final GraphDatabaseService graphDbService;
    private final TransactionManager transactionManager;
    private final AdaptiveCacheManager cacheManager;
    private final LockManager lockManager;
    private final EntityIdGenerator idGenerator;
    
    private NodeManager nodeManager;
    
    private boolean readOnly = false;

    public GraphDbModule( GraphDatabaseService graphDb,
            AdaptiveCacheManager cacheManager, LockManager lockManager,
            TransactionManager transactionManager, EntityIdGenerator idGenerator,
            boolean readOnly )
    {
        this.graphDbService = graphDb;
        this.cacheManager = cacheManager;
        this.lockManager = lockManager;
        this.transactionManager = transactionManager;
        this.idGenerator = idGenerator;
        this.readOnly = readOnly;
    }
    
    public void init()
    {
    }

    public void start( LockReleaser lockReleaser, 
        PersistenceManager persistenceManager, RelationshipTypeCreator relTypeCreator,
        Map<Object,Object> params )
    {
        if ( !startIsOk )
        {
            return;
        }
        
        String cacheTypeName = (String) params.get( Config.CACHE_TYPE );
        CacheType cacheType = null;
        try
        {
            cacheType = cacheTypeName != null ? CacheType.valueOf( cacheTypeName ) : DEFAULT_CACHE_TYPE;
        }
        catch ( IllegalArgumentException e )
        {
            throw new IllegalArgumentException( "Invalid cache type, please use one of: " +
                    Arrays.asList( CacheType.values() ) + " or keep empty for default (" +
                    DEFAULT_CACHE_TYPE + ")", e.getCause() );
        }
        
        if ( !readOnly )
        {
            nodeManager = new NodeManager( graphDbService, cacheManager,
                    lockManager, lockReleaser, transactionManager,
                    persistenceManager, idGenerator, relTypeCreator, cacheType );
        }
        else
        {
            nodeManager = new ReadOnlyNodeManager( graphDbService,
                    cacheManager, lockManager, lockReleaser,
                    transactionManager, persistenceManager, idGenerator, cacheType );
        }
        // load and verify from PS
        RelationshipTypeData relTypes[] = null;
        PropertyIndexData propertyIndexes[] = null;
        // beginTx();
        relTypes = persistenceManager.loadAllRelationshipTypes();
        propertyIndexes = persistenceManager.loadPropertyIndexes( 
            INDEX_COUNT );
        // commitTx();
        nodeManager.addRawRelationshipTypes( relTypes );
        nodeManager.addPropertyIndexes( propertyIndexes );
        if ( propertyIndexes.length < INDEX_COUNT )
        {
            nodeManager.setHasAllpropertyIndexes( true );
        }
        nodeManager.start( params );
        startIsOk = false;
    }
    
    private void beginTx()
    {
        try
        {
            transactionManager.begin();
        }
        catch ( NotSupportedException e )
        {
            throw new TransactionFailureException( 
                "Unable to begin transaction.", e );
        }
        catch ( SystemException e )
        {
            throw new TransactionFailureException( 
                "Unable to begin transaction.", e );
        }
    }
    
    private void commitTx()
    {
        try
        {
            transactionManager.commit();
        }
        catch ( SecurityException e )
        {
            throw new TransactionFailureException( "Failed to commit.", e );
        }
        catch ( IllegalStateException e )
        {
            throw new TransactionFailureException( "Failed to commit.", e );
        }
        catch ( RollbackException e )
        {
            throw new TransactionFailureException( "Failed to commit.", e );
        }
        catch ( HeuristicMixedException e )
        {
            throw new TransactionFailureException( "Failed to commit.", e );
        }
        catch ( HeuristicRollbackException e )
        {
            throw new TransactionFailureException( "Failed to commit.", e );
        }
        catch ( SystemException e )
        {
            throw new TransactionFailureException( "Failed to commit.", e );
        }
    }
    
    public void setReferenceNodeId( Long nodeId )
    {
        nodeManager.setReferenceNodeId( nodeId.longValue() );
        try
        {
            nodeManager.getReferenceNode();
        }
        catch ( NotFoundException e )
        {
            log.warning( "Reference node[" + nodeId + "] not valid." );
        }
    }

    public Long getCurrentReferenceNodeId()
    {
        try
        {
            return nodeManager.getReferenceNode().getId();
        }
        catch ( NotFoundException e )
        {
            return -1L;
        }
    }

    public void createNewReferenceNode()
    {
        Node node = nodeManager.createNode();
        nodeManager.setReferenceNodeId( node.getId() );
    }

    public void reload( Map<Object,Object> params )
    {
        throw new UnsupportedOperationException();
    }

    public void stop()
    {
        nodeManager.clearPropertyIndexes();
        nodeManager.clearCache();
        nodeManager.stop();
    }

    public void destroy()
    {
    }

    public NodeManager getNodeManager()
    {
        return this.nodeManager;
    }

    public Iterable<RelationshipType> getRelationshipTypes()
    {
        return nodeManager.getRelationshipTypes();
    }
}
