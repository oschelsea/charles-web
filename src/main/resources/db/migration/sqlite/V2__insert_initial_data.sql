-- Flyway V2: 插入初始数据 (SQLite版本)
-- 此脚本用于初始化系统基础数据

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO "sys_config" VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2021-08-26', NULL, NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO "sys_config" VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2021-08-26', NULL, NULL, '初始化密码 123456');
INSERT INTO "sys_config" VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2021-08-26', NULL, NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO "sys_config" VALUES (4, '账号自助-验证码开关', 'sys.account.captchaOnOff', 'true', 'Y', 'admin', '2021-08-26', NULL, NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO "sys_config" VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2021-08-26', NULL, NULL, '是否开启注册用户功能（true开启，false关闭）');

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO "sys_dept" VALUES (100, 0, '0', 'xxx科技', NULL, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);
INSERT INTO "sys_dept" VALUES (101, 100, '0,100', '深圳总公司', 1, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);
INSERT INTO "sys_dept" VALUES (102, 100, '0,100', '长沙分公司', 2, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);
INSERT INTO "sys_dept" VALUES (103, 101, '0,100,101', '研发部门', 1, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);
INSERT INTO "sys_dept" VALUES (104, 101, '0,100,101', '市场部门', 2, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);
INSERT INTO "sys_dept" VALUES (105, 101, '0,100,101', '测试部门', 3, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);
INSERT INTO "sys_dept" VALUES (106, 101, '0,100,101', '财务部门', 4, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);
INSERT INTO "sys_dept" VALUES (107, 101, '0,100,101', '运维部门', 5, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);
INSERT INTO "sys_dept" VALUES (108, 102, '0,100,102', '市场部门', 1, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);
INSERT INTO "sys_dept" VALUES (109, 102, '0,100,102', '财务部门', 2, 'xxx', '15888888888', 'xxx@qq.com', '0', '0', 'admin', '2021-08-26', NULL, NULL);

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO "sys_dict_data" VALUES (1, 1, '男', '0', 'sys_user_sex', NULL, NULL, 'Y', '0', 'admin', '2021-08-26', NULL, NULL, '性别男');
INSERT INTO "sys_dict_data" VALUES (2, 2, '女', '1', 'sys_user_sex', NULL, NULL, 'N', '0', 'admin', '2021-08-26', NULL, NULL, '性别女');
INSERT INTO "sys_dict_data" VALUES (3, 3, '未知', '2', 'sys_user_sex', NULL, NULL, 'N', '0', 'admin', '2021-08-26', NULL, NULL, '性别未知');
INSERT INTO "sys_dict_data" VALUES (4, 1, '显示', '0', 'sys_show_hide', NULL, 'primary', 'Y', '0', 'admin', '2021-08-26', NULL, NULL, '显示菜单');
INSERT INTO "sys_dict_data" VALUES (5, 2, '隐藏', '1', 'sys_show_hide', NULL, 'danger', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '隐藏菜单');
INSERT INTO "sys_dict_data" VALUES (6, 1, '正常', '0', 'sys_normal_disable', NULL, 'primary', 'Y', '0', 'admin', '2021-08-26', NULL, NULL, '正常状态');
INSERT INTO "sys_dict_data" VALUES (7, 2, '停用', '1', 'sys_normal_disable', NULL, 'danger', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '停用状态');
INSERT INTO "sys_dict_data" VALUES (8, 1, '正常', '0', 'sys_job_status', NULL, 'primary', 'Y', '0', 'admin', '2021-08-26', NULL, NULL, '正常状态');
INSERT INTO "sys_dict_data" VALUES (9, 2, '暂停', '1', 'sys_job_status', NULL, 'danger', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '停用状态');
INSERT INTO "sys_dict_data" VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', NULL, NULL, 'Y', '0', 'admin', '2021-08-26', NULL, NULL, '默认分组');
INSERT INTO "sys_dict_data" VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', NULL, NULL, 'N', '0', 'admin', '2021-08-26', NULL, NULL, '系统分组');
INSERT INTO "sys_dict_data" VALUES (12, 1, '是', 'Y', 'sys_yes_no', NULL, 'primary', 'Y', '0', 'admin', '2021-08-26', NULL, NULL, '系统默认是');
INSERT INTO "sys_dict_data" VALUES (13, 2, '否', 'N', 'sys_yes_no', NULL, 'danger', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '系统默认否');
INSERT INTO "sys_dict_data" VALUES (14, 1, '通知', '1', 'sys_notice_type', NULL, 'warning', 'Y', '0', 'admin', '2021-08-26', NULL, NULL, '通知');
INSERT INTO "sys_dict_data" VALUES (15, 2, '公告', '2', 'sys_notice_type', NULL, 'success', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '公告');
INSERT INTO "sys_dict_data" VALUES (16, 1, '正常', '0', 'sys_notice_status', NULL, 'primary', 'Y', '0', 'admin', '2021-08-26', NULL, NULL, '正常状态');
INSERT INTO "sys_dict_data" VALUES (17, 2, '关闭', '1', 'sys_notice_status', NULL, 'danger', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '关闭状态');
INSERT INTO "sys_dict_data" VALUES (18, 1, '新增', '1', 'sys_oper_type', NULL, 'info', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '新增操作');
INSERT INTO "sys_dict_data" VALUES (19, 2, '修改', '2', 'sys_oper_type', NULL, 'info', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '修改操作');
INSERT INTO "sys_dict_data" VALUES (20, 3, '删除', '3', 'sys_oper_type', NULL, 'danger', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '删除操作');
INSERT INTO "sys_dict_data" VALUES (21, 4, '授权', '4', 'sys_oper_type', NULL, 'primary', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '授权操作');
INSERT INTO "sys_dict_data" VALUES (22, 5, '导出', '5', 'sys_oper_type', NULL, 'warning', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '导出操作');
INSERT INTO "sys_dict_data" VALUES (23, 6, '导入', '6', 'sys_oper_type', NULL, 'warning', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '导入操作');
INSERT INTO "sys_dict_data" VALUES (24, 7, '强退', '7', 'sys_oper_type', NULL, 'danger', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '强退操作');
INSERT INTO "sys_dict_data" VALUES (25, 8, '生成代码', '8', 'sys_oper_type', NULL, 'warning', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '生成操作');
INSERT INTO "sys_dict_data" VALUES (26, 9, '清空数据', '9', 'sys_oper_type', NULL, 'danger', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '清空操作');
INSERT INTO "sys_dict_data" VALUES (27, 1, '成功', '0', 'sys_common_status', NULL, 'primary', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '正常状态');
INSERT INTO "sys_dict_data" VALUES (28, 2, '失败', '1', 'sys_common_status', NULL, 'danger', 'N', '0', 'admin', '2021-08-26', NULL, NULL, '停用状态');

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO "sys_dict_type" VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2021-08-26', NULL, NULL, '用户性别列表');
INSERT INTO "sys_dict_type" VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2021-08-26', NULL, NULL, '菜单状态列表');
INSERT INTO "sys_dict_type" VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2021-08-26', NULL, NULL, '系统开关列表');
INSERT INTO "sys_dict_type" VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2021-08-26', NULL, NULL, '任务状态列表');
INSERT INTO "sys_dict_type" VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2021-08-26', NULL, NULL, '任务分组列表');
INSERT INTO "sys_dict_type" VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2021-08-26', NULL, NULL, '系统是否列表');
INSERT INTO "sys_dict_type" VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2021-08-26', NULL, NULL, '通知类型列表');
INSERT INTO "sys_dict_type" VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2021-08-26', NULL, NULL, '通知状态列表');
INSERT INTO "sys_dict_type" VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2021-08-26', NULL, NULL, '操作类型列表');
INSERT INTO "sys_dict_type" VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2021-08-26', NULL, NULL, '登录状态列表');

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO "sys_job" VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_job" VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(''ry'')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_job" VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(''ry'', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2021-08-26', NULL, NULL, NULL);

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO "sys_menu" VALUES (1, '系统管理', 0, 1, 'system', NULL, 1, NULL, 'M', '0', '0', NULL, 'system', 'admin', '2021-08-26', NULL, NULL, '系统管理目录');
INSERT INTO "sys_menu" VALUES (2, '系统监控', 0, 2, 'monitor', NULL, 1, NULL, 'M', '0', '0', NULL, 'monitor', 'admin', '2021-08-26', NULL, NULL, '系统监控目录');
INSERT INTO "sys_menu" VALUES (3, '系统工具', 0, 3, 'tool', NULL, 1, NULL, 'M', '0', '0', NULL, 'tool', 'admin', '2021-08-26', NULL, NULL, '系统工具目录');
INSERT INTO "sys_menu" VALUES (4, 'xxx官网', 0, 4, 'http://xxx.io', NULL, 2, NULL, 'M', '0', '0', NULL, 'guide', 'admin', '2021-08-26', NULL, NULL, 'charles官网地址');
INSERT INTO "sys_menu" VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', 1, NULL, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2021-08-26', NULL, NULL, '用户管理菜单');
INSERT INTO "sys_menu" VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', 1, NULL, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2021-08-26', NULL, NULL, '角色管理菜单');
INSERT INTO "sys_menu" VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', 1, NULL, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2021-08-26', NULL, NULL, '菜单管理菜单');
INSERT INTO "sys_menu" VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', 1, NULL, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2021-08-26', NULL, NULL, '部门管理菜单');
INSERT INTO "sys_menu" VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', 1, NULL, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2021-08-26', NULL, NULL, '岗位管理菜单');
INSERT INTO "sys_menu" VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', 1, NULL, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2021-08-26', NULL, NULL, '字典管理菜单');
INSERT INTO "sys_menu" VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', 1, NULL, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2021-08-26', NULL, NULL, '参数设置菜单');
INSERT INTO "sys_menu" VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', 1, NULL, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2021-08-26', NULL, NULL, '通知公告菜单');
INSERT INTO "sys_menu" VALUES (108, '日志管理', 1, 9, 'log', NULL, 1, NULL, 'M', '0', '0', NULL, 'log', 'admin', '2021-08-26', NULL, NULL, '日志管理菜单');
INSERT INTO "sys_menu" VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', 1, NULL, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2021-08-26', NULL, NULL, '在线用户菜单');
INSERT INTO "sys_menu" VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', 1, NULL, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2021-08-26', NULL, NULL, '定时任务菜单');
INSERT INTO "sys_menu" VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', 1, NULL, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2021-08-26', NULL, NULL, '数据监控菜单');
INSERT INTO "sys_menu" VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', 1, NULL, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2021-08-26', NULL, NULL, '服务监控菜单');
INSERT INTO "sys_menu" VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', 1, NULL, 'C', '1', '1', 'monitor:cache:list', 'redis', 'admin', '2021-08-26', 'admin', '2021-09-07 08:38:12', '缓存监控菜单');
INSERT INTO "sys_menu" VALUES (114, '表单构建', 3, 1, 'build', 'tool/build/index', 1, NULL, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2021-08-26', NULL, NULL, '表单构建菜单');
INSERT INTO "sys_menu" VALUES (115, '代码生成', 3, 2, 'gen', 'tool/gen/index', 1, NULL, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2021-08-26', NULL, NULL, '代码生成菜单');
INSERT INTO "sys_menu" VALUES (116, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', 1, NULL, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2021-08-26', NULL, NULL, '系统接口菜单');
INSERT INTO "sys_menu" VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', 1, NULL, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2021-08-26', NULL, NULL, '操作日志菜单');
INSERT INTO "sys_menu" VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', 1, NULL, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2021-08-26', NULL, NULL, '登录日志菜单');
INSERT INTO "sys_menu" VALUES (1001, '用户查询', 100, 1, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:user:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1002, '用户新增', 100, 2, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:user:add', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1003, '用户修改', 100, 3, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1004, '用户删除', 100, 4, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1005, '用户导出', 100, 5, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:user:export', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1006, '用户导入', 100, 6, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:user:import', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1007, '重置密码', 100, 7, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1008, '角色查询', 101, 1, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:role:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1009, '角色新增', 101, 2, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:role:add', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1010, '角色修改', 101, 3, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1011, '角色删除', 101, 4, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1012, '角色导出', 101, 5, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:role:export', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1013, '菜单查询', 102, 1, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1014, '菜单新增', 102, 2, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1015, '菜单修改', 102, 3, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1016, '菜单删除', 102, 4, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1017, '部门查询', 103, 1, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1018, '部门新增', 103, 2, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1019, '部门修改', 103, 3, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1020, '部门删除', 103, 4, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1021, '岗位查询', 104, 1, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:post:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1022, '岗位新增', 104, 2, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:post:add', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1023, '岗位修改', 104, 3, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1024, '岗位删除', 104, 4, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1025, '岗位导出', 104, 5, NULL, NULL, 1, NULL, 'F', '0', '0', 'system:post:export', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1026, '字典查询', 105, 1, '#', NULL, 1, NULL, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1027, '字典新增', 105, 2, '#', NULL, 1, NULL, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1028, '字典修改', 105, 3, '#', NULL, 1, NULL, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1029, '字典删除', 105, 4, '#', NULL, 1, NULL, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1030, '字典导出', 105, 5, '#', NULL, 1, NULL, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1031, '参数查询', 106, 1, '#', NULL, 1, NULL, 'F', '0', '0', 'system:config:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1032, '参数新增', 106, 2, '#', NULL, 1, NULL, 'F', '0', '0', 'system:config:add', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1033, '参数修改', 106, 3, '#', NULL, 1, NULL, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1034, '参数删除', 106, 4, '#', NULL, 1, NULL, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1035, '参数导出', 106, 5, '#', NULL, 1, NULL, 'F', '0', '0', 'system:config:export', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1036, '公告查询', 107, 1, '#', NULL, 1, NULL, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1037, '公告新增', 107, 2, '#', NULL, 1, NULL, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1038, '公告修改', 107, 3, '#', NULL, 1, NULL, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1039, '公告删除', 107, 4, '#', NULL, 1, NULL, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1040, '操作查询', 500, 1, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1041, '操作删除', 500, 2, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1042, '日志导出', 500, 4, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1043, '登录查询', 501, 1, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1044, '登录删除', 501, 2, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1045, '日志导出', 501, 3, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1046, '在线查询', 109, 1, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1047, '批量强退', 109, 2, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1048, '单条强退', 109, 3, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1049, '任务查询', 110, 1, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1050, '任务新增', 110, 2, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1051, '任务修改', 110, 3, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1052, '任务删除', 110, 4, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1053, '状态修改', 110, 5, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1054, '任务导出', 110, 7, '#', NULL, 1, NULL, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1055, '生成查询', 115, 1, '#', NULL, 1, NULL, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1056, '生成修改', 115, 2, '#', NULL, 1, NULL, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1057, '生成删除', 115, 3, '#', NULL, 1, NULL, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1058, '导入代码', 115, 2, '#', NULL, 1, NULL, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1059, '预览代码', 115, 4, '#', NULL, 1, NULL, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_menu" VALUES (1060, '生成代码', 115, 5, '#', NULL, 1, NULL, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2021-08-26', NULL, NULL, NULL);

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO "sys_notice" VALUES (1, '温馨提醒：2018-07-01 charles新版本发布啦', '2', '新版本内容', '0', 'admin', '2021-08-26', 'admin', '2021-09-03 14:28:56', '管理员');
INSERT INTO "sys_notice" VALUES (2, '维护通知：2018-07-01 charles系统凌晨维护', '1', '维护内容', '0', 'admin', '2021-08-26', NULL, NULL, '管理员');

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO "sys_post" VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_post" VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_post" VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2021-08-26', NULL, NULL, NULL);
INSERT INTO "sys_post" VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2021-08-26', NULL, NULL, NULL);

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO "sys_role" VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2021-08-26', NULL, NULL, '超级管理员');
INSERT INTO "sys_role" VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', '2021-08-26', NULL, NULL, '普通角色');

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO "sys_role_dept" VALUES (2, 100);
INSERT INTO "sys_role_dept" VALUES (2, 101);
INSERT INTO "sys_role_dept" VALUES (2, 105);

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO "sys_role_menu" VALUES (2, 1);
INSERT INTO "sys_role_menu" VALUES (2, 2);
INSERT INTO "sys_role_menu" VALUES (2, 3);
INSERT INTO "sys_role_menu" VALUES (2, 4);
INSERT INTO "sys_role_menu" VALUES (2, 100);
INSERT INTO "sys_role_menu" VALUES (2, 101);
INSERT INTO "sys_role_menu" VALUES (2, 102);
INSERT INTO "sys_role_menu" VALUES (2, 103);
INSERT INTO "sys_role_menu" VALUES (2, 104);
INSERT INTO "sys_role_menu" VALUES (2, 105);
INSERT INTO "sys_role_menu" VALUES (2, 106);
INSERT INTO "sys_role_menu" VALUES (2, 107);
INSERT INTO "sys_role_menu" VALUES (2, 108);
INSERT INTO "sys_role_menu" VALUES (2, 109);
INSERT INTO "sys_role_menu" VALUES (2, 110);
INSERT INTO "sys_role_menu" VALUES (2, 111);
INSERT INTO "sys_role_menu" VALUES (2, 112);
INSERT INTO "sys_role_menu" VALUES (2, 113);
INSERT INTO "sys_role_menu" VALUES (2, 114);
INSERT INTO "sys_role_menu" VALUES (2, 115);
INSERT INTO "sys_role_menu" VALUES (2, 116);
INSERT INTO "sys_role_menu" VALUES (2, 500);
INSERT INTO "sys_role_menu" VALUES (2, 501);
INSERT INTO "sys_role_menu" VALUES (2, 1001);
INSERT INTO "sys_role_menu" VALUES (2, 1002);
INSERT INTO "sys_role_menu" VALUES (2, 1003);
INSERT INTO "sys_role_menu" VALUES (2, 1004);
INSERT INTO "sys_role_menu" VALUES (2, 1005);
INSERT INTO "sys_role_menu" VALUES (2, 1006);
INSERT INTO "sys_role_menu" VALUES (2, 1007);
INSERT INTO "sys_role_menu" VALUES (2, 1008);
INSERT INTO "sys_role_menu" VALUES (2, 1009);
INSERT INTO "sys_role_menu" VALUES (2, 1010);
INSERT INTO "sys_role_menu" VALUES (2, 1011);
INSERT INTO "sys_role_menu" VALUES (2, 1012);
INSERT INTO "sys_role_menu" VALUES (2, 1013);
INSERT INTO "sys_role_menu" VALUES (2, 1014);
INSERT INTO "sys_role_menu" VALUES (2, 1015);
INSERT INTO "sys_role_menu" VALUES (2, 1016);
INSERT INTO "sys_role_menu" VALUES (2, 1017);
INSERT INTO "sys_role_menu" VALUES (2, 1018);
INSERT INTO "sys_role_menu" VALUES (2, 1019);
INSERT INTO "sys_role_menu" VALUES (2, 1020);
INSERT INTO "sys_role_menu" VALUES (2, 1021);
INSERT INTO "sys_role_menu" VALUES (2, 1022);
INSERT INTO "sys_role_menu" VALUES (2, 1023);
INSERT INTO "sys_role_menu" VALUES (2, 1024);
INSERT INTO "sys_role_menu" VALUES (2, 1025);
INSERT INTO "sys_role_menu" VALUES (2, 1026);
INSERT INTO "sys_role_menu" VALUES (2, 1027);
INSERT INTO "sys_role_menu" VALUES (2, 1028);
INSERT INTO "sys_role_menu" VALUES (2, 1029);
INSERT INTO "sys_role_menu" VALUES (2, 1030);
INSERT INTO "sys_role_menu" VALUES (2, 1031);
INSERT INTO "sys_role_menu" VALUES (2, 1032);
INSERT INTO "sys_role_menu" VALUES (2, 1033);
INSERT INTO "sys_role_menu" VALUES (2, 1034);
INSERT INTO "sys_role_menu" VALUES (2, 1035);
INSERT INTO "sys_role_menu" VALUES (2, 1036);
INSERT INTO "sys_role_menu" VALUES (2, 1037);
INSERT INTO "sys_role_menu" VALUES (2, 1038);
INSERT INTO "sys_role_menu" VALUES (2, 1039);
INSERT INTO "sys_role_menu" VALUES (2, 1040);
INSERT INTO "sys_role_menu" VALUES (2, 1041);
INSERT INTO "sys_role_menu" VALUES (2, 1042);
INSERT INTO "sys_role_menu" VALUES (2, 1043);
INSERT INTO "sys_role_menu" VALUES (2, 1044);
INSERT INTO "sys_role_menu" VALUES (2, 1045);
INSERT INTO "sys_role_menu" VALUES (2, 1046);
INSERT INTO "sys_role_menu" VALUES (2, 1047);
INSERT INTO "sys_role_menu" VALUES (2, 1048);
INSERT INTO "sys_role_menu" VALUES (2, 1049);
INSERT INTO "sys_role_menu" VALUES (2, 1050);
INSERT INTO "sys_role_menu" VALUES (2, 1051);
INSERT INTO "sys_role_menu" VALUES (2, 1052);
INSERT INTO "sys_role_menu" VALUES (2, 1053);
INSERT INTO "sys_role_menu" VALUES (2, 1054);
INSERT INTO "sys_role_menu" VALUES (2, 1055);
INSERT INTO "sys_role_menu" VALUES (2, 1056);
INSERT INTO "sys_role_menu" VALUES (2, 1057);
INSERT INTO "sys_role_menu" VALUES (2, 1058);
INSERT INTO "sys_role_menu" VALUES (2, 1059);
INSERT INTO "sys_role_menu" VALUES (2, 1060);

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO "sys_user" VALUES (1, 103, 'admin', 'charles', '00', 'app@163.com', '15888888888', '1', NULL, '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2021-09-14 20:52:59', 'admin', '2021-08-26', NULL, '2021-09-14 12:53:00', '管理员');
INSERT INTO "sys_user" VALUES (2, 105, 'ry', 'charles', '00', 'app@qq.com', '15666666666', '0', NULL, '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2021-08-26', 'admin', '2021-08-26', 'admin', '2021-09-03 13:54:49', '测试员');

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO "sys_user_post" VALUES (1, 1);
INSERT INTO "sys_user_post" VALUES (2, 2);

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO "sys_user_role" VALUES (1, 1);
INSERT INTO "sys_user_role" VALUES (2, 2);

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------
INSERT INTO "qrtz_locks" VALUES ('WebAppScheduler', 'STATE_ACCESS');
INSERT INTO "qrtz_locks" VALUES ('WebAppScheduler', 'TRIGGER_ACCESS');


--- -------------------- 补充 ----------------

-- 修改字典数据表的 list_class 字段，将 danger 改为 error
UPDATE `sys_dict_data` SET `list_class` = 'error' WHERE `list_class` = 'danger';

-- 字典适配多语言
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_user_sex.male', `dict_type` = 'sys_user_sex' WHERE `dict_code` = 1;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_user_sex.female', `dict_type` = 'sys_user_sex' WHERE `dict_code` = 2;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_user_sex.unknown', `dict_type` = 'sys_user_sex' WHERE `dict_code` = 3;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_show_hide.show', `dict_type` = 'sys_show_hide' WHERE `dict_code` = 4;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_show_hide.hide', `dict_type` = 'sys_show_hide' WHERE `dict_code` = 5;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_normal_disable.normal', `dict_type` = 'sys_normal_disable' WHERE `dict_code` = 6;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_normal_disable.disable', `dict_type` = 'sys_normal_disable' WHERE `dict_code` = 7;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_yes_no.yes', `dict_type` = 'sys_yes_no' WHERE `dict_code` = 12;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_yes_no.no', `dict_type` = 'sys_yes_no' WHERE `dict_code` = 13;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_notice_type.notice', `dict_type` = 'sys_notice_type' WHERE `dict_code` = 14;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_notice_type.announcement', `dict_type` = 'sys_notice_type' WHERE `dict_code` = 15;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_notice_status.normal', `dict_type` = 'sys_notice_status' WHERE `dict_code` = 16;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_notice_status.close', `dict_type` = 'sys_notice_status' WHERE `dict_code` = 17;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.insert', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 18;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.update', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 19;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.delete', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 20;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.grant', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 21;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.export', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 22;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.import', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 23;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.force', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 24;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.gencode', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 25;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.clean', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 26;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_common_status.success', `dict_type` = 'sys_common_status' WHERE `dict_code` = 27;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_common_status.fail', `dict_type` = 'sys_common_status' WHERE `dict_code` = 28;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_oper_type.other', `dict_type` = 'sys_oper_type' WHERE `dict_code` = 29;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.password', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 30;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.sms', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 31;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.email', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 32;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.miniapp', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 33;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_grant_type.social', `dict_type` = 'sys_grant_type' WHERE `dict_code` = 34;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_device_type.pc', `dict_type` = 'sys_device_type' WHERE `dict_code` = 35;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_device_type.android', `dict_type` = 'sys_device_type' WHERE `dict_code` = 36;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_device_type.ios', `dict_type` = 'sys_device_type' WHERE `dict_code` = 37;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.sys_device_type.miniapp', `dict_type` = 'sys_device_type' WHERE `dict_code` = 38;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.revoked', `dict_type` = 'wf_business_status' WHERE `dict_code` = 39;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.draft', `dict_type` = 'wf_business_status' WHERE `dict_code` = 40;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.pending', `dict_type` = 'wf_business_status' WHERE `dict_code` = 41;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.completed', `dict_type` = 'wf_business_status' WHERE `dict_code` = 42;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.cancelled', `dict_type` = 'wf_business_status' WHERE `dict_code` = 43;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.returned', `dict_type` = 'wf_business_status' WHERE `dict_code` = 44;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_business_status.terminated', `dict_type` = 'wf_business_status' WHERE `dict_code` = 45;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_form_type.custom_form', `dict_type` = 'wf_form_type' WHERE `dict_code` = 46;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_form_type.dynamic_form', `dict_type` = 'wf_form_type' WHERE `dict_code` = 47;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.revoke', `dict_type` = 'wf_task_status' WHERE `dict_code` = 48;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.pass', `dict_type` = 'wf_task_status' WHERE `dict_code` = 49;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.pending_review', `dict_type` = 'wf_task_status' WHERE `dict_code` = 50;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.cancel', `dict_type` = 'wf_task_status' WHERE `dict_code` = 51;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.return', `dict_type` = 'wf_task_status' WHERE `dict_code` = 52;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.terminate', `dict_type` = 'wf_task_status' WHERE `dict_code` = 53;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.transfer', `dict_type` = 'wf_task_status' WHERE `dict_code` = 54;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.delegate', `dict_type` = 'wf_task_status' WHERE `dict_code` = 55;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.copy', `dict_type` = 'wf_task_status' WHERE `dict_code` = 56;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.add_sign', `dict_type` = 'wf_task_status' WHERE `dict_code` = 57;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.minus_sign', `dict_type` = 'wf_task_status' WHERE `dict_code` = 58;
UPDATE `sys_dict_data` SET `dict_label` = 'dict.wf_task_status.timeout', `dict_type` = 'wf_task_status' WHERE `dict_code` = 59;



-- 目录类型菜单
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'carbon:cloud-service-management', `menu_name` = 'route.system' WHERE `menu_id` = 1;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'stash:dashboard', `menu_name` = 'route.monitor' WHERE `menu_id` = 2;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'tabler:tools', `menu_name` = 'route.tool' WHERE `menu_id` = 3;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'material-symbols:kid-star-outline', `menu_name` = 'route.demo' WHERE `menu_id` = 5;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'tabler:building-cog', `menu_name` = 'menu.system_tenant' WHERE `menu_id` = 6;
UPDATE `sys_menu` SET `component` = 'Layout', `icon` = 'tabler:logs', `menu_name` = 'menu.system_log' WHERE `menu_id` = 108;

-- 页面类型
UPDATE `sys_menu` SET `icon` = 'ic:round-manage-accounts', `menu_name` = 'route.system_user' WHERE `menu_id` = 100;
UPDATE `sys_menu` SET `icon` = 'carbon:user-role', `menu_name` = 'route.system_role' WHERE `menu_id` = 101;
UPDATE `sys_menu` SET `icon` = 'material-symbols:route', `menu_name` = 'route.system_menu' WHERE `menu_id` = 102;
UPDATE `sys_menu` SET `icon` = 'mingcute:department-line', `menu_name` = 'route.system_dept' WHERE `menu_id` = 103;
UPDATE `sys_menu` SET `icon` = 'hugeicons:permanent-job', `menu_name` = 'route.system_post' WHERE `menu_id` = 104;
UPDATE `sys_menu` SET `icon` = 'qlementine-icons:dictionary-16', `menu_name` = 'route.system_dict' WHERE `menu_id` = 105;
UPDATE `sys_menu` SET `icon` = 'carbon:parameter', `menu_name` = 'route.system_config' WHERE `menu_id` = 106;
UPDATE `sys_menu` SET `icon` = 'solar:chat-line-outline', `menu_name` = 'route.system_notice' WHERE `menu_id` = 107;
UPDATE `sys_menu` SET `icon` = 'majesticons:status-online-line', `menu_name` = 'route.monitor_online' WHERE `menu_id` = 109;
UPDATE `sys_menu` SET `icon` = 'simple-icons:redis', `menu_name` = 'route.monitor_cache' WHERE `menu_id` = 113;
UPDATE `sys_menu` SET `icon` = 'material-symbols:code-blocks-outline', `menu_name` = 'route.tool_gen' WHERE `menu_id` = 115;
UPDATE `sys_menu` SET `icon` = 'material-symbols:attach-file', `menu_name` = 'route.system_oss' WHERE `menu_id` = 118;
UPDATE `sys_menu` SET `icon` = 'tabler:building-skyscraper', `menu_name` = 'route.system_tenant' WHERE `menu_id` = 121;
UPDATE `sys_menu` SET `icon` = 'lets-icons:package-box-alt', `menu_name` = 'route.system_tenant-package' WHERE `menu_id` = 122;
UPDATE `sys_menu` SET `icon` = 'tabler:device-imac-cog', `menu_name` = 'route.system_client' WHERE `menu_id` = 123;
UPDATE `sys_menu` SET `icon` = 'carbon:operations-record', `menu_name` = 'route.monitor_operlog' WHERE `menu_id` = 500;
UPDATE `sys_menu` SET `icon` = 'tabler:login-2', `menu_name` = 'route.monitor_logininfor' WHERE `menu_id` = 501;
UPDATE `sys_menu` SET `icon` = 'gg:debug', `menu_name` = 'route.demo_demo' WHERE `menu_id` = 1500;
UPDATE `sys_menu` SET `icon` = 'gg:debug', `menu_name` = 'route.demo_tree' WHERE `menu_id` = 1506;
UPDATE `sys_menu` SET `path` = 'oss/config', `component` = 'system/oss-config/index', `icon` = 'hugeicons:configuration-01', `menu_name` = 'route.system_oss-config' WHERE `menu_id` = 133;

INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, update_by, update_time, remark
) VALUES (
    9, 'route.about', 0, 99, 'about', 'about/index', 
    1, 1, 'C', '0', '0', '', 'fluent:book-information-24-regular',
     1, datetime('now'), NULL, NULL, '关于页面'
)
ON CONFLICT(menu_id) DO UPDATE SET
    update_time = datetime('now');

-- IFrame 类型
UPDATE `sys_menu` SET `component` = 'FrameView',  `is_frame` = 2, `icon` = 'bx:bxl-spring-boot', `menu_name` = 'menu.monitor_admin' WHERE `menu_id` = 117;
UPDATE `sys_menu` SET `component` = 'FrameView',  `is_frame` = 2, `icon` = 'gridicons:scheduled', `menu_name` = 'menu.monitor_snail-job' WHERE `menu_id` = 120;
-- 外链类型
UPDATE `sys_menu` SET `menu_name` = 'RuoYi-Vue-Plus', `order_num` = 100, `path` = 'https://gitee.com/dromara/RuoYi-Vue-Plus', `component` = 'FrameView', `icon` = 'local-icon-gitee', `remark` = 'RuoYi-Vue-Plus 仓库地址' WHERE `menu_id` = 4;
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`,  `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,  `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, 'Soybean Admin', 0, 100, 'https://github.com/soybeanjs', 'FrameView',  0, 0, 'M', '0', '0', '', 'mdi:github',  1, datetime('now'), null, null, 'Soybean Admin 仓库地址') ON CONFLICT(menu_id) DO UPDATE SET
    update_time = datetime('now');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`,  `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, 'RuoYi-Plus-Soybean', 0, 100, 'https://gitee.com/xlsea/ruoyi-plus-soybean', 'FrameView', 0, 0, 'M', '0', '0', '', 'local-icon-gitee',  1,datetime('now'), null, null, 'RuoYi-Plus-Soybean 仓库地址')ON CONFLICT(menu_id) DO UPDATE SET
    update_time = datetime('now');

-- plus-ui 需要禁用的页面
UPDATE `sys_menu` SET `status` = '1' WHERE `menu_id` IN ( '116', '130', '131', '132' );
-- 工作流需要禁用的页面
UPDATE `sys_menu` SET `status` = '1' WHERE `menu_id` IN ( '11616', '11618', '11638', '11700', '11701' );
-- 删除不支持的菜单
delete from sys_menu where menu_id in (110,111,112,114);
-- 禁用外链菜单
UPDATE `sys_menu` SET `status` = '1' WHERE `menu_id` IN ( '4', '7', '8' );