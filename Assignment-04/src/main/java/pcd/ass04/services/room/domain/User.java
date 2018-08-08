package pcd.ass04.services.room.domain;

import io.vertx.core.json.JsonObject;
import pcd.ass04.util.JsonHelper;

import java.util.Objects;

public class User {

    private Long id;
    private String name;

    public static User fromJson(JsonObject json) {
        final User user = new User();

        JsonHelper.ifPresentLong(json, "id", user::setId);
        JsonHelper.ifPresentString(json, "name", user::setName);

        return user;
    }

    public User() {
    }

    public User(long id, String name) {
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
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId()) &&
                Objects.equals(getName(), user.getName());
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
