package pcd.ass04.services.room;

final class Channels {

    private static final String ROOM_CHANNEL = "room";

    static final String INDEX = ROOM_CHANNEL + ".index";
    static final String STORE = ROOM_CHANNEL + ".store";
    static final String SHOW = ROOM_CHANNEL + ".show";
    static final String UPDATE = ROOM_CHANNEL + ".update";
    static final String DESTROY = ROOM_CHANNEL + ".destroy";
    static final String JOIN = ROOM_CHANNEL + ".join";
    static final String LEAVE = ROOM_CHANNEL + ".leave";
    static final String MESSAGES = ROOM_CHANNEL + ".messages";
    static final String STATUSCS = ROOM_CHANNEL + ".status";
    static final String EXITCS = ROOM_CHANNEL + ".enterCS";
    static final String ENTERCS = ROOM_CHANNEL + ".exitCS";

}
