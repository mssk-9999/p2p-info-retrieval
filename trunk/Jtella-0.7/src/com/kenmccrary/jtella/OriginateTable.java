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

import com.kenmccrary.jtella.util.Log;

/**
 *   Contains information for routing originated messages
 *
 *
 */
class OriginateTable 
{
  private HashMap hashMap;

  OriginateTable()
  {
    hashMap = new HashMap();
  }
  
  /**
   *  Puts a GUID to MessageReceiver mapping in the table
   *
   *
   */
  synchronized void put(GUID guid, MessageReceiver receiver)
  {
    hashMap.put(guid, receiver);
    Log.getLog().logDebug("OriginateTable storing: " + 
                          guid.toString() +
                          ", new size: " +
                          hashMap.size());
    
  }
  
  /**
   *  Removes the guide/receiver mapping
   *
   */
  synchronized void remove(GUID guid)
  {
    hashMap.remove(guid);
    
    Log.getLog().logDebug("OriginateTable removing: " + 
                          guid.toString() +
                          ", new size: " +
                          hashMap.size());
  }

  /**
   *  Get a message receiver for a GUID
   *
   *
   */
  synchronized MessageReceiver get(GUID guid)
  {
    return (MessageReceiver)hashMap.get(guid);
  }

  /**
   *  Check if <code>MessageReceiver</code> exists for guid
   *  This is equivalent to checking if we sent a message
   *
   */
  boolean containsGUID(GUID guid)
  {
    return hashMap.containsKey(guid);
  }
}
