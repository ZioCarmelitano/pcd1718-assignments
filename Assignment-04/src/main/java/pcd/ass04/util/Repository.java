package pcd.ass04.util;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {

    List<T> findAll();

    Optional<? extends T> findById(ID id);

    ID save(T t);

    void deleteById(ID id);

}
