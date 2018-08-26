package pcd.ass04.services.room.repository;

import pcd.ass04.services.room.domain.Room;
import pcd.ass04.services.room.domain.User;
import pcd.ass04.util.AbstractRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class RoomRepositoryImpl extends AbstractRepository<Room, Long> implements RoomRepository {

    private final AtomicLong counter = new AtomicLong(1);
    private final Map<Room, List<User>> roomMap;

    public RoomRepositoryImpl() {
        super();
        roomMap = new HashMap<>();
    }

    @Override
    public Set<Room> findAll() {
        return read(roomMap::keySet);
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

}
