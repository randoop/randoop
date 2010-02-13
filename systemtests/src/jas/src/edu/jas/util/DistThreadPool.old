/*
 * $Id: DistThreadPool.java 1787 2008-05-15 20:17:28Z kredel $
 */

package edu.jas.util;

import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.LinkedList;

import org.apache.log4j.Logger;


/**
 * Distributed thread pool.
 * Using stack / list workpile and Executable Channels and Servers.
 * @author Heinz Kredel
 */

public class DistThreadPool /*extends ThreadPool*/ {

    /**
     * machine file to use.
     */
    private final String mfile;


    /**
     * default machine file for test.
     */
    private final static String DEFAULT_MFILE = ExecutableChannels.DEFAULT_MFILE;


    /**
     * Number of threads to use.
     */
    protected final int threads;


    /**
     * Default number of threads to use.
     */
    static final int DEFAULT_SIZE = 3;


    /**
     * Channels to remote executable servers.
     */
    final ExecutableChannels ec;


    /**
     * Array of workers.
     */
    protected final DistPoolThread[] workers;


    /**
     * Number of idle workers.
     */
    protected int idleworkers = 0; 


    /**
     * Work queue / stack.
     */
    // should be expressed using strategy pattern
    // List or Collection is not appropriate
                                             // LIFO strategy for recursion
    protected LinkedList<Runnable> jobstack; // FIFO strategy for GB


    protected StrategyEnumeration strategy = StrategyEnumeration.LIFO;


    private static final Logger logger = Logger.getLogger(DistThreadPool.class);
    private static boolean debug = logger.isDebugEnabled();


    /**
     * Constructs a new DistThreadPool
     * with strategy StrategyEnumeration.FIFO
     * and size DEFAULT_SIZE.
     */ 
    public DistThreadPool() {
        this(StrategyEnumeration.FIFO,DEFAULT_SIZE,null);
    }


   /**
    * Constructs a new DistThreadPool
    * with size DEFAULT_SIZE.
    * @param strategy for job processing.
    */ 
    public DistThreadPool(StrategyEnumeration strategy) {
        this(strategy,DEFAULT_SIZE,null);
    }


   /**
    * Constructs a new DistThreadPool
    * with strategy StrategyEnumeration.FIFO.
    * @param size of the pool.
    */ 
    public DistThreadPool(int size) {
        this(StrategyEnumeration.FIFO,size,null);
    }


   /**
    * Constructs a new DistThreadPool
    * with strategy StrategyEnumeration.FIFO.
    * @param size of the pool.
    * @param mfile machine file.
    */ 
    public DistThreadPool(int size, String mfile) {
        this(StrategyEnumeration.FIFO,size,mfile);
    }


   /**
    * Constructs a new DistThreadPool.
    * @param strategy for job processing.
    * @param size of the pool.
    * @param mfile machine file.
    */ 
    public DistThreadPool(StrategyEnumeration strategy, int size, String mfile) {
        this.strategy = strategy;
        if ( size < 0 ) {
           this.threads = 0;
         } else {
           this.threads = size;
         }
        if ( mfile == null || mfile.length() == 0 ) {
           this.mfile = DEFAULT_MFILE;
         } else {
           this.mfile = mfile;
         }
        jobstack = new LinkedList<Runnable>(); // ok for all strategies ?
        try {
            ec = new ExecutableChannels( this.mfile );
        } catch (FileNotFoundException e) {
            // ec = null;
            e.printStackTrace();
            throw new RuntimeException("DistThreadPool " +e);
        }
        if ( debug ) {
           logger.debug("ExecutableChannels = " + ec);
        }
        try {
            ec.open(threads);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("DistThreadPool " +e);
        }
        if ( debug ) {
           logger.debug("ExecutableChannels = " + ec);
        }
        workers = new DistPoolThread[threads];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new DistPoolThread(this,ec,i);
            workers[i].start();
        }
        logger.info("strategy = " + strategy);
    }


   /**
    * number of worker threads.
    */
    public int getNumber() {
       return workers.length; // not null
    }


   /**
    * get used strategy.
    */
    public StrategyEnumeration getStrategy() {
       return strategy; 
    }


   /**
    * the used executable channel.
    */
    public ExecutableChannels getEC() {
       return ec; // not null
    }


   /**
    * Terminates the threads.
    * @param shutDown true, if shut-down of the 
    * remote executable servers is requested, 
    * false, if remote executable servers stay alive.
    */
    public void terminate(boolean shutDown) {
        if ( shutDown ) {
           ShutdownRequest sdr = new ShutdownRequest();
           for (int i = 0; i < workers.length; i++) {
               addJob( sdr );
           }
        }
        terminate();
    }


   /**
    * Terminates the threads.
    */
    public void terminate() {
        while ( hasJobs() ) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        for (int i = 0; i < workers.length; i++) {
            try { 
                while ( workers[i].isAlive() ) {
                        workers[i].interrupt(); 
                        workers[i].join(100);
                }
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt();
            }
        }
        ec.close();
    }


   /**
    * adds a job to the workpile.
    * @param job
    */
    public synchronized void addJob(Runnable job) {
        jobstack.addLast(job);
        logger.debug("adding job" );
        if (idleworkers > 0) {
            logger.debug("notifying a jobless worker");
            notify();
        }
    }


   /**
    * get a job for processing.
    */
    protected synchronized Runnable getJob() throws InterruptedException {
        while (jobstack.isEmpty()) {
            idleworkers++;
            logger.debug("waiting");
            wait();
            idleworkers--;
        }
        // is expressed using strategy enumeration
        if (strategy == StrategyEnumeration.LIFO) { 
             return jobstack.removeLast(); // LIFO
        } else {
             return jobstack.removeFirst(); // FIFO
        }
    }


   /**
    * check if there are jobs for processing.
    */
    public boolean hasJobs() {
        if ( jobstack.size() > 0 ) {
            return true;
        }
        for (int i = 0; i < workers.length; i++) {
            if ( workers[i].working ) {
               return true;
            }
        }
        return false;
    }


   /**
    * check if there are more than n jobs for processing.
    * @param n Integer
    * @return true, if there are possibly more than n jobs. 
    */
    public boolean hasJobs(int n) {
        int j = jobstack.size();
        if ( j > 0 && ( j + workers.length > n ) ) {
           return true;
           // if j > 0 no worker should be idle
           // ( ( j > 0 && ( j+workers.length > n ) ) || ( j > n )
        }
        int x = 0;
        for (int i=0; i < workers.length; i++) {
            if ( workers[i].working ) {
               x++;
            }
        }
        if ( (j + x) > n ) {
           return true;
        }
        return false;
    }

}


/**
 * Implements a shutdown task.
 */
class ShutdownRequest implements Runnable {
  /**
   * Run the thread.
   */
   public void run() {
       System.out.println("ShutdownRequest");
   }
}


/**
 * Implements one local part of the distributed thread.
 */
class DistPoolThread extends Thread {

    final DistThreadPool pool;

    final ExecutableChannels ec;

    final int myId;

    private static final Logger logger = Logger.getLogger(DistPoolThread.class);
    private static boolean debug = logger.isDebugEnabled();

    boolean working = false;


  /**
   * @param pool DistThreadPool.
   */
   public DistPoolThread(DistThreadPool pool,ExecutableChannels ec,int i) {
       this.pool = pool;
       this.ec = ec;
       myId = i;
   }


  /**
   * Run the thread.
   */
   @Override
public void run() {
        logger.info( "ready" );
        Runnable job;
        int done = 0;
        long time = 0;
        long t;
        boolean running = true;
        while (running) {
            try {
                logger.debug( "looking for a job" );
                job = pool.getJob();
                working = true;
                if ( debug ) {
                   logger.info( "working on " + job);
                }
                t = System.currentTimeMillis();
                // send and wait, like rmi
                try {
              if ( job instanceof ShutdownRequest ) {
                       ec.send( myId, ExecutableServer.STOP );
              } else {
                       ec.send( myId, job );
              }
                } catch (IOException e) {
                    e.printStackTrace();
                    working = false;
                }
                //job.run(); 
                Object o = null;
                try {
                    if ( working ) {
                       o = ec.receive( myId );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    running = false; 
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    running = false; 
                }
                working = false;
                time += System.currentTimeMillis() - t;
                done++;
                if ( debug ) {
                   logger.info( "done with " + o);
                }
            } catch (InterruptedException e) { 
                running = false; 
                Thread.currentThread().interrupt();
            }
        }
        logger.info( "terminated, done " + done + " jobs in " 
                        + time + " milliseconds");
   }

}
