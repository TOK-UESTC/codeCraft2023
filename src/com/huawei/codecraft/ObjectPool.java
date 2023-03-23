import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ObjectPool<T> {
    private final List<T> freeObjects;
    private final Set<T> used;
    private final Function supply;

    public ObjectPool(ObjectFactory<T> objectFactory) {
        this.freeObjects = new ArrayList<>();
        this.objectFactory = objectFactory;
    }

    public T acquire() {
        if (freeObjects.isEmpty()) {
            return objectFactory.createNew();
        } else {
            return freeObjects.remove(freeObjects.size() - 1);
        }
    }

    public void release(T object) {
        freeObjects.add(object);
    }

    public interface ObjectFactory<T> {
        T createNew();
    }
}