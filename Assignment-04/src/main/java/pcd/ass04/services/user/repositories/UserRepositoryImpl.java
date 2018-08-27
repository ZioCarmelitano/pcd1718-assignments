package pcd.ass04.services.user.repositories;

import pcd.ass04.services.user.exceptions.UserNotFoundException;
import pcd.ass04.services.user.model.User;
import pcd.ass04.util.AbstractRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserRepositoryImpl extends AbstractRepository<User, Long> implements UserRepository {

    private final List<User> users = new ArrayList<>();
    private long idCounter = 0L;

    private static volatile UserRepositoryImpl instance;
    private static final Object mutex = new Object();

    private UserRepositoryImpl() {
    }

    @Override
    public List<User> getAllUsers() {
        return read(() -> users);
    }

    @Override
    public User storeUser(User user) {
        return write(() -> {
            this.idCounter++;
            user.setId(idCounter);
            this.users.add(user);
            return user;
        });
    }

    @Override
    public Optional<User> getUser(long userId) {
        return read(() -> users.stream().filter(u -> u.getId() == userId).findFirst());
    }

    @Override
    public User updateUser(User user) throws UserNotFoundException {
        Optional<User> userUpdated = write(() -> {
            Optional<User> userToUpdate = findUser(user.getId());
            userToUpdate.ifPresent(u -> u.setName(user.getName()));
            return userToUpdate;
        });
        return userUpdated.orElseThrow(UserNotFoundException::new);
    }

    @Override
    public Long destroyUser(long userId) throws UserNotFoundException {
        Optional<User> userFound = write(() -> {
            Optional<User> userToUpdate = findUser(userId);
            userToUpdate.ifPresent(this.users::remove);
            return userToUpdate;
        });
        return userFound.map(User::getId)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<User> findAll() {
        return getAllUsers();
    }

    @Override
    public Optional<? extends User> findById(Long userId) {
        return findUser(userId);
    }

    @Override
    public Long save(User user) {
        return storeUser(user).getId();
    }

    @Override
    public void deleteById(Long userId) {
        try {
            destroyUser(userId);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static UserRepositoryImpl getInstance() {
        UserRepositoryImpl result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new UserRepositoryImpl();
            }
        }
        return result;
    }

    private Optional<User> findUser(long userId) {
        return read(() -> this.users.stream().filter(u -> u.getId() == userId).findFirst());
    }

}
