package com.arelore.server.common.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.util.*;

/**
 * 通用工具类
 */
public class CommonUtils {

    private CommonUtils() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return true-空，false-非空
     */
    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return true-非空，false-空
     */
    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    /**
     * 判断集合是否为空
     *
     * @param collection 集合
     * @return true-空，false-非空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否不为空
     *
     * @param collection 集合
     * @return true-非空，false-空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断 Map 是否为空
     *
     * @param map Map
     * @return true-空，false-非空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断 Map 是否不为空
     *
     * @param map Map
     * @return true-非空，false-空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 判断数组是否为空
     *
     * @param array 数组
     * @return true-空，false-非空
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否不为空
     *
     * @param array 数组
     * @return true-非空，false-空
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * 对象转 JSON 字符串
     *
     * @param obj 对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj);
    }

    /**
     * JSON 字符串转对象
     *
     * @param json   JSON 字符串
     * @param clazz  目标类型
     * @param <T>    泛型
     * @return 对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StrUtil.isEmpty(json)) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    /**
     * JSON 字符串转 List
     *
     * @param json   JSON 字符串
     * @param clazz  元素类型
     * @param <T>    泛型
     * @return List
     */
    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
        if (StrUtil.isEmpty(json)) {
            return null;
        }
        return JSON.parseArray(json, clazz);
    }

    /**
     * JSON 字符串转复杂类型
     *
     * @param json          JSON 字符串
     * @param typeReference 类型引用
     * @param <T>           泛型
     * @return 对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StrUtil.isEmpty(json)) {
            return null;
        }
        return JSON.parseObject(json, typeReference);
    }

    /**
     * 创建 HashMap
     *
     * @param key   键
     * @param value 值
     * @return HashMap
     */
    public static <K, V> Map<K, V> of(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * 创建 HashMap（多对）
     *
     * @param keysAndValues 键值对数组 [k1, v1, k2, v2, ...]
     * @return HashMap
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <K, V> Map<K, V> ofMap(Object... keysAndValues) {
        Map<K, V> map = new HashMap<>();
        if (keysAndValues != null && keysAndValues.length > 0) {
            for (int i = 0; i < keysAndValues.length; i += 2) {
                if (i + 1 < keysAndValues.length) {
                    map.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
                }
            }
        }
        return map;
    }

    /**
     * 创建 List
     *
     * @param items 元素数组
     * @param <T>   泛型
     * @return List
     */
    @SafeVarargs
    public static <T> List<T> ofList(T... items) {
        List<T> list = new ArrayList<>();
        if (items != null) {
            Collections.addAll(list, items);
        }
        return list;
    }

    /**
     * 创建 Set
     *
     * @param items 元素数组
     * @param <T>   泛型
     * @return Set
     */
    @SafeVarargs
    public static <T> Set<T> ofSet(T... items) {
        Set<T> set = new HashSet<>();
        if (items != null) {
            Collections.addAll(set, items);
        }
        return set;
    }

    /**
     * 安全获取 List 元素
     *
     * @param list    List
     * @param index   索引
     * @param defaultVal 默认值
     * @param <T>     泛型
     * @return 元素或默认值
     */
    public static <T> T safeGet(List<T> list, int index, T defaultVal) {
        if (list == null || index < 0 || index >= list.size()) {
            return defaultVal;
        }
        return list.get(index);
    }

    /**
     * 安全获取 Map 值
     *
     * @param map       Map
     * @param key       键
     * @param defaultVal 默认值
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 值或默认值
     */
    public static <K, V> V safeGet(Map<K, V> map, K key, V defaultVal) {
        if (map == null || key == null || !map.containsKey(key)) {
            return defaultVal;
        }
        return map.get(key);
    }

    /**
     * 数字转 Integer，处理 null
     *
     * @param number 数字
     * @return Integer
     */
    public static Integer toInteger(Number number) {
        if (number == null) {
            return null;
        }
        return number.intValue();
    }

    /**
     * 字符串转 Integer，处理 null 和异常
     *
     * @param str 字符串
     * @return Integer
     */
    public static Integer toInteger(String str) {
        if (StrUtil.isEmpty(str)) {
            return null;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 字符串转 Long，处理 null 和异常
     *
     * @param str 字符串
     * @return Long
     */
    public static Long toLong(String str) {
        if (StrUtil.isEmpty(str)) {
            return null;
        }
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 字符串转 Double，处理 null 和异常
     *
     * @param str 字符串
     * @return Double
     */
    public static Double toDouble(String str) {
        if (StrUtil.isEmpty(str)) {
            return null;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 去除字符串前后缀
     *
     * @param str      字符串
     * @param prefix   前缀
     * @param suffix   后缀
     * @return 处理后的字符串
     */
    public static String trim(String str, String prefix, String suffix) {
        if (StrUtil.isEmpty(str)) {
            return str;
        }
        String result = str;
        if (prefix != null && result.startsWith(prefix)) {
            result = result.substring(prefix.length());
        }
        if (suffix != null && result.endsWith(suffix)) {
            result = result.substring(0, result.length() - suffix.length());
        }
        return result;
    }
}
