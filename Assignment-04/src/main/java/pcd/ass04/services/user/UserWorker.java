package pcd.ass04.services.user;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import pcd.ass04.ServiceVerticle;
import pcd.ass04.services.user.exceptions.UserNotFoundException;
import pcd.ass04.services.user.model.User;
import pcd.ass04.services.user.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static pcd.ass04.services.user.Channels.*;

final class UserWorker extends ServiceVerticle {

    private final UserRepository userRepository;

    UserWorker(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void start() {

        eventBus.<JsonObject>consumer(INDEX, msg -> {
            List<User> users = this.userRepository.getAll();
            JsonArray userArray = new JsonArray();
            users.stream()
                    .map(User::toJson)
                    .forEach(userArray::add);
            msg.reply(userArray);
        });


        eventBus.<JsonObject>consumer(STORE, msg -> {
            final JsonObject body = msg.body();
            final JsonObject userToStoreJson = body.getJsonObject("request");

            System.out.println("Storing: " + userToStoreJson);
            User userStored = this.userRepository.store(new User(userToStoreJson.getString("name")));

            msg.reply(userStored.toJson());
        });


        eventBus.<JsonObject>consumer(SHOW, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");

            final long userId = Long.parseLong(params.getString("id"));

            Optional<User> user = this.userRepository.get(userId);
            if (user.isPresent()) {
                msg.reply(user.get().toJson());
            } else {
                msg.fail(NOT_FOUND.code(), "User to show not found");
            }
        });


        eventBus.<JsonObject>consumer(UPDATE, msg -> {
            final JsonObject body = msg.body();
            final JsonObject userToUpdateJson = body.getJsonObject("request");
            final JsonObject params = body.getJsonObject("params");

            long userToUpdateId = Long.parseLong(params.getString("id"));
            try {
                User userUpdated = this.userRepository.update(new User(userToUpdateId, userToUpdateJson.getString("name")));
                msg.reply(userUpdated.toJson());
            } catch (UserNotFoundException e) {
                msg.fail(NOT_FOUND.code(), "User to update not found");
            }
        });


        eventBus.<JsonObject>consumer(DESTROY, msg -> {
            final JsonObject body = msg.body();
            final JsonObject params = body.getJsonObject("params");
            final long userId = Long.parseLong(params.getString("id"));

            try {
                this.userRepository.destroy(userId);
                msg.reply(new JsonObject().put("id", userId));
            } catch (UserNotFoundException e) {
                msg.fail(NOT_FOUND.code(), "User to delete not found");
            }
        });
    }


}
