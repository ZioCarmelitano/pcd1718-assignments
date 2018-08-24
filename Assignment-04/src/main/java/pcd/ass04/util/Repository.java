package pcd.ass04.util;

import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.Collection;
import java.util.Optional;

public interface Repository<T, ID> {

    <I extends Collection<? extends T>> I findAll();

    Optional<? extends T> findById(ID id);

    ID save(T t);

    ID deleteById(ID id);

}
