/*
 * Copyright (C) 2000-2001  Ken McCrary
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Email: jkmccrary@yahoo.com
 */
package com.kenmccrary.jtella;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import com.kenmccrary.jtella.util.Log;

/**
 *  Contains history information on Messages used for routing
 *  Maps GUIDs to the connection they arrived on
 *
 */
class RouteTable
{
  private HashMap primaryHashMap;
  private HashMap secondaryHashMap;
  private int maxSize;

  /**
   *  Construct the RouteTable, indicating maximum size
   *
   *  @param maxSize maximum number of records to maintain
   */
  RouteTable(int maxSize)
  {
    // two hastables are used to maintain the history for a finite time, 
    // otherwise the system would accumulate memory continuously
    primaryHashMap = new HashMap();
    secondaryHashMap = new HashMap();
    this.maxSize = maxSize;
  }

  /**
   *  Puts a GUID to Connection mapping in the table
   *
   *
   */
  synchronized void put(GUID guid, NodeConnection connection)
  {
    if ( primaryHashMap.size() > maxSize ) 
    {
      // constrain the history size
      Log.getLog().logDebug("Paging route table");

      secondaryHashMap = primaryHashMap;
      primaryHashMap = new HashMap();        
    }

    primaryHashMap.put(guid, connection);
  }

  /**
   *  Retrieves a Connection for a GUID
   *
   */
  synchronized NodeConnection get(GUID guid)
  {
    NodeConnection node = (NodeConnection)primaryHashMap.get(guid);
    
    if ( null != node )
    {
        return node;
    }
    else
    {
        return (NodeConnection)secondaryHashMap.get(guid);
    }
  }

  /**
   *  Check if Connection history exists for guid
   *
   */
  boolean containsGUID(GUID guid)
  {
    if ( primaryHashMap.containsKey(guid) ||
         secondaryHashMap.containsKey(guid) )
    {
        return true;
    }
    else
    {
        return false;
    }  
  }  
}

