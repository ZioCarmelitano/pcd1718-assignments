package pcd.ass04.services.user.repositories;

import pcd.ass04.services.user.model.User;
import pcd.ass04.services.user.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<User> getAll();

    User store(User user);

    Optional<User> get(long userId);

    User update(User user) throws UserNotFoundException;

    void destroy(long userId) throws UserNotFoundException;
}
