-- ==========================================
-- Appended from Xenon schema.sql
-- ==========================================

-- Xenon Database Schema for SQLite

-- Workspace table
CREATE TABLE IF NOT EXISTS workspace (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         name VARCHAR(100) NOT NULL UNIQUE,
    namespace_uri VARCHAR(255),
    description VARCHAR(500),
    is_isolated INTEGER DEFAULT 0,
    is_enabled INTEGER DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted INTEGER DEFAULT 0
    );

-- DataStore table
CREATE TABLE IF NOT EXISTS datastore (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    is_enabled INTEGER DEFAULT 1,
    connection_params TEXT,
    workspace_id INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (workspace_id) REFERENCES workspace(id),
    UNIQUE(workspace_id, name)
    );

-- FeatureType table
CREATE TABLE IF NOT EXISTS feature_type (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                                            name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100),
    title VARCHAR(200),
    description VARCHAR(1000),
    keywords VARCHAR(500),
    srs VARCHAR(50),
    native_srs VARCHAR(50),
    bbox_min_x REAL,
    bbox_min_y REAL,
    bbox_max_x REAL,
    bbox_max_y REAL,
    native_bbox_min_x REAL,
    native_bbox_min_y REAL,
    native_bbox_max_x REAL,
    native_bbox_max_y REAL,
    is_enabled INTEGER DEFAULT 1,
    datastore_id INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (datastore_id) REFERENCES datastore(id),
    UNIQUE(datastore_id, name)
    );

-- Coverage table
CREATE TABLE IF NOT EXISTS coverage (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100),
    title VARCHAR(200),
    description VARCHAR(1000),
    keywords VARCHAR(500),
    srs VARCHAR(50),
    native_srs VARCHAR(50),
    native_format VARCHAR(50),
    bbox_min_x REAL,
    bbox_min_y REAL,
    bbox_max_x REAL,
    bbox_max_y REAL,
    width INTEGER,
    height INTEGER,
    num_bands INTEGER,
    is_enabled INTEGER DEFAULT 1,
    datastore_id INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (datastore_id) REFERENCES datastore(id),
    UNIQUE(datastore_id, name)
    );

-- Style table
CREATE TABLE IF NOT EXISTS style (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     name VARCHAR(100) NOT NULL,
    title VARCHAR(200),
    description VARCHAR(500),
    format VARCHAR(20) NOT NULL DEFAULT 'SLD',
    content TEXT,
    filename VARCHAR(255),
    workspace_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (workspace_id) REFERENCES workspace(id)
    );

-- Layer table
CREATE TABLE IF NOT EXISTS layer (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     name VARCHAR(100) NOT NULL,
    title VARCHAR(200),
    description VARCHAR(1000),
    type VARCHAR(20) NOT NULL,
    is_enabled INTEGER DEFAULT 1,
    is_advertised INTEGER DEFAULT 1,
    is_queryable INTEGER DEFAULT 1,
    is_opaque INTEGER DEFAULT 0,
    feature_type_id INTEGER,
    coverage_id INTEGER,
    datastore_id INTEGER,
    workspace_id INTEGER NOT NULL,
    default_style_id INTEGER,
    srs VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted INTEGER DEFAULT 0,
    FOREIGN KEY (feature_type_id) REFERENCES feature_type(id),
    FOREIGN KEY (coverage_id) REFERENCES coverage(id),
    FOREIGN KEY (datastore_id) REFERENCES datastore(id),
    FOREIGN KEY (workspace_id) REFERENCES workspace(id),
    FOREIGN KEY (default_style_id) REFERENCES style(id),
    UNIQUE(workspace_id, name)
    );

-- Layer-Style association table
CREATE TABLE IF NOT EXISTS layer_style (
                                           layer_id INTEGER NOT NULL,
                                           style_id INTEGER NOT NULL,
                                           PRIMARY KEY (layer_id, style_id),
    FOREIGN KEY (layer_id) REFERENCES layer(id),
    FOREIGN KEY (style_id) REFERENCES style(id)
    );

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_workspace_name ON workspace(name);
CREATE INDEX IF NOT EXISTS idx_datastore_workspace ON datastore(workspace_id);
CREATE INDEX IF NOT EXISTS idx_featuretype_datastore ON feature_type(datastore_id);
CREATE INDEX IF NOT EXISTS idx_coverage_datastore ON coverage(datastore_id);
CREATE INDEX IF NOT EXISTS idx_layer_name ON layer(name);
CREATE INDEX IF NOT EXISTS idx_style_workspace ON style(workspace_id);

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, "path", component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES(2000, 'Xenon GIS', 0, 10, 'xenon', 'Layout', 1, 0, 'M', '0', '0', '', 'ion:earth', 'admin', '2026-03-10 16:02:53', NULL, NULL, 'Xenon GIS Module');
INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, "path", component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES(2001, '工作空间', 2000, 1, 'workspace', 'xenon/workspace/index', 1, 0, 'C', '0', '0', 'xenon:workspace:list', 'carbon:workspace', 'admin', '2026-03-10 16:02:53', NULL, NULL, 'Workspace Management');
INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, "path", component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES(2002, '数据存储', 2000, 2, 'datastore', 'xenon/datastore/index', 1, 0, 'C', '0', '0', 'xenon:datastore:list', 'carbon:datastore', 'admin', '2026-03-10 16:02:53', NULL, NULL, 'Datastore Management');
INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, "path", component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES(2003, '图层管理', 2000, 3, 'layer', 'xenon/layer/index', 1, 0, 'C', '0', '0', 'xenon:layer:list', 'mingcute:layer-fill', 'admin', '2026-03-10 16:02:53', NULL, NULL, 'Layer Management');
INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, "path", component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES(2004, '样式管理', 2000, 4, 'style', 'xenon/style/index', 1, 0, 'C', '0', '0', 'xenon:style:list', 'material-symbols:style-outline', 'admin', '2026-03-10 16:02:53', NULL, NULL, 'Style Management');
INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, "path", component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES(2005, '数据预览', 2000, 5, 'preview', 'xenon/preview/index', 1, 0, 'C', '0', '0', 'xenon:preview:list', 'qlementine-icons:preview-16', 'admin', '2026-03-10 16:02:53', NULL, NULL, '2D Map Preview');
INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, "path", component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES(2006, '3D预览', 2000, 6, 'preview3d', 'xenon/preview3d/index', 1, 0, 'C', '0', '0', 'xenon:preview-3d:list', 'mdi:earth-plus', 'admin', '2026-03-10 16:02:53', NULL, NULL, '3D Map Preview');