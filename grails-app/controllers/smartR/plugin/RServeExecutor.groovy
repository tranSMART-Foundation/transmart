package smartR.plugin

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RServeExecutor extends ThreadPoolExecutor {

    public RServeExecutor(long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        // keep a threadPool just in case we can extend the multiprocessing later

        // we will execute 1 thread at a time
        super(1, 1, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
       // System.out.println("Perform beforeExecute() logic");
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null) {
            System.out.println("Perform exception handler logic");
        }
      //  System.out.println("Perform afterExecute() logic");
    }

}