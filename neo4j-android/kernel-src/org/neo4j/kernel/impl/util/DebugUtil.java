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
package org.neo4j.kernel.impl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

public class DebugUtil
{
    public static void printShortStackTrace( Throwable cause, int maxNumberOfStackLines )
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter( stringWriter );
        cause.printStackTrace( writer );
        writer.close();
        String string = stringWriter.getBuffer().toString();
        System.out.println( firstLinesOf( string, maxNumberOfStackLines+1 ) );
    }

    public static String firstLinesOf( String string, int maxNumberOfLines )
    {
        // Totally verbose implementation of this functionality :)
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter( stringWriter );
        try
        {
            BufferedReader reader = new BufferedReader( new StringReader( string ) );
            String line = null;
            for ( int count = 0; ( line = reader.readLine() ) != null && count < maxNumberOfLines;
                    count++ )
            {
                writer.println( line );
            }
            writer.close();
            return stringWriter.getBuffer().toString();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Can't happen", e );
        }
    }
}
