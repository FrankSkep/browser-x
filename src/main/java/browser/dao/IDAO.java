package browser.dao;
import browser.data_structure.LinkedList;

public interface IDAO<T> {
    void save(T entity);

    void delete(T entity);

    void deleteAll();

    LinkedList<T> getAll();
}