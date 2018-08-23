package pcd.ass04.util;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface Repository<T, ID> {

    Observable<T> findAll();

    Single<T> findById(ID id);

    Single<ID> save(T t);

    Single<ID> deleteById(ID id);

}
