package pcd.ass04.services.webapp;

final class Channels {
    private static final String WEBAPP_CHANNEL = "webapp";

    static final String NEW_USER = WEBAPP_CHANNEL + ".user";
    static final String DELETE_USER = WEBAPP_CHANNEL + ".deleteUser";
    static final String ROOMS = WEBAPP_CHANNEL + ".rooms";
    static final String NEW_ROOM = WEBAPP_CHANNEL + ".room";
    static final String ROOM = WEBAPP_CHANNEL + ".getRoom";
    static final String DELETE_ROOM= WEBAPP_CHANNEL + ".deleteRoom";
    static final String JOIN = WEBAPP_CHANNEL + ".joinRoom";
    static final String LEAVE = WEBAPP_CHANNEL + ".leaveRoom";
    static final String MESSAGES = WEBAPP_CHANNEL + ".message";
    static final String ENTERCS = WEBAPP_CHANNEL + ".enterCS";
    static final String EXITCS = WEBAPP_CHANNEL + ".exitCS";
}
