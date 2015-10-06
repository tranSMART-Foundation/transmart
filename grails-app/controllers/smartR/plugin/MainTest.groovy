package smartR.plugin

class MainTest {
    public static void main(String[] args) {
            Integer threadCounter = 0;

            RServeSessionsManager mgr = new RServeSessionsManager()
            UUID sess1 = mgr.createNewSession()
            UUID sess2 = mgr.createNewSession()

            while (true) {
                threadCounter++;

                // Adding threads one by one
                def thread1 = new RServeThread("heatmap/init"+threadCounter.toString() )

                //thread1 UUID is the process id
                print(sess1)
                println( ">> " + thread1)

                //this will put the thread in the queue (async execute)
                mgr.get(sess1).execute(thread1);

                def thread2 = new RServeThread("heatmap/init"+threadCounter.toString())
                print(sess2)
                println(' >> ' + thread2)
                mgr.get(sess2).execute(thread2);

                if (threadCounter % 20 ==0)
                    Thread.sleep(15000);
            }
    }
}
