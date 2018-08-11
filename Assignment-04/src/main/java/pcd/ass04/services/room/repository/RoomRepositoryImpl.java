package pcd.ass04.services.room.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import pcd.ass04.services.room.domain.Room;
import pcd.ass04.services.room.domain.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class RoomRepositoryImpl implements RoomRepository {

    private final AtomicLong counter = new AtomicLong(1);
    private final Map<Room, List<User>> roomMap;

    public RoomRepositoryImpl() {
        roomMap = new HashMap<>();
    }

    @Override
    public Observable<Room> findAll() {
        return Observable.fromIterable(roomMap.keySet());
    }

    @Override
    public Single<Room> findById(Long id) {
        return Single.fromCallable(() -> {
            System.out.println("Map Room: " + this.roomMap);
            final Optional<Room> room = roomMap.keySet().stream()
                    .filter(r -> Objects.equals(r.getId(), id))
                    .findFirst();
            if (room.isPresent()) {
                return room.get();
            } else {
                throw new RuntimeException("Could not find room with ID " + id);
            }
        });
    }

    @Override
    public Single<Long> save(Room room) {
        return Single.fromCallable(() -> {
            if (room.getId() != null) {
                final Room r = findById(room.getId()).blockingGet();
                r.setName(room.getName());
            } else {
                room.setId(counter.getAndIncrement());
                roomMap.put(room, new ArrayList<>());
            }
            return room.getId();
        });
    }

    @Override
    public Single<Long> deleteById(Long id) {
        return Single.fromCallable(() -> {
            final Room room = findById(id).blockingGet();
            roomMap.remove(room);
            return room.getId();
        });
    }

    @Override
    public Completable addUser(Room room, User user) {
        return Completable.fromAction(() -> {
            final List<User> users = roomMap.get(room);
            if (users == null) {
                throw new IllegalArgumentException("Room '" + room.getName() + "' does not exist!");
            }

            users.add(user);
        });
    }

    @Override
    public Single<User> findUserById(Room room, long id) {
        return Single.fromCallable(() -> {
            final List<User> users = roomMap.get(room);
            if (users == null) {
                throw new IllegalArgumentException("Room '" + room.getName() + "' does not exist!");
            }

            Optional<User> user = users.stream()
                    .filter(Predicate.isEqual(id))
                    .findFirst();

            if (user.isPresent()) {
                return user.get();
            } else {
                throw new RuntimeException("Could not find user with ID " + id);
            }
        });
    }

    @Override
    public Completable removeUser(Room room, User user) {
        return Completable.fromAction(() -> {
            final List<User> users = roomMap.get(room);
            if (users == null) {
                throw new IllegalArgumentException("Room '" + room.getName() + "' does not exist!");
            }

            users.add(user);
        });
    }

}
