package pcd.ass04.services.room.domain;

import io.vertx.core.json.JsonObject;
import pcd.ass04.util.JsonHelper;

import java.util.Objects;

public class Room {

    private Long id;
    private String name;

    public static Room fromJson(JsonObject json) {
        final Room room = new Room();

        JsonHelper.ifPresentLong(json, "id", room::setId);
        JsonHelper.ifPresentString(json, "name", room::setName);

        return room;
    }

    public Room() {
    }

    public Room(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return getId() == room.getId() &&
                Objects.equals(getName(), room.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        if (id != null) {
            json.put("id", getId());
        }

        if (name != null) {
            json.put("name", getName());
        }

        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

}
