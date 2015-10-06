package smartR.plugin

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


class RServeSessionExecutor {

    BlockingQueue<Runnable> workQueue;

    RServeExecutor executor;

    RServeSessionExecutor() {

        workQueue = new LinkedBlockingQueue<Runnable>();
        executor = new RServeExecutor(5, TimeUnit.MINUTES, workQueue);

        // in case the execution return some error, what to do?!!!!
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                                          ThreadPoolExecutor executorpool) {
                System.out.println("Task Rejected : "
                        + ((RServeThread) r).getUuid());
//                System.out.println("Waiting for a second !!");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("Lets add another time : "
//                        + ((smartR.plugin.RServeThread) r).getUuid());
//                executorpool.execute(r);
            }
        });

    }

    public void execute(Runnable command) {
        executor.execute(command);

    }


    //TODO: add remaining methods
    //public kill


}
