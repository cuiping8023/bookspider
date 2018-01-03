package com.cienet.util;

import com.cienet.po.Book;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Created by cuiping on 2018/1/3.
 */
public class TransUtil {
        /**
         * map转对象
         *
         * @param map  map
         * @param type 类型
         * @param <T>  泛型
         * @return 对象
         * @throws Exception 反射异常
         */
        static <T> T mapToBean(Map<String, Object> map, Class<T> type) throws Exception {
            if (map == null) {
                return null;
            }
            Set<Map.Entry<String, Object>> sets = map.entrySet();

            T entity = type.newInstance();

            Method[] methods = type.getDeclaredMethods();
            for (Map.Entry<String, Object> entry : sets) {
                String str = entry.getKey();
                String setMethod = "set" + str.substring(0, 1).toUpperCase() + str.substring(1);
                for (Method method : methods) {

                    if (method.getName().equals(setMethod)) {
                        method.invoke(entity, entry.getValue());
                    }
                }
            }
            return entity;
        }
    }


