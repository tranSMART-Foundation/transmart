package heim

import java.util.UUID;

public class RServeThread implements Runnable
{
    private UUID uuid;
    private String name; //this will be the path name of the R file to run

    public RServeThread(String name) {
        this.uuid = UUID.randomUUID();
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        try {

            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Executing " + uuid + " : " + name );
    }

    @Override
    public String toString() {
        return "smartR.plugin.RServeThread{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                '}';
    }
}
