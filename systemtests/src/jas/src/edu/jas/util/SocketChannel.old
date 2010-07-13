/*
 * $Id: SocketChannel.java 1662 2008-02-05 17:22:57Z kredel $
 */

//package edu.unima.ky.parallel;
package edu.jas.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;


/**
 * SocketChannel 
 * provides a communication channel for Java objects 
 * using TCP/IP sockets. 
 * Refactored for java.util.concurrent.
 * @author Akitoshi Yoshida
 * @author Heinz Kredel.
 */
public class SocketChannel {

  /*
   * Input stream from the socket.
   */
  private final ObjectInputStream in;


  /*
   * Output stream to the socket.
   */
  private final ObjectOutputStream out;


  /*
   * Underlying socket.
   */
  private final Socket soc;


  /**
   * Constructs a socket channel on the given socket s.
   * @param s A socket object.
   */
  public SocketChannel(Socket s) throws IOException {
    soc = s;
    //if ( checkOrder(s) ) {
    //  in = new ObjectInputStream(s.getInputStream());
    //  out = new ObjectOutputStream(s.getOutputStream());
    //} else {
      out = new ObjectOutputStream( s.getOutputStream() );
      out.flush();
      in  = new ObjectInputStream( s.getInputStream() );
    //}
  }


  /**
   * Get the Socket
   */
  public Socket getSocket() {
      return soc;
  }


  /**
   * Sends an object
   */
  public void send(Object v) throws IOException {
    synchronized (out) {
      out.writeObject(v);
      out.flush();
    }
  }


  /**
   * Receives an object
   */
  public Object receive() throws IOException, ClassNotFoundException {
    Object v = null;
    synchronized (in) {
      v = in.readObject();
    }
    return v;
  }


  /**
   * Closes the channel.
   */
  public void close() {
    if ( in != null ) {
      try { in.close(); 
      } catch (IOException e) { }
    }
    if ( out != null ) {
      try { out.close(); 
      } catch (IOException e) { }
    }
    if ( soc != null ) {
      try { soc.close(); 
      } catch (IOException e) { }
    }
  }


  /**
   * to string
   */
  @Override
public String toString() {
    return "socketChannel("+soc+")";
  }


  /*
   * Determines the order of stream initialization.
   * @param s A socket's object.  
  private boolean checkOrder(Socket s) throws IOException {
    // first use the port numbers as the key
    int p1 = s.getLocalPort();
    int p2 = s.getPort();
    if (p1 < p2) return true;
    else if (p1 > p2) return false;

    // second use the inetaddr as the key
    int a1 = s.getLocalAddress().hashCode();
    int a2 = s.getInetAddress().hashCode();
    if (a1 < a2) return true;
    else if (a1 > a2) return false;

    // this shouldn't happen
    throw new IOException();
  }
   */

}
