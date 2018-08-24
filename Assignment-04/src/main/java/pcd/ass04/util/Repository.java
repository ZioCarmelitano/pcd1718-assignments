package pcd.ass04.util;

import java.util.Collection;
import java.util.Optional;

public interface Repository<T, ID> {

    <I extends Collection<? extends T>> I findAll();

    Optional<? extends T> findById(ID id);

    ID save(T t);

    ID deleteById(ID id);

}
