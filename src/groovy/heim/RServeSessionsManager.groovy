package heim

import java.util.concurrent.ConcurrentHashMap

class RServeSessionsManager extends ConcurrentHashMap<String, RServeSessionExecutor> {

    public String createNewSession() {
        String uuid = UUID.randomUUID().toString()
        this.put(uuid, new RServeSessionExecutor())
        uuid
    }
}
