package heim

public class RServeThread implements Runnable
{
    private UUID uuid;
    private String workflow
    private String scriptName
    private String name  //this will be the path name of the R file to run


    ScriptManagerService scriptManagerService
    public RServeThread(String workflow,String scriptName,ScriptManagerService service) {
        this.uuid = UUID.randomUUID();
        this.workflow = workflow
        this.scriptName = scriptName
        this.name = workflow+'/'+scriptName
        this.scriptManagerService =  service
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
            scriptManagerService.executeRScript(workflow,scriptName)
            //TODO: Handle R output
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
