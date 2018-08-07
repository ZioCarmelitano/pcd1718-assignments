package pcd.ass04.services.user.repositories;

import pcd.ass04.services.user.model.User;
import pcd.ass04.services.user.exceptions.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {

    private List<User> users = new ArrayList<>();

    private static volatile InMemoryUserRepository instance;
    private static final Object mutex = new Object();

    private InMemoryUserRepository(){}

    @Override
    public synchronized List<User> getAll() {
        return users;
    }

    @Override
    public synchronized void store(User user) {
        this.users.add(user);
    }

    @Override
    public synchronized Optional<User> get(long userId) {
        return users.stream().filter(u -> u.getId() == userId).findFirst();
    }

    @Override
    public synchronized void update(User user) throws UserNotFoundException {
        Optional<User> userToUpdate = findUser(user.getId());
        if (userToUpdate.isPresent()) {
            userToUpdate.get().setUsername(user.getUsername());
        } else {
            throw new UserNotFoundException();
        }
    }

    @Override
    public synchronized void destroy(long userId) throws UserNotFoundException {
        Optional<User> userToUpdate = findUser(userId);
        if (userToUpdate.isPresent()) {
            this.users.remove(userToUpdate.get());
        } else {
            throw new UserNotFoundException();
        }
    }

    private Optional<User> findUser(long userId) {
        return this.users.stream().filter(u -> u.getId() == userId).findFirst();
    }

    public static InMemoryUserRepository getInstance() {
        InMemoryUserRepository result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new InMemoryUserRepository();
            }
        }
        return result;
    }
}
