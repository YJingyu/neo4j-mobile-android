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
package org.neo4j.kernel.impl.nioneo.store;

public class RelationshipRecord extends PrimitiveRecord
{
    private final long firstNode;
    private final long secondNode;
    private final int type;
    private long firstPrevRel = Record.NO_PREV_RELATIONSHIP.intValue();
    private long firstNextRel = Record.NO_NEXT_RELATIONSHIP.intValue();
    private long secondPrevRel = Record.NO_PREV_RELATIONSHIP.intValue();
    private long secondNextRel = Record.NO_NEXT_RELATIONSHIP.intValue();

    public RelationshipRecord( long id, long firstNode, long secondNode, int type )
    {
        super( id );
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.type = type;
    }

    public long getFirstNode()
    {
        return firstNode;
    }

    public long getSecondNode()
    {
        return secondNode;
    }

    public int getType()
    {
        return type;
    }

    public long getFirstPrevRel()
    {
        return firstPrevRel;
    }

    public void setFirstPrevRel( long firstPrevRel )
    {
        this.firstPrevRel = firstPrevRel;
    }

    public long getFirstNextRel()
    {
        return firstNextRel;
    }

    public void setFirstNextRel( long firstNextRel )
    {
        this.firstNextRel = firstNextRel;
    }

    public long getSecondPrevRel()
    {
        return secondPrevRel;
    }

    public void setSecondPrevRel( long secondPrevRel )
    {
        this.secondPrevRel = secondPrevRel;
    }

    public long getSecondNextRel()
    {
        return secondNextRel;
    }

    public void setSecondNextRel( long secondNextRel )
    {
        this.secondNextRel = secondNextRel;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "RelationshipRecord[" ).append( getId() ).append( "," ).append(
                inUse() ).append( "," ).append( firstNode ).append( "," ).append(
                secondNode ).append( "," ).append( type ).append( "," ).append(
                firstPrevRel ).append( "," ).append( firstNextRel ).append( "," ).append(
                secondPrevRel ).append( "," ).append( secondNextRel ).append(
                "," ).append( getNextProp() ).append( "]" );
        return buf.toString();
    }
}