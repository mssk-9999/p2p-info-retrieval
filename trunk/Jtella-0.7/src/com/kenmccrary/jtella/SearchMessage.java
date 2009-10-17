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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Vector;

/**
 *  SearchMessage, the message for queries
 *
 *
 */
public class SearchMessage extends Message
{
  private String criteria;
  
  /**
   *  Construct a GNUTella search query
   *
   */
  // TODO something with the speed
  public SearchMessage(String criteria, int minumumSpeed)
  {
    super(Message.QUERY);
    this.criteria = criteria;            

    buildPayload();
  }


  /**
   *  Construct a SearchMessage from data read from network
   *
   *  @param rawMessage binary data from a connection
   *  @param originatingConnection Connection creating this message
   *
   */
  SearchMessage(short[] rawMessage, Connection originatingConnection)
  {
    super(rawMessage, originatingConnection);
  }


  /**
   *  Contructs the payload for the search message
   *
   *  Bytes 0-1 download speed
   *  followed by search query (null terminated)
   */
  void buildPayload()
  {
    byte[] chars = criteria.getBytes();
    short[] payload = new short[chars.length + 3];
    payload[0] = 0; // TODO speed
    payload[1] = 0;

    int payloadIndex = 2;
    for (int i = 0; i < chars.length; i++) 
    {
      payload[payloadIndex] = chars[i];
      payloadIndex += 1;
    }

    payload[payloadIndex] = 0;
    addPayload(payload);

    /*
    try
    {
    
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      DataOutputStream payloadStream = new DataOutputStream(byteStream);
      payloadStream.writeShort(0); // TODO speed
      payloadStream.write(criteria.getBytes());
      payloadStream.writeByte(0); // null terminate
      byte[] byteData = byteStream.toByteArray();

      // TODO better
      short[] shortData = new short[byteData.length];
      System.arraycopy(byteData, 0, shortData, 0, byteData.length);

      addPayload(shortData);
    }
    catch (IOException io)
    {
      // TODO handle
      io.printStackTrace();
    }
    
    */
  }

  /**
   *  Get the minimum download speed for responses
   *
   *  @return download speed
   */
  public int getMinimumDownloadSpeed()
  {
     int byte1 = payload[0];
     int byte2 = payload[1];

     return byte1 | (byte2 <<8);
  }

  /**
   *  Query the search criteria for this message
   *
   *  @return search criteria
   */
  public String getSearchCriteria()
  {
    if ( null == payload || payload.length < 3 ) 
    {
      // no data
      return new String("");
    }

    /*
    Vector stringData = new Vector();
    int payloadIndex = 2;
    byte b = (byte)payload[payloadIndex++];

    while ( payloadIndex < (payload.length )) 
    {
      if ( 0 != b ) 
      {
        stringData.addElement(new Byte(b));
      }

      b = (byte)payload[payloadIndex++];
    }

    if ( 0 == stringData.size() ) 
    {
      // no data
      return new String("");
    }

    byte[] stringBytes = new byte[stringData.size()];
    for(int z = 0; z < stringBytes.length; z++)
    {
      stringBytes[z] = ((Byte)stringData.get(z)).byteValue();
    }

    return new String(stringBytes);
    */

    byte[] text = new byte[payload.length - 2 - 1]; // 2 - speed, 1 null
    int payloadIndex = 2;
    for (int i = 0; i < text.length; i++) 
    {
      text[i] = (byte)payload[payloadIndex++];
    }

    return new String(text);
  }
}  
