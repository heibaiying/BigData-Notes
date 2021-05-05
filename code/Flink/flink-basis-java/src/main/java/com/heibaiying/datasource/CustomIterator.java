package com.heibaiying.datasource;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName CustomIterator.java
 * @Description TODO
 * @createTime 2021年05月03日 22:16:00
 */
public class CustomIterator implements Iterator<Integer>, Serializable {
    private Integer i = 0;

    @Override
    public boolean hasNext() {
        return i < 100;
    }

    @Override
    public Integer next() {
        i++;
        return i;
    }
}
