package dao;

import java.util.List;

public interface GenericDAO<T, F> {
    
    List<T> findAll(F filter);

    T findById(int id);

    boolean insert(T request);

    boolean update(T request);

    boolean delete(int id);
}
