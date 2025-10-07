-- 号池监控面板数据库初始化脚本
-- 数据库类型: SQLite

-- 创建号池表
CREATE TABLE IF NOT EXISTS pool (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    name VARCHAR(100) NOT NULL,
    description TEXT,
    data_source_class VARCHAR(255),
    data_source_config TEXT,
    update_frequency INTEGER DEFAULT 60,
    display_fields TEXT,
    display_strategy VARCHAR(20) DEFAULT 'public',
    enabled BOOLEAN DEFAULT 1,
    last_update_time INTEGER,
    last_update_status VARCHAR(20),
    last_error_message TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
    );

-- 创建号池状态表
CREATE TABLE IF NOT EXISTS pool_status (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           pool_id INTEGER NOT NULL,
                                           valid_count INTEGER DEFAULT 0,
                                           invalid_count INTEGER DEFAULT 0,
                                           cooling_count INTEGER DEFAULT 0,
                                           total_count INTEGER DEFAULT 0,
                                           pressure REAL DEFAULT 0.00,
                                           record_time INTEGER NOT NULL,
                                           data_source VARCHAR(20) DEFAULT 'system',
    remarks TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (pool_id) REFERENCES pool(id)
    );

-- 创建虚拟聚合池表
CREATE TABLE IF NOT EXISTS virtual_pool (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                                            name VARCHAR(100) NOT NULL,
    description TEXT,
    pool_ids TEXT NOT NULL,
    aggregate_strategy VARCHAR(20) DEFAULT 'sum',
    weight_config TEXT,
    enabled BOOLEAN DEFAULT 1,
    display_order INTEGER DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
    );

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_pool_enabled ON pool(enabled, deleted);
CREATE INDEX IF NOT EXISTS idx_pool_display_strategy ON pool(display_strategy, deleted);
CREATE INDEX IF NOT EXISTS idx_pool_status_pool_id ON pool_status(pool_id, deleted);
CREATE INDEX IF NOT EXISTS idx_pool_status_record_time ON pool_status(record_time, deleted);
CREATE INDEX IF NOT EXISTS idx_pool_status_pool_record ON pool_status(pool_id, record_time, deleted);
CREATE INDEX IF NOT EXISTS idx_virtual_pool_enabled ON virtual_pool(enabled, deleted);
CREATE INDEX IF NOT EXISTS idx_virtual_pool_display_order ON virtual_pool(display_order, deleted);

-- 插入示例数据
INSERT OR IGNORE INTO pool (id, name, description, data_source_class, update_frequency, display_strategy, enabled) VALUES
(1, '主要业务号池', '核心业务使用的主要号池', 'com.zjc.datasource.impl.MockPoolDataSourceService', 60, 'public', 1),
(2, '备用号池A', '备用服务使用的号池', 'com.zjc.datasource.impl.MockPoolDataSourceService', 60, 'public', 1),
(3, '测试号池', '测试环境专用号池', 'com.zjc.datasource.impl.MockPoolDataSourceService', 30, 'private', 1),
(4, '紧急备用池', '紧急情况使用的备用号池', 'com.zjc.datasource.impl.MockPoolDataSourceService', 120, 'public', 1);

-- 插入虚拟聚合池示例数据
INSERT OR IGNORE INTO virtual_pool (id, name, description, pool_ids, aggregate_strategy, enabled, display_order) VALUES
(1, '核心业务聚合池', '聚合: 主要业务号池 + 备用号池A', '[1,2]', 'sum', 1, 1),
(2, '全部号池聚合', '聚合: 所有号池统计', '[1,2,3,4]', 'sum', 1, 2);
