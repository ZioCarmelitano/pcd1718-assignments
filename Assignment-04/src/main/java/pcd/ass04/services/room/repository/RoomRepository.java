package pcd.ass04.services.room.repository;

import pcd.ass04.services.room.domain.Room;
import pcd.ass04.services.room.domain.User;
import pcd.ass04.util.Repository;

import java.util.Optional;

public interface RoomRepository extends Repository<Room, Long> {

    void addUser(Room room, User user);

    Optional<? extends User> findUserById(Room room, long id);

    void removeUser(Room room, User user);

}
