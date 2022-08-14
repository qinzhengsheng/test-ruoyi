package com.ruoyi.common.utils.pdf;

import org.apache.commons.beanutils.BeanComparator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortUtil {
    public SortUtil() {
    }

    public static <V> void sort(List<V> list, final String... properties) {
        Collections.sort(list, new Comparator<V>() {
            @Override
            public int compare(V o1, V o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                } else if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else {
                    String[] var6;
                    int var5 = (var6 = properties).length;

                    for (int var4 = 0; var4 < var5; ++var4) {
                        String property = var6[var4];
                        Comparator c = new BeanComparator(property);
                        int result = c.compare(o1, o2);
                        if (result != 0) {
                            return result;
                        }
                    }

                    return 0;
                }
            }
        });
    }

}


