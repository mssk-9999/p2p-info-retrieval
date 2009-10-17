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

import java.util.Vector;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;

import com.kenmccrary.jtella.util.Log;

/**
 *  A cache of the known hosts on the network
 *
 */
// TODO complete
public class HostCache
{
  private Vector hosts; // TODO use a set


  /**
   * Constructs an empty HostCach
   *
   */
  HostCache()
  {
    hosts = new Vector();
  }
  
  /**
   *   Adds a host to the cache
   *
   *   @param ipAddress internet address
   *   @param port port 
   */
  public void addHost(Host host)
  {
     if ( !hosts.contains(host) )
     {
       hosts.addElement(host);
       Log.getLog().logInformation("Adding host: " + host.toString());
     }  
  }
  
  
  /**
   *  Removes a host from the cache
   *
   *  @param ipAddress address of host to remove
   *  @param port port of host to remove
   */
  public void removeHost(String ipAddress, int port)
  {
      remove(new Host(ipAddress, port, 0, 0));
  }    
  
  /**
   *  Removes a host from the cache
   *
   *  @param host host to remove
   */
  public void remove(Host host)
  {
    hosts.remove(host);  
  }    
    
  /**
   *  Get a list of the Hosts cached
   *
   *  @return host list
   */
  public List getKnownHosts()
  {
    return new LinkedList(hosts);  
  }    

  /**
   *  Remove a host from the cache, probably because its not responding
   *
   */
  public void removeHost(Host host)
  {
    hosts.removeElement(host);

  }

  /**
   *  Query how many hosts are cached
   *  
   *  @return number of hosts
   */
  int size()
  {
    return hosts.size();
  }

  /**
   *  Get the next host available
   *
   *  @return host or null if none available
   */
  Host nextHost()
  {
    if ( size() == 0 ) 
    {
      return null;
    }

    return (Host)(getHosts().nextElement());
  }

  /**
   *  Get an enumeration of the hosts cached
   *
   */
  Enumeration getHosts()
  {
    return hosts.elements();
  }

  /**
   *  Retrieve a random sample of known hosts. The returned sample may be equal
   *  to or smaller than the requested size
   *
   *  @param desired sample size
   *  @return sample
   */
  Host[] getRandomSample(int sampleSize)
  {
    Vector knownHosts = new Vector(hosts);
    
    if ( knownHosts.size() > sampleSize )
    {
      // collect a random sample
      Random random = new Random();
      Host[] hosts = new Host[sampleSize];
      
      for (int i = 0; i < hosts.length; i++)
      {
        int randomIndex = random.nextInt(knownHosts.size());
        hosts[i] = (Host)knownHosts.elementAt(randomIndex);
      }
      
      return hosts;
    }
    else 
    {
      // Known hosts is smaller/equal to the requested sample
      Host[] hosts = new Host[knownHosts.size()];
      
      for (int i = 0; i < hosts.length; i++)
      {
        hosts[i] = (Host)knownHosts.elementAt(i);
      }  
      
      return hosts;
    }
  }
}
