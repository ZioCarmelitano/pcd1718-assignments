package pcd.ass04.services.user.model;

import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class User {

    private final long id;
    private String username;

    public User(long id, String username) {
        this.id = id;
        this.username = username;
    }

    public User(JsonObject user) {
        this.id = user.getLong("id");
        this.username = user.getString("username");
    }

    public long getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("username", username);
    }
}
