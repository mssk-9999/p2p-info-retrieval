package examples;
/*
 * JTella FileServerExample
 *
 * Copyright (c) 2000 Ken McCrary, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * KEN MCCRARY MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. KEN MCCRARY
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
 
import com.kenmccrary.jtella.GNUTellaConnection;
import com.kenmccrary.jtella.SearchMessage;
import com.kenmccrary.jtella.SearchReplyMessage;
import com.kenmccrary.jtella.PushMessage;
import com.kenmccrary.jtella.SearchReplyMessage.FileRecord;
import com.kenmccrary.jtella.MessageReceiverAdapter;
import com.kenmccrary.jtella.FileServerSession;
import com.kenmccrary.jtella.DownloadConstants;

/**
 *  An example class for demonstrating file serving concepts on the
 *  GNUTella network using JTella. The application expects two command
 *  line parameters, the host name, and the port the servant is listening on
 *
 */
public class FileServerExample
{
  private GNUTellaConnection conn;
  
  /**
   *  Constructs the example given a started network connection
   *
   *  @param networkConnection the GNUTella connection
   */
  public FileServerExample(GNUTellaConnection networkConnection)
  {
    this.conn = networkConnection;
  }
 
  /**
   *  Entry point for the file server example application, host and port number are
   *  expected arguments
   *
   */
  public static void main(String[] args)
  {
    System.out.println("<--- JTella FileServer Example running --->\n");


    if (args.length != 2)
    {
      System.out.println("Usage: FileServerExample host port");
      System.exit(1);
    }

    try
    {
      System.out.println("Connecting to Gnutella Network...");
      
      //-------------------------------------------------------------
      // Start a network connection and create the session
      //-------------------------------------------------------------
      GNUTellaConnection c = new GNUTellaConnection(args[0],
                                  Integer.decode(args[1]).intValue());
      c.start();
      
      TestReceiver receiver = new TestReceiver();
      FileServerSession s = c.createFileServerSession(receiver);
      receiver.setFileServerSession(s);

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

  /**
   *  Test class for receiving search responses
   *
   */
  static class TestReceiver extends MessageReceiverAdapter
  {

    FileServerSession serverSession;


    /**
     *  Entrypoint for search messages
     *
     */
    public void receiveSearch(SearchMessage searchMessage)
    {
      if ( null == serverSession ) 
      {
        return;
      }

      System.out.println("FileServer received query: " + 
                         searchMessage.getSearchCriteria());

      // Respond to a test query only to avoid polluting the network
      if (searchMessage.getSearchCriteria().equals("test999")) 
      {
        System.out.println("FileServer returning query hit: " + 
                           searchMessage.getSearchCriteria());
        SearchReplyMessage.FileRecord record = new SearchReplyMessage.FileRecord( 1, 
                                                                                  1, 
                                                                                  "test");

        // This IP address is garbage, receiving client should respond with a push
        // when it fails to connect
        // Note the servant code is just for testing, other servants will
        // not recognize it
        SearchReplyMessage searchReplyMessage = new SearchReplyMessage(searchMessage,
                                                                       (short)6666, 
                                                                       "123.123.123.123",
                                                                       DownloadConstants.DOWNLOADSPEED_T3,
                                                                       "JTLA"); 

        searchReplyMessage.addFileRecord(record);
        serverSession.queryHit(searchMessage, searchReplyMessage);
      }
    }


    /**
     *  Implement to receive a push request
     *
     *  @param pushMessage request to push a file
     */
    public void receivePush(PushMessage pushMessage)
    {
      System.out.println("FileServer received a push request");
      System.out.println(pushMessage.getIPAddress() + ":" + pushMessage.getPort());
      System.out.println("File Index: " + pushMessage.getFileIndex());
    }

    /**
     *  Attach the session to the receiver
     *
     */
    void setFileServerSession(FileServerSession serverSession)
    {
      this.serverSession = serverSession;
    }
  }
}
