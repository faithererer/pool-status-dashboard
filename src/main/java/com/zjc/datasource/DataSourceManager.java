package com.zjc.datasource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjc.dto.DataSourceTypeDTO;
import com.zjc.dto.PoolStatusDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源管理器
 * 负责管理和调用各种数据源服务
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Component
public class DataSourceManager {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * JSON对象映射器
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 数据源服务缓存
     * key: 数据源类型, value: 数据源服务实例
     */
    private final Map<String, PoolDataSourceService> dataSourceServices = new ConcurrentHashMap<>();

    /**
     * 反射方法缓存
     * key: 类名.方法名, value: 方法对象
     */
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    /**
     * 初始化数据源服务
     */
    @PostConstruct
    public void initDataSources() {
        log.info("开始初始化数据源服务");
        
        try {
            // 获取所有实现了PoolDataSourceService接口的Bean
            Map<String, PoolDataSourceService> beans = applicationContext.getBeansOfType(PoolDataSourceService.class);
            
            for (Map.Entry<String, PoolDataSourceService> entry : beans.entrySet()) {
                PoolDataSourceService service = entry.getValue();
                String dataSourceType = service.getDataSourceType();
                
                dataSourceServices.put(dataSourceType, service);
                log.info("注册数据源服务: {} - {}", dataSourceType, service.getDescription());
            }
            
            log.info("数据源服务初始化完成，共注册 {} 个数据源", dataSourceServices.size());
            
        } catch (Exception e) {
            log.error("初始化数据源服务失败", e);
        }
    }

    /**
     * 获取号池状态数据
     *
     * @param poolId 号池ID
     * @param poolName 号池名称
     * @param dataSourceClass 数据源类名
     * @param dataSourceConfig 数据源配置
     * @return 号池状态数据
     */
    public PoolStatusDTO getPoolStatus(Long poolId, String poolName, String dataSourceClass, Map<String, Object> dataSourceConfig) {
        log.debug("获取号池状态数据, poolId: {}, poolName: {}, dataSourceClass: {}", poolId, poolName, dataSourceClass);
        
        try {
            // 首先尝试使用注册的数据源服务
            PoolDataSourceService service = dataSourceServices.get(dataSourceClass);
            if (service != null) {
                // 将Map转换为JSON字符串传递给服务
                String configJson = convertMapToJson(dataSourceConfig);
                return service.getPoolStatus(poolId, poolName, configJson);
            }
            
            // 如果没有找到注册的服务，尝试使用反射调用
            return getPoolStatusByReflection(poolId, poolName, dataSourceClass, dataSourceConfig);
            
        } catch (Exception e) {
            log.error("获取号池状态数据失败, poolId: {}, dataSourceClass: {}", poolId, dataSourceClass, e);
            return createErrorStatus(poolId);
        }
    }

    /**
     * 检查数据源连接
     *
     * @param dataSourceClass 数据源类名
     * @param dataSourceConfig 数据源配置
     * @return 连接是否正常
     */
    public boolean checkConnection(String dataSourceClass, Map<String, Object> dataSourceConfig) {
        log.debug("检查数据源连接, dataSourceClass: {}", dataSourceClass);
        
        try {
            // 首先尝试使用注册的数据源服务
            PoolDataSourceService service = dataSourceServices.get(dataSourceClass);
            if (service != null) {
                // 将Map转换为JSON字符串传递给服务
                String configJson = convertMapToJson(dataSourceConfig);
                return service.checkConnection(configJson);
            }
            
            // 如果没有找到注册的服务，尝试使用反射调用
            return checkConnectionByReflection(dataSourceClass, dataSourceConfig);
            
        } catch (Exception e) {
            log.error("检查数据源连接失败, dataSourceClass: {}", dataSourceClass, e);
            return false;
        }
    }

    /**
     * 获取所有可用的数据源类型
     * 
     * @return 数据源类型映射
     */
    public List<DataSourceTypeDTO> getAvailableDataSources() {
        List<DataSourceTypeDTO> result = new ArrayList<>();
        
        for (PoolDataSourceService service : dataSourceServices.values()) {
            result.add(new DataSourceTypeDTO(
                service.getDataSourceType(),
                service.getDescription(),
                service.getClassFullName()
            ));
        }
        
        return result;
    }

    /**
     * 通过反射获取号池状态数据
     */
    private PoolStatusDTO getPoolStatusByReflection(Long poolId, String poolName, String dataSourceClass, Map<String, Object> dataSourceConfig) {
        try {
            // 直接使用完整类名
            String className = dataSourceClass;
            
            // 获取类和方法
            Class<?> clazz = Class.forName(className);
            Object instance = applicationContext.getBean(clazz);
            
            String methodKey = className + ".getPoolStatus";
            Method method = methodCache.computeIfAbsent(methodKey, k -> {
                try {
                    return clazz.getMethod("getPoolStatus", Long.class, String.class, String.class);
                } catch (NoSuchMethodException e) {
                    log.error("未找到方法: {}", k, e);
                    return null;
                }
            });
            
            if (method != null) {
                // 将Map转换为JSON字符串传递给方法
                String configJson = convertMapToJson(dataSourceConfig);
                return (PoolStatusDTO) method.invoke(instance, poolId, poolName, configJson);
            }
            
        } catch (Exception e) {
            log.error("反射调用获取号池状态失败, dataSourceClass: {}", dataSourceClass, e);
        }
        
        return createErrorStatus(poolId);
    }

    /**
     * 通过反射检查数据源连接
     */
    private boolean checkConnectionByReflection(String dataSourceClass, Map<String, Object> dataSourceConfig) {
        try {
            // 直接使用完整类名
            String className = dataSourceClass;
            
            // 获取类和方法
            Class<?> clazz = Class.forName(className);
            Object instance = applicationContext.getBean(clazz);
            
            String methodKey = className + ".checkConnection";
            Method method = methodCache.computeIfAbsent(methodKey, k -> {
                try {
                    return clazz.getMethod("checkConnection", String.class);
                } catch (NoSuchMethodException e) {
                    log.error("未找到方法: {}", k, e);
                    return null;
                }
            });
            
            if (method != null) {
                // 将Map转换为JSON字符串传递给方法
                String configJson = convertMapToJson(dataSourceConfig);
                return (Boolean) method.invoke(instance, configJson);
            }
            
        } catch (Exception e) {
            log.error("反射调用检查连接失败, dataSourceClass: {}", dataSourceClass, e);
        }
        
        return false;
    }

    /**
     * 创建错误状态数据
     */
    private PoolStatusDTO createErrorStatus(Long poolId) {
        PoolStatusDTO status = new PoolStatusDTO();
        status.setPoolId(poolId);
        status.setValidCount(0);
        status.setInvalidCount(0);
        status.setCoolingCount(0);
        status.setTotalCount(0);
        status.setPressure(BigDecimal.valueOf(0.0));
        status.setRecordTime(System.currentTimeMillis());
        return status;
    }

    /**
     * 首字母大写
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 获取数据源服务实例
     */
    public PoolDataSourceService getDataSourceService(String dataSourceType) {
        return dataSourceServices.get(dataSourceType);
    }

    /**
     * 注册数据源服务
     */
    public void registerDataSourceService(String dataSourceType, PoolDataSourceService service) {
        dataSourceServices.put(dataSourceType, service);
        log.info("动态注册数据源服务: {} - {}", dataSourceType, service.getDescription());
    }

    /**
     * 注销数据源服务
     */
    public void unregisterDataSourceService(String dataSourceType) {
        PoolDataSourceService removed = dataSourceServices.remove(dataSourceType);
        if (removed != null) {
            log.info("注销数据源服务: {}", dataSourceType);
        }
    }

    /**
     * 将Map转换为JSON字符串
     */
    private String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("转换Map到JSON失败", e);
            return "{}";
        }
    }
}