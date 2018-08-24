package pcd.ass04.services.user.model;

import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class User {

    private long id;
    private String name;

    public User(String name) {
        this.name = name;
    }

    public User(long id, String name) {
        this.id = id;
        this.name = name;
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
                Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("name", name);
    }

    public void setId(long id) {
        this.id = id;
    }
}
