/*
 * $Id: ComputerThreads.java 1249 2007-07-29 10:05:58Z kredel $
 */

package edu.jas.kern;


import java.util.List;

//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.Future;
//import java.util.concurrent.Callable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


/**
 * ComputerThreads,
 * provides global thread / executor service.
 * @author Heinz Kredel
 * @usage To obtain a reference to the thread pool use 
 * <code>ComputerThreads.getPool()</code>. 
 * Once a pool has been created it must be shutdown to exit JAS with
 * <code>ComputerThreads.terminate()</code>. 
 */

public class ComputerThreads {

    private static final Logger logger = Logger.getLogger(ComputerThreads.class);
    // private boolean debug = logger.isInfoEnabled(); //logger.isInfoEnabled();


    /**
      * Number of processors.
      */
    public static final int N_CPUS = Runtime.getRuntime().availableProcessors();


    /**
      * Maximal number of threads.
      * N_CPUS x 1.5, x 2, x 2.5, min 3, ?.
      */
    public static final int N_THREADS = ( N_CPUS < 3 ? 3 : N_CPUS + N_CPUS/2 );


    /**
      * Queue capacity.
      */
    public static final int Q_CAPACITY = 1000; // 10000


    /**
      * WorkQueue.
      */
    private static BlockingQueue<Runnable> workpile; 


    /**
      * Saturation policy.
      */
    public static final RejectedExecutionHandler REH = new ThreadPoolExecutor.CallerRunsPolicy();


    /**
      * Thread pool.
      */
    static ThreadPoolExecutor pool = null;


    /**
     * No public constructor.
     */
    private ComputerThreads() {
    }


    /**
     * Test if a pool is running.
     * @return true if a thread pool has been started or is running, else false.
     */
    public static synchronized boolean isRunning() {
        if ( pool == null ) {
            return false;
        }
        if ( pool.isTerminated() || pool.isTerminating() ) {
            return false;
        }
        return true;
    }


    /**
     * Get the thread pool.
     * @return pool ExecutorService.
     */
    public static synchronized ExecutorService getPool() {
        if ( pool == null ) {
            workpile = new ArrayBlockingQueue<Runnable>(Q_CAPACITY);
            pool = new ThreadPoolExecutor(N_CPUS, N_THREADS,
                                          100L, TimeUnit.MILLISECONDS,
                                          workpile, REH);
        }
        return Executors.unconfigurableExecutorService(pool);

            /* not useful, is not run from jython
            final GCDProxy<C> proxy = this;
            Runtime.getRuntime().addShutdownHook( 
                             new Thread() {
                                 public void run() {
                                        logger.info("running shutdown hook");
                                        proxy.terminate();
                                 }
                             }
            );
            */
    }


    /**
     * Stop execution.
     */
    public static synchronized void terminate() {
        if ( pool == null ) {
           return;
        }
        logger.info("number of CPUs            " + N_CPUS);
        logger.info("maximal number of threads " + N_THREADS);
        logger.info("task queue size           " + Q_CAPACITY);
        logger.info("reject execution handler  " + REH.getClass().getName());
        if ( workpile != null ) {
           logger.info("there are " + workpile.size() + " queued tasks ");
        }
        List<Runnable> r = pool.shutdownNow();
        if ( r.size() != 0 ) {
           logger.info("there are " + r.size() + " unfinished tasks ");
        }
        logger.info("maximal number of active threads " + pool.getLargestPoolSize());
        logger.info("number of sheduled tasks         " + pool.getTaskCount());
        logger.info("number of completed tasks        " + pool.getCompletedTaskCount());
        pool = null;
        workpile = null;
    }

}
