package pcd.ass04.services.user.repositories;

import pcd.ass04.services.user.model.User;
import pcd.ass04.services.user.exceptions.UserNotFoundException;
import pcd.ass04.util.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {

    List<User> getAllUsers();

    User storeUser(User user);

    Optional<User> getUser(long userId);

    User updateUser(User user) throws UserNotFoundException;

    Long destroyUser(long userId) throws UserNotFoundException;
}
