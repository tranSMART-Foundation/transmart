package heim

class RServeSessionsManager extends HashMap<UUID, RServeSessionExecutor> {

    public UUID createNewSession() {
        UUID uuid = UUID.randomUUID();
        this.put( uuid, new RServeSessionExecutor() );
        return uuid;
    }
}
