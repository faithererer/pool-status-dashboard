package com.zjc.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bean工具类
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
public class BeanUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 复制属性，忽略null值
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }
    
    /**
     * 获取对象中为null的属性名
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        
        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
    
    /**
     * 将JSON字符串转换为Map
     */
    public static Map<String, Object> jsonToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("JSON转换为Map失败: {}", json, e);
            return new HashMap<>();
        }
    }
    
    /**
     * 将Map转换为JSON字符串
     */
    public static String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Map转换为JSON失败: {}", map, e);
            return "{}";
        }
    }
    
    /**
     * 将JSON字符串转换为List<Long>
     */
    public static List<Long> jsonToLongList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<Object> list = objectMapper.readValue(json, new TypeReference<List<Object>>() {});
            return list.stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        } else if (obj instanceof String) {
                            try {
                                return Long.parseLong((String) obj);
                            } catch (NumberFormatException e) {
                                log.warn("无法将字符串转换为Long: {}", obj);
                                return null;
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.warn("JSON转换为Long列表失败: {}", json, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 将List<Long>转换为JSON字符串
     */
    public static String longListToJson(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("Long列表转换为JSON失败: {}", list, e);
            return "[]";
        }
    }
    
    /**
     * 将JSON字符串转换为Boolean Map
     */
    public static Map<String, Boolean> jsonToBooleanMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            Map<String, Boolean> result = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Boolean) {
                    result.put(entry.getKey(), (Boolean) value);
                } else if (value instanceof String) {
                    result.put(entry.getKey(), Boolean.parseBoolean((String) value));
                } else {
                    result.put(entry.getKey(), false);
                }
            }
            
            return result;
        } catch (JsonProcessingException e) {
            log.warn("JSON转换为Boolean Map失败: {}", json, e);
            return new HashMap<>();
        }
    }
    
    /**
     * 将Boolean Map转换为JSON字符串
     */
    public static String booleanMapToJson(Map<String, Boolean> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Boolean Map转换为JSON失败: {}", map, e);
            return "{}";
        }
    }
}