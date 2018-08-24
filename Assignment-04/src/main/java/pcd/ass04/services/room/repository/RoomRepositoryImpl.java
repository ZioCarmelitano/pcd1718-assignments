package pcd.ass04.services.room.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import pcd.ass04.services.room.domain.Room;
import pcd.ass04.services.room.domain.User;
import pcd.ass04.util.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class RoomRepositoryImpl implements RoomRepository {

    private final AtomicLong counter = new AtomicLong(1);
    private final Map<Room, List<User>> roomMap;

    private final Lock readLock;
    private final Lock writeLock;

    public RoomRepositoryImpl() {
        roomMap = new HashMap<>();

        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
    }

    @Override
    public Set<Room> findAll() {
        return read(() ->  roomMap.keySet());
    }

    @Override
    public Optional<Room> findById(Long id) {
        return read(() -> {
            System.out.println("Map Room: " + this.roomMap);
            return roomMap.keySet().stream()
                    .filter(r -> Objects.equals(r.getId(), id))
                    .findFirst();
        });
    }

    @Override
    public Long save(Room room) {
        return write(() -> {
            if (room.getId() != null) {
                findById(room.getId())
                        .ifPresent(r -> r.setName(room.getName()));
            } else {
                room.setId(counter.getAndIncrement());
                roomMap.put(room, new ArrayList<>());
            }
            return room.getId();
        });
    }

    @Override
    public Long deleteById(Long id) {
        return write(() -> {
            final Room room = findById(id).get();
            roomMap.remove(room);
            return room.getId();
        });
    }

    @Override
    public void addUser(Room room, User user) {
        write(() -> {
            final List<User> users = roomMap.get(room);
            if (users == null) {
                throw new IllegalArgumentException("Room '" + room.getName() + "' does not exist!");
            }
            users.add(user);
        });
    }

    @Override
    public Optional<User> findUserById(Room room, long id) {
        return read(() -> {
            final List<User> users = roomMap.get(room);
            if (users == null) {
                throw new IllegalArgumentException("Room '" + room.getName() + "' does not exist!");
            }

            return users.stream()
                    .filter(u -> Objects.equals(u.getId(), id))
                    .findFirst();
        });
    }

    @Override
    public void removeUser(Room room, User user) {
        write(() -> {
            final List<User> users = roomMap.get(room);
            if (users == null) {
                throw new IllegalArgumentException("Room '" + room.getName() + "' does not exist!");
            }

            users.remove(user);
        });
    }

    private <T> T read(Supplier<? extends T> operation) {
        return Utils.get(readLock, operation);
    }

    private void write(Runnable operation) {
        write(() -> {
            operation.run();
            return null;
        });
    }

    private <T> T write(Supplier<? extends T> operation) {
        return Utils.get(writeLock, operation);
    }

}
