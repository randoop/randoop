/*
 * $Id: DistHashTableServer.java 1779 2008-05-12 10:32:49Z kredel $
 */

package edu.jas.util;

import java.io.IOException;
import java.io.Serializable;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

//import edu.unima.ky.parallel.ChannelFactory;
//import edu.unima.ky.parallel.SocketChannel;


/**
 * Server for the distributed version of a list.
 * @author Heinz Kredel
 * @todo redistribute list for late comming clients, removal of elements.
 */

public class DistHashTableServer extends Thread {

    private static final Logger logger = Logger.getLogger(DistHashTableServer.class);

    public final static int DEFAULT_PORT = 9009; //ChannelFactory.DEFAULT_PORT + 99;
    protected final ChannelFactory cf;

    protected List<DHTBroadcaster> servers;

    private boolean goon = true;
    private Thread mythread = null;

    private DHTCounter listElem = null;
    protected final SortedMap theList;


    /**
     * Constructs a new DistHashTableServer.
     */ 
    public DistHashTableServer() {
        this(DEFAULT_PORT);
    }


    /**
     * DistHashTableServer.
     * @param port to run server on.
     */
    public DistHashTableServer(int port) {
        this( new ChannelFactory(port) );
    }


/**
 * DistHashTableServer.
 * @param cf ChannelFactory to use.
 */
    public DistHashTableServer(ChannelFactory cf) {
        listElem = new DHTCounter(0);
        this.cf = cf;
        servers = new ArrayList<DHTBroadcaster>();
        theList = new TreeMap();
    }


/**
 * main.
 * Usage: DistHashTableServer &lt;port&gt;
 */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if ( args.length < 1 ) {
            System.out.println("Usage: DistHashTableServer <port>");
        } else {
           try {
                port = Integer.parseInt( args[0] );
            } catch (NumberFormatException e) {
            }
        }
        (new DistHashTableServer(port)).run();
        // until CRTL-C
    }


/**
 * thread initialization and start.
 */ 
    public void init() {
        this.start();
    }


/**
 * main server method.
 */ 
    @Override
     public void run() {
       SocketChannel channel = null;
       DHTBroadcaster s = null;
       mythread = Thread.currentThread();
       Entry e;
       Object n;
       Object o;
       while (goon) {
          //logger.debug("list server " + this + " go on");
          try {
               channel = cf.getChannel();
               if ( logger.isDebugEnabled() ) {
                  logger.debug("dls channel = "+channel);
               }
               if ( mythread.isInterrupted() ) {
                  goon = false;
                  //logger.info("list server " + this + " interrupted");
               } else {
                  s = new DHTBroadcaster(channel,servers,listElem,theList);
                  int ls = 0;
                  synchronized (servers) {
                     if ( goon ) {
                        servers.add( s );
                        ls = theList.size();
                        s.start();
                     }
                  }
                  if ( logger.isInfoEnabled() ) {
                     logger.info("server " + s + " started " + s.isAlive());
                  }
                  if ( ls > 0 ) {
                     //logger.debug("sending " + ls + " list elements");
                     synchronized (theList) {
                         Iterator it = theList.entrySet().iterator();
                         for ( int i = 0; i < ls; i++ ) {
                             e = (Entry)it.next();
                             n = e.getKey();
                             o = e.getValue();
                             try {
                                 s.sendChannel( n, o );
                             } catch (IOException ioe) {
                                 // stop s
                             }
                         }
                     } 
                  }
               }
          } catch (InterruptedException end) {
               goon = false;
               Thread.currentThread().interrupt();
          }
       }
       if ( logger.isDebugEnabled() ) {
          logger.debug("listserver " + this + " terminated");
       }
    }


/**
 * terminate all servers.
 */ 
    public void terminate() {
        goon = false;
        logger.debug("terminating ListServer");
        if ( cf != null ) {
           cf.terminate();
        }
        if ( servers != null ) {
           synchronized (servers) {
              Iterator it = servers.iterator();
              while ( it.hasNext() ) {
                 DHTBroadcaster br = (DHTBroadcaster) it.next();
                 br.closeChannel();
                 try { 
                     int c = 0; 
                     while ( br.isAlive() ) {
                           c++;
                           if ( c > 10 ) {
                              logger.warn("giving up on " + br);
                              break;
                           }
                           //System.out.print(".");
                           br.interrupt(); 
                           br.join(100);
                     }
                     if ( logger.isDebugEnabled() ) {
                        logger.debug("server " + br + " terminated");
                     }
                 } catch (InterruptedException e) { 
                     Thread.currentThread().interrupt();
                 }
              }
              servers.clear();
           }
           //? servers = null;
        }
        logger.debug("DHTBroadcasters terminated");
        if ( mythread == null ) {
           return;
        }
        try { 
            while ( mythread.isAlive() ) {
                  //System.out.print("-");
                  mythread.interrupt(); 
                  mythread.join(100);
            }
            if ( logger.isDebugEnabled() ) {
               logger.debug("server terminated " + mythread);
            }
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt();
        }
        mythread = null;
        logger.debug("ListServer terminated");
    }


/**
 * number of servers.
 */ 
    public int size() {
        synchronized (servers) {
           return servers.size();
        }
    }

}


/**
 * Class for holding the list index used a key in TreeMap.
 * Implemented since Integer has no add() method.
 * Must implement Comparable so that TreeMap works with correct ordering.
 * @unused
 */ 

class DHTCounter implements Serializable, Comparable<DHTCounter> {

    private int value;


   /**
    * DHTCounter constructor.
    */
    public DHTCounter() {
        this(0);
    }


   /**
    * DHTCounter constructor..
    * @param v initial value.
    */
    public DHTCounter(int v) {
        value = v;
    }


   /**
    * intValue.
    * @return the value.
    */
    public int intValue() {
        return value;
    }


   /**
    * add.
    * @param v value to add.
    */
    public void add(int v) { // synchronized elsewhere
        value += v;
    }


    /**
     * equals.
     * @param ob an Object.
     * @return true if this is equal to o, else false.
     */
    @Override
     public boolean equals(Object ob) {
        if ( ! (ob instanceof DHTCounter) ) {
           return false;
        }
        return 0 == compareTo( (DHTCounter)ob );
    }




   /**
    * compareTo.
    * @param c a DHTCounter.
    * @return 1 if (this &lt; c), 0 if (this == c), -1 if (this &gt; c).
    */
    public int compareTo(DHTCounter c) {
        int x = c.intValue();
        if ( value > x ) { 
           return 1;
        }
        if ( value < x ) { 
           return -1;
        }
        return 0;
    }


   /**
    * toString.
    */
    @Override
     public String toString() {
        return "DHTCounter("+value+")";
    }

}


/**
 * Thread for broadcasting all incoming objects to the list clients.
 */ 

class DHTBroadcaster extends Thread /*implements Runnable*/ {

    private static final Logger logger = Logger.getLogger(DHTBroadcaster.class);
    private final SocketChannel channel;
    private final List bcaster;
    private DHTCounter listElem;
    private final SortedMap theList;


/**
 * DHTBroadcaster.
 * @param s SocketChannel to use.
 * @param bc list of broadcasters.
 * @param le DHTCounter.
 * @param sm SortedMap with key value pairs.
 */
    public DHTBroadcaster(SocketChannel s, 
                          List bc, 
                          DHTCounter le, 
                          SortedMap sm) {
        channel = s;
        bcaster = bc;
        listElem = le;
        theList = sm;
    } 


/**
 * closeChannel.
 */
    public void closeChannel() {
        channel.close();
    }


/**
 * sendChannel.
 * @param n key
 * @param o value
 * @throws IOException
 */
    public void sendChannel(Object n, Object o) throws IOException {
        DHTTransport tc = new DHTTransport(n,o);
        channel.send(tc);
    }


/**
 * sendChannel.
 * @param tc DHTTransport.
 * @throws IOException
 */
    public void sendChannel(DHTTransport tc) throws IOException {
        channel.send(tc);
    }


/**
 * broadcast.
 * @param o DHTTransport element to broadcast.
 */
    public void broadcast(Object o) {
        if ( logger.isDebugEnabled() ) {
           logger.debug("broadcast = "+o);
        }
        DHTTransport tc = null;
        if ( o == null ) {
            return;
        }
        if ( ! (o instanceof DHTTransport) ) {
            return;
        }
        tc = (DHTTransport)o;
        synchronized (theList) {
            //test
            //Object x = theList.get( tc.key );
            //if ( x != null ) {
            //   logger.info("theList duplicate key " + tc.key );
            //}
            theList.put( tc.key, tc.value );
        }
        synchronized (bcaster) {
            Iterator it = bcaster.iterator();
            while ( it.hasNext() ) {
                DHTBroadcaster br = (DHTBroadcaster) it.next();
                try {
                    if ( logger.isDebugEnabled() ) {
                       logger.debug("bcasting to " + br);
                    }
                    br.sendChannel( tc );
                } catch (IOException e) {
                    try { 
                        br.closeChannel();
                        while ( br.isAlive() ) {
                            br.interrupt(); 
                            br.join(100);
                        }
                    } catch (InterruptedException w) { 
                        Thread.currentThread().interrupt();
                    }
                    it.remove( /*br*/ ); //ConcurrentModificationException
                    logger.debug("bcaster.remove() " + br);
                }
            }
        }
    }


/**
 * run.
 */
    @Override
     public void run() {
        Object o;
        boolean goon = true;
        while (goon) {
              try {
                  logger.debug("trying to receive");
                  o = channel.receive();
                  if ( this.isInterrupted() ) {
                      break;
                  }
                  if ( logger.isDebugEnabled() ) {
                     logger.debug("received = "+o);
                  }
                  broadcast(o);
                  if ( this.isInterrupted() ) {
                      goon = false;
                  }
              } catch (IOException e) {
                  goon = false;
                  //e.printStackTrace();
              } catch (ClassNotFoundException e) {
                  goon = false;
                  e.printStackTrace();
              }
        }
        if ( logger.isDebugEnabled() ) {
           logger.debug("DHTBroadcaster terminated "+this);
        }
        channel.close();
    }


/**
 * toString.
 * @return a string representation of this.
 */
    @Override
     public String toString() {
        return "DHTBroadcaster("+channel+","+bcaster.size()+","+listElem+")";
    }

}
