package pcd.ass04.services.user.repositories;

import pcd.ass04.services.user.exceptions.UserNotFoundException;
import pcd.ass04.services.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryUserRepository implements UserRepository {

    private final List<User> users = new ArrayList<>();
    private long idCounter = 0L;

    private static volatile InMemoryUserRepository instance;
    private static final Object mutex = new Object();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock(true);

    private InMemoryUserRepository() {
    }

    @Override
    public List<User> getAll() {
        rwLock.readLock().lock();
        try {
            return users;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public User store(User user) {
        rwLock.writeLock().lock();
        try {
            this.idCounter++;
            user.setId(idCounter);
            this.users.add(user);
            return user;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Optional<User> get(long userId) {
        rwLock.readLock().lock();
        try {
            return users.stream().filter(u -> u.getId() == userId).findFirst();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public User update(User user) throws UserNotFoundException {
        rwLock.writeLock().lock();
        try {
            Optional<User> userToUpdate = findUser(user.getId());
            if (userToUpdate.isPresent()) {
                userToUpdate.get().setUsername(user.getUsername());
                return userToUpdate.get();
            } else {
                throw new UserNotFoundException();
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void destroy(long userId) throws UserNotFoundException {
        rwLock.writeLock().lock();
        try {
            Optional<User> userToUpdate = findUser(userId);
            if (userToUpdate.isPresent()) {
                this.users.remove(userToUpdate.get());
            } else {
                throw new UserNotFoundException();
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private Optional<User> findUser(long userId) {
        rwLock.readLock().lock();
        try {
            return this.users.stream().filter(u -> u.getId() == userId).findFirst();
        } finally {
            rwLock.readLock().unlock();
        }
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
