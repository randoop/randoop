/*
 * $Id: DistHashTable.java 1663 2008-02-05 17:32:07Z kredel $
 */

package edu.jas.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
//import java.util.Map;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

//import edu.unima.ky.parallel.ChannelFactory;
//import edu.unima.ky.parallel.SocketChannel;


/**
 * Distributed version of a HashTable.
 * Implemented with a SortedMap / TreeMap to keep the sequence 
 * order of elements.
 * @author Heinz Kredel
 */

public class DistHashTable /* implements Map not jet */ {

    private static final Logger logger = Logger.getLogger(DistHashTable.class);

    protected final SortedMap theList;
    protected final ChannelFactory cf;
    protected SocketChannel channel = null;
    protected DHTListener listener = null;


/**
 * Constructs a new DistHashTable.
 * @param host name or IP of server host.
 */ 
    public DistHashTable(String host) {
        this(host,DistHashTableServer.DEFAULT_PORT);
    }


/**
 * DistHashTable.
 * @param host name or IP of server host.
 * @param port on server.
 */
    public DistHashTable(String host,int port) {
        this(new ChannelFactory(port+1),host,port);
    }


/**
 * DistHashTable.
 * @param cf ChannelFactory to use.
 * @param host name or IP of server host.
 * @param port on server.
 */
    public DistHashTable(ChannelFactory cf,String host,int port) {
        this.cf = cf;
        try {
            channel = cf.getChannel(host,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ( logger.isDebugEnabled() ) {
           logger.debug("dl channel = " + channel);
        }
        theList = new TreeMap();
        listener = new DHTListener(channel,theList);
        listener.start();
    }


/**
 * DistHashTable.
 * @param sc SocketChannel to use.
 */
    public DistHashTable(SocketChannel sc) {
        cf = null;
        channel = sc;
        theList = new TreeMap();
        listener = new DHTListener(channel,theList);
        listener.start();
    }


/**
 * Get the values as Collection.
 */ 
    public Collection values() {
        return theList.values();
    }


/**
 * Get the keys as set.
 */ 
    public Set keySet() {
        return theList.keySet();
    }


/**
 * Get the internal list, convert from Collection.
 * @fix but is ok
 */ 
    public ArrayList getArrayList() {
        synchronized ( theList ) {
           return new ArrayList( theList.values() );
        }
    }


/**
 * Get the internal sorted map.
 * For synchronization purpose in normalform.
 */ 
    public SortedMap getList() {
        return theList;
    }


/**
 * Size of the (local) list.
 */ 
    public int size() {
        synchronized ( theList ) {
           return theList.size();
        }
    }


/**
 * Is the List empty?
 */ 
    public boolean isEmpty() {
        synchronized ( theList ) {
           return theList.isEmpty();
        }
    }


/**
 * List key iterator.
 */ 
    public Iterator iterator() {
        synchronized ( theList ) {
           return theList.keySet().iterator();
        }
    }


/**
 * List value iterator.
 */ 
    public Iterator valueIterator() {
        synchronized ( theList ) {
           return theList.values().iterator();
        }
    }


/**
 * Put object to the distributed hash table.
 * Blocks until the key value pair is send and received 
 * from the server.
 * @param key
 * @param value
 */
    public void putWait(Object key, Object value) {
        put(key,value); // = send
        try {
            synchronized ( theList ) {
               while ( ! value.equals( theList.get(key) ) ) {
                  //System.out.print("#");
                  theList.wait(100);
               }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }


/**
 * Put object to the distributed hash table.
 * Returns immediately after sending does not block.
 * @param key
 * @param value
 */
    public void put(Object key, Object value) {
        if ( key == null || value == null ) {
           throw new NullPointerException("null keys or values not allowed");
        }
        DHTTransport tc = new DHTTransport(key,value);
        try {
            channel.send(tc);
            //System.out.println("send: "+tc+" @ "+listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/**
 * Get value under key from DHT.
 * Blocks until the object is send and received from the server
 * (actually it blocks until some value under key is received).
 * @param key
 * @return the value stored under the key.
 */
    public Object getWait(Object key) {
        Object value = null;
        try {
            synchronized ( theList ) {
               value = theList.get(key);
               while ( value == null ) {
                   //System.out.print("^");
                   theList.wait(100);
                   value = theList.get(key);
               }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        return value;
    }


/**
 * Get value under key from DHT.
 * If no value is jet available null is returned. 
 * @param key
 * @return the value stored under the key.
 */
    public Object get(Object key) {
        synchronized ( theList ) {
           return theList.get(key);
        }
    }


/**
 * Clear the List.
 * Caveat: must be called on all clients.
 */ 
    public void clear() {
        // send clear message to others
        synchronized ( theList) {
           theList.clear();
        }
    }


/**
 * Terminate the list thread.
 */ 
    public void terminate() {
        if ( cf != null ) {
           cf.terminate();
        }
        if ( channel != null ) {
           channel.close();
        }
        //theList.clear();
        if ( listener == null ) { 
           return;
        }
        if ( logger.isDebugEnabled() ) {
           logger.debug("terminate " + listener);
        }
        listener.setDone(); 
        try { 
            while ( listener.isAlive() ) {
                  //System.out.print("+");
                  listener.interrupt(); 
                  listener.join(100);
            }
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt();
        }
        listener = null;
    }

}


/**
 * Thread to comunicate with the list server.
 */

class DHTListener extends Thread {

    private static final Logger logger = Logger.getLogger(DHTListener.class);

    private final SocketChannel channel;
    private final SortedMap theList;
    private boolean goon;


    DHTListener(SocketChannel s, SortedMap list) {
        channel = s;
        theList = list;
    } 


    void setDone() {
        goon = false;
    }

/**
 * run.
 */
    @Override
     public void run() {
        Object o;
        DHTTransport tc;
        goon = true;
        while (goon) {
            tc = null;
            o = null;
            try {
                o = channel.receive();
                if ( logger.isDebugEnabled() ) {
                   logger.debug("receive("+o+")");
                }
                if ( this.isInterrupted() ) {
                   goon = false;
                   break;
                } 
                if ( o == null ) {
                   goon = false;
                   break;
                }
                if ( o instanceof DHTTransport ) {
                   tc = (DHTTransport)o;
                   if ( tc.key != null ) {
                      //logger.debug("receive, put(" + tc + ")");
                      synchronized ( theList ) {
                         theList.put( tc.key, tc.value );
                         theList.notify();
                      }
                   }
                }
            } catch (IOException e) {
                goon = false;
                //e.printStackTrace();
            } catch (ClassNotFoundException e) {
                goon = false;
                e.printStackTrace();
            }
        }
    }

}
