package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final List<T> pool;
    private final Set<T> used;
    private final Supplier<T> supplier;

    public ObjectPool(int initCount, Supplier<T> supplier) {
        this.supplier = supplier;
        pool = new ArrayList<>();
        used = new HashSet<>();

        // 先开辟池子，避免冷启动过慢
        for (int i = 0; i < initCount; i++) {
            pool.add(supplier.get());
        }
    }

    public T acquire() {
        T t;
        if (pool.isEmpty()) {
            t = supplier.get();
        } else {
            // 从池中取出，放到使用池中
            t = pool.remove(pool.size() - 1);
        }
        used.add(t);
        return t;
    }

    public void release(T t) {
        if (used.remove(t)) {
            pool.add(t);
        }
    }

    public int availableSize() {
        return pool.size();
    }

    public int usedSize() {
        return used.size();
    }
}