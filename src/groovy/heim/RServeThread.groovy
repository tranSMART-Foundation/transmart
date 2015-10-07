package heim

import java.util.UUID;

public class RServeThread implements Runnable
{
    private UUID uuid;
    private String workflow
    private String scriptName
    private String name  //this will be the path name of the R file to run

    public RServeThread(String workflow,String scriptName) {
        this.uuid = UUID.randomUUID();
        this.workflow = workflow
        this.scriptName = scriptName
        this.name = workflow+'/'+scriptName
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
            ScriptManager.executeRScript(workflow,scriptName)
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
