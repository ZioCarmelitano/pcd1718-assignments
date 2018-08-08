package pcd.ass04.services.room.repository;

import io.reactivex.Completable;
import io.reactivex.Single;
import pcd.ass04.services.room.domain.Room;
import pcd.ass04.services.room.domain.User;
import pcd.ass04.util.Repository;

public interface RoomRepository extends Repository<Room, Long> {

    Completable addUser(Room room, User user);

    Single<User> findUserById(Room room, long id);

    Completable removeUser(Room room, User user);

}
