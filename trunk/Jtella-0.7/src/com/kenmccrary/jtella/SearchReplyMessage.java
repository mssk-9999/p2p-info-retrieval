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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.kenmccrary.jtella.util.Log;

/**
 *  Search Reply message(QUERY HIT), response to a search request
 *
 *
 */
public class SearchReplyMessage extends Message
{
  private Vector fileRecords = new Vector();

  // These are used to send the message
  private SearchMessage searchMessage;
  private short port;
  private String ipAddress;
  private int speed;
  private String vendorCode;

  /**
   *  Construct a SearchReply message
   *
   *
   */
  SearchReplyMessage()
  {
    super(Message.QUERYREPLY);
  }

  /**
   *  Construct a search reply message from data read
   *  read from network
   *
   *  @param rawMessage binary data from a connection
   *  @param originatingConnection Connection creating this message
   */
  SearchReplyMessage(short[] rawMessage, Connection originatingConnection)
  {
    super(rawMessage, originatingConnection);
  }

  /**
   *  Used to respond to a query message
   *
   *  @param searchMessage the search thats being responded to
   *  @param port the point used for download
   *  @param ipAddress of the servant
   *  @param speed download speed in kilobytes/sec
   *  */
  public SearchReplyMessage(SearchMessage searchMessage,
                            short port,
                            String ipAddress,
                            int speed)
  {
    this(searchMessage, port, ipAddress, speed, null);
  }

  /**
   *  Used to respond to a query message
   *
   *  @param searchMessage the search thats being responded to
   *  @param port the point used for download
   *  @param ipAddress of the servant
   *  @param speed download speed in kilobytes/sec
   *  @param vendorCode option 4 byte value identifying the servant vendor
   *  */
  public SearchReplyMessage(SearchMessage searchMessage,
                            short port,
                            String ipAddress,
                            int speed,
                            String vendorCode)
  {
    super(Message.QUERYREPLY);
    setGUID(searchMessage.getGUID());
    this.searchMessage = searchMessage;
    this.port = port;
    this.ipAddress = ipAddress;
    this.speed = speed;
    this.vendorCode = vendorCode;
  }

  /**
   *  Contructs the payload for the search reply message
   *
   */
  void buildPayload()
  {

    try
    {
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      DataOutputStream dataStream = new DataOutputStream(byteStream);

      // hit count
      dataStream.writeByte(fileRecords.size());

      // port, little endian
      int portByte1 = 0x00FF & port;
      int portByte2 = (0xFF00 & port) >> 8;
      dataStream.write(portByte1);
      dataStream.write(portByte2);

      // IP Address
      int beginIndex = 0;
      int endIndex = ipAddress.indexOf('.');

      int ip1 = Integer.parseInt(ipAddress.substring(beginIndex, endIndex));

      beginIndex = endIndex + 1;
      endIndex = ipAddress.indexOf('.', beginIndex);

      int ip2 = Integer.parseInt(ipAddress.substring(beginIndex, endIndex));

      beginIndex = endIndex + 1;
      endIndex = ipAddress.indexOf('.', beginIndex);

      int ip3 = Integer.parseInt(ipAddress.substring(beginIndex, endIndex));

      beginIndex = endIndex + 1;

      int ip4 = Integer.parseInt(ipAddress.substring(beginIndex, ipAddress.length()));

      dataStream.writeByte(ip1);
      dataStream.writeByte(ip2);
      dataStream.writeByte(ip3);
      dataStream.writeByte(ip4);

      // download speed, little endian
      int speedByte1 = 0x000000FF & speed;
      int speedByte2 = (0x0000FF00 & speed) >> 8;
      int speedByte3 = (0x00FF0000 & speed) >> 16;
      int speedByte4 = (0xFF000000 & speed) >> 24;
      dataStream.writeByte(speedByte1);
      dataStream.writeByte(speedByte2);
      dataStream.writeByte(speedByte3);
      dataStream.writeByte(speedByte4);

      // result set
      for (int i = 0; i < fileRecords.size(); i++)
      {
        FileRecord fileRecord = (FileRecord)fileRecords.elementAt(i);
        byte[] fileRecordData = fileRecord.getBytes();

        dataStream.write(fileRecordData);
      }

      // bearshare trailer
      if ( null != vendorCode )
      {
        byte[] byteData = vendorCode.getBytes();

        // write the 4 byte vendor code
        for (int i = 0; i < 4; i++)
        {
          dataStream.writeByte(byteData[i]);
        }

        // open data size
        // todo support Quality of Server statistics here
        dataStream.writeByte(0); // no further datafs

      }
      
      // client identifier
      short[] clientID = Utilities.getClientIdentifier();

      for (int i = 0; i < clientID.length; i++)
      {
        dataStream.writeByte(clientID[i]);
      }

      addPayload(byteStream.toByteArray());
      dataStream.close();
    }
    catch(IOException io)
    {
      Log.getLog().log(io);
    }


  }

  /**
   *  Query the umber of files found for the search
   *
   */
  public int getFileCount()
  {
    return payload[0];
  }


  /**
   *  Query the port for this search reply
   *
   *  @return port
   */
  public int getPort()
  {
    int port = 0;
    int byte1 = 0;
    int byte2 = 0;
    byte1 |= payload[1];
    byte2 |= payload[2];

    // the port is in little endian format
    port |= byte1;
    port |= (byte2 << 8);

    return port;
  }


  /**
   *  Query the IP address for this pong message
   *  result is an IP address in the form of
   *  "206.26.48.100".
   *
   *  @return IP address
   */
  public String getIPAddress()
  {
    StringBuffer ipBuffer = new StringBuffer();

    ipBuffer.append(Integer.toString(payload[3])).
             append(".").
             append(Integer.toString(payload[4])).
             append(".").
             append(Integer.toString(payload[5])).
             append(".").
             append(Integer.toString(payload[6]));

    return ipBuffer.toString();
  }

  /**
   *  Returns the replying host's connection bandwidth
   *
   *  @return download speed, in kilobytes/sec
   */
  public int getDownloadSpeed()
  {
    int byte1 = payload[7];
    int byte2 = payload[8];
    int byte3 = payload[9];
    int byte4 = payload[10];

    return byte1 | (byte2 << 8 | byte3 << 16 | byte4 << 24);

  }

  /**
   *  Adds a file record. This is for originating a message
   *  for a query hit
   *
   *  @param fileRecord file information
   */
  public void addFileRecord(FileRecord fileRecord)
  {
    fileRecords.addElement(fileRecord);
  }

  /**
   *  Get information about the files found
   *
   */
  public FileRecord getFileRecord(int index)
  {
    if ( 0 == fileRecords.size() )
    {
      int payloadIndex = 11;

      for (int i = 0; i < getFileCount(); i ++ )
      {
        // TODO calculate size/index

        // Read the index
        int index1 = payload[payloadIndex++];
        int index2 = payload[payloadIndex++];
        int index3 = payload[payloadIndex++];
        int index4 = payload[payloadIndex++];

        int fileIndex = 0;
        fileIndex |= index1;
        fileIndex |= index2 << 8;
        fileIndex |= index3 << 16;
        fileIndex |= index4 << 24;

        // Read the size
        int size1 = payload[payloadIndex++];
        int size2 = payload[payloadIndex++];
        int size3 = payload[payloadIndex++];
        int size4 = payload[payloadIndex++];

        int fileSize = 0;
        fileSize |= size1;
        fileSize |= size2 << 8;
        fileSize |= size3 << 16;
        fileSize |= size4 << 24;

        // Read the file terminated by double null
        // temp
        int nullCount = 0;
        Vector stringData = new Vector();
        while ( 2 != nullCount )
        {

          byte b = (byte)payload[payloadIndex++];
          
          if  ( 0 == nullCount )
          {  
            // The file name is terminated by null which is sometimes followed
            // by another null but in some cases (mp3) extra data is between
            // the null characters
            stringData.addElement(new Byte(b));
          }
          
          if ( 0 == b )
          {
            nullCount++;
          }
        }

        byte[] stringBytes = new byte[stringData.size()];

        for(int z = 0; z < stringBytes.length; z++)
        {
          stringBytes[z] = ((Byte)stringData.get(z)).byteValue();
        }

        fileRecords.addElement(new FileRecord(fileIndex,
                                              fileSize,
                                              new String(stringBytes)));

      }
    }

    return (FileRecord)fileRecords.elementAt(index);
  }


  /**
   *  Retrieve the client GUID for the replying servant
   *
   *  @return client GUID
   */
  public GUID getClientIdentifier()
  {
    int startIndex = payloadSize - 16; // GUID is last 16 bytes
    short[] guidData = new short[16];

    for (int i = 0; i < 16; i++)
    {
      guidData[i] = payload[startIndex++];
    }

    return new GUID(guidData);
  }

  /**
   *  Retrieve the vendor code for the responding servant
   *
   *  @return vendor code or NONE if the code is not present
   */
  public String getVendorCode()
  {
    String code = "NONE";

    if ( isBearShareTrailerPresent() )
    {
      int trailerIndex = getFileRecordBounds();

      byte[] codeData = new byte[4];
      for (int i = 0; i < 4; i++ )
      {
        codeData[i] = (byte)payload[trailerIndex++];
      }

      code = new String(codeData);
    }

    return code;
  }

  /**
   *  Produces a byte[] suitable for
   *  GNUTELLA network
   *
   */
  byte[] getByteArray()
  {
    // construct the payload prior to sending if the payload doesn't exist
    // the payload will not exist for a SearchReply generated by the JTella
    // application. If this is a message we are routing, the payload is read
    // from the stream
    if ( 0 == getPayloadLength() )
    {
      buildPayload();
    }
    
    return super.getByteArray();
  }

  /**
   *  Check if the bearshare trailer is present between file records
   *  and GUID
   *
   */
  private boolean isBearShareTrailerPresent()
  {
    return getFileRecordBounds() != (payloadSize - 16);
  }

  /**
   *  Method to determine the ending ending for file data. This can be used
   *  to determine if the message contains the BearShare trailer
   *
   *  @return ending index
   */
  private int getFileRecordBounds()
  {
    int boundsIndex = 11;

    for (int i = 0; i < getFileCount(); i++ )
    {
      boundsIndex += 4; // File Index
      boundsIndex += 4; // File Size

      // asci file name terminated by nulls which may not be continguous
      int nullCount = 0;
      while ( 2 != nullCount )
      {
        byte b = (byte)payload[boundsIndex++];
        if ( 0 == b )
        {
          nullCount++;
        }
      }
    }

    return boundsIndex;
  }

  /**
   *  Represents information about a single file served
   *
   */
  static public class FileRecord
  {
     int index;
     int size;
     String fileName;

     /**
      *  Constructs a record describing a shared file
      *
      *  @param index index of the file
      *  @param size size of the file in bytes
      *  @param filename file name
      */
     public FileRecord(int index, int size, String fileName)
     {
       this.index = index;
       this.size = size;
       this.fileName = fileName;
     }

     /**
      *  Get the index of the file
      *
      *  @return index
      */
     public int getIndex()
     {
       return index;
     }

     /**
      *  Get the size of the file
      *
      *  @return file size
      */
     public int getSize()
     {
       return size;
     }

     /**
      *  Get the file name
      *
      *  @return file name
      */
     public String getName()
     {
       return fileName;
     }

     /**
      *  Flatten the <code>FileRecord</code>
      *
      *  @return bytes
      */
     byte[] getBytes()
     {
       byte[] result = null;
       try
       {
         ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         DataOutputStream dataStream = new DataOutputStream(byteStream);

         // write the index, little endian
         int index1 = 0x000000FF & index;
         int index2 = 0x0000FF00 & index;
         index2 = index2 >> 8;
         int index3 = 0x00FF0000 & index;
         index3 = index3 >> 16;
         int index4 = 0xFF000000 & index;
         index4 = index4 >> 24;

         dataStream.write(index1);
         dataStream.write(index2);
         dataStream.write(index3);
         dataStream.write(index4);

         int size1 = 0x000000FF & size;
         int size2 = 0x0000FF00 & size;
         size2 = size2 >> 8;
         int size3 = 0x00FF0000 & size;
         size3 = size3 >> 16;
         int size4 = 0xFF000000 & size;
         size4 = size4 >> 24;

         dataStream.write(size1);
         dataStream.write(size2);
         dataStream.write(size3);
         dataStream.write(size4);

         // write the file name, terminated by two nulls
         dataStream.write(fileName.getBytes());
         dataStream.writeByte(0);
         dataStream.writeByte(0);

         result = byteStream.toByteArray();
         dataStream.close();
       }
       catch(IOException io)
       {
         Log.getLog().log(io);
       }


       return result;
     }

  }
}
