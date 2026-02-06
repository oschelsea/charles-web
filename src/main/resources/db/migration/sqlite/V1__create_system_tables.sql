-- Flyway V1: 创建系统表 (SQLite版本)
-- 此脚本用于初始化SQLite数据库表结构

PRAGMA foreign_keys = false;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
CREATE TABLE IF NOT EXISTS "gen_table" (
  "table_id" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
  "table_name" text(200),
  "table_comment" text(500),
  "sub_table_name" TEXT(64),
  "sub_table_fk_name" TEXT(64),
  "class_name" text(100),
  "tpl_category" text(200) DEFAULT 'crud',
  "package_name" text(100),
  "module_name" text(30),
  "business_name" text(30),
  "function_name" text(50),
  "function_author" text(50),
  "gen_type" TEXT(1) DEFAULT '0',
  "gen_path" TEXT(200) DEFAULT '/',
  "options" text(1000),
  "create_by" text(64),
  "create_time" text,
  "update_by" text(64),
  "update_time" text,
  "remark" text(500)
);

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
CREATE TABLE IF NOT EXISTS "gen_table_column" (
  "column_id" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
  "table_id" text(64),
  "column_name" text(200),
  "column_comment" text(500),
  "column_type" text(100),
  "java_type" text(500),
  "java_field" text(200),
  "is_pk" text(1),
  "is_increment" text(1),
  "is_required" text(1),
  "is_insert" text(1),
  "is_edit" text(1),
  "is_list" text(1),
  "is_query" text(1),
  "query_type" text(200) DEFAULT 'EQ',
  "html_type" text(200),
  "dict_type" text(200),
  "sort" integer(11),
  "create_by" text(64),
  "create_time" text,
  "update_by" text(64),
  "update_time" text
);

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_blob_triggers" (
  "sched_name" text(120) NOT NULL,
  "trigger_name" text(200) NOT NULL,
  "trigger_group" text(200) NOT NULL,
  "blob_data" blob,
  PRIMARY KEY ("sched_name", "trigger_name", "trigger_group")
);

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_calendars" (
  "sched_name" text(120) NOT NULL,
  "calendar_name" text(200) NOT NULL,
  "calendar" blob NOT NULL,
  PRIMARY KEY ("sched_name", "calendar_name")
);

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_cron_triggers" (
  "sched_name" text(120) NOT NULL,
  "trigger_name" text(200) NOT NULL,
  "trigger_group" text(200) NOT NULL,
  "cron_expression" text(200) NOT NULL,
  "time_zone_id" text(80),
  PRIMARY KEY ("sched_name", "trigger_name", "trigger_group")
);

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_fired_triggers" (
  "sched_name" text(120) NOT NULL,
  "entry_id" text(95) NOT NULL,
  "trigger_name" text(200) NOT NULL,
  "trigger_group" text(200) NOT NULL,
  "instance_name" text(200) NOT NULL,
  "fired_time" integer(20) NOT NULL,
  "sched_time" integer(20) NOT NULL,
  "priority" integer(11) NOT NULL,
  "state" text(16) NOT NULL,
  "job_name" text(200),
  "job_group" text(200),
  "is_nonconcurrent" text(1),
  "requests_recovery" text(1),
  PRIMARY KEY ("sched_name", "entry_id")
);

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_job_details" (
  "sched_name" text(120) NOT NULL,
  "job_name" text(200) NOT NULL,
  "job_group" text(200) NOT NULL,
  "description" text(250),
  "job_class_name" text(250) NOT NULL,
  "is_durable" text(1) NOT NULL,
  "is_nonconcurrent" text(1) NOT NULL,
  "is_update_data" text(1) NOT NULL,
  "requests_recovery" text(1) NOT NULL,
  "job_data" blob,
  PRIMARY KEY ("sched_name", "job_name", "job_group")
);

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_locks" (
  "sched_name" text(120) NOT NULL,
  "lock_name" text(40) NOT NULL,
  PRIMARY KEY ("sched_name", "lock_name")
);

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_paused_trigger_grps" (
  "sched_name" text(120) NOT NULL,
  "trigger_group" text(200) NOT NULL,
  PRIMARY KEY ("sched_name", "trigger_group")
);

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_scheduler_state" (
  "sched_name" text(120) NOT NULL,
  "instance_name" text(200) NOT NULL,
  "last_checkin_time" integer(20) NOT NULL,
  "checkin_interval" integer(20) NOT NULL,
  PRIMARY KEY ("sched_name", "instance_name")
);

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_simple_triggers" (
  "sched_name" text(120) NOT NULL,
  "trigger_name" text(200) NOT NULL,
  "trigger_group" text(200) NOT NULL,
  "repeat_count" integer(20) NOT NULL,
  "repeat_interval" integer(20) NOT NULL,
  "times_triggered" integer(20) NOT NULL,
  PRIMARY KEY ("sched_name", "trigger_name", "trigger_group")
);

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_simprop_triggers" (
  "sched_name" text(120) NOT NULL,
  "trigger_name" text(200) NOT NULL,
  "trigger_group" text(200) NOT NULL,
  "str_prop_1" text(512),
  "str_prop_2" text(512),
  "str_prop_3" text(512),
  "int_prop_1" integer(11),
  "int_prop_2" integer(11),
  "long_prop_1" integer(20),
  "long_prop_2" integer(20),
  "dec_prop_1" real(13,4),
  "dec_prop_2" real(13,4),
  "bool_prop_1" text(1),
  "bool_prop_2" text(1),
  PRIMARY KEY ("sched_name", "trigger_name", "trigger_group")
);

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS "qrtz_triggers" (
  "sched_name" text(120) NOT NULL,
  "trigger_name" text(200) NOT NULL,
  "trigger_group" text(200) NOT NULL,
  "job_name" text(200) NOT NULL,
  "job_group" text(200) NOT NULL,
  "description" text(250),
  "next_fire_time" integer(20),
  "prev_fire_time" integer(20),
  "priority" integer(11),
  "trigger_state" text(16) NOT NULL,
  "trigger_type" text(8) NOT NULL,
  "start_time" integer(20) NOT NULL,
  "end_time" integer(20),
  "calendar_name" text(200),
  "misfire_instr" integer(6),
  "job_data" blob,
  PRIMARY KEY ("sched_name", "trigger_name", "trigger_group")
);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_config" (
  "config_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "config_name" TEXT(100),
  "config_key" TEXT(100),
  "config_value" TEXT(500),
  "config_type" TEXT(1) DEFAULT 'N',
  "create_by" TEXT(64),
  "create_time" TEXT,
  "update_by" TEXT(64),
  "update_time" TEXT,
  "remark" TEXT(500)
);

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_dept" (
  "dept_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "parent_id" INTEGER(20) DEFAULT 0,
  "ancestors" TEXT(50),
  "dept_name" TEXT(30),
  "order_num" INTEGER(4) DEFAULT 0,
  "leader" TEXT(20) DEFAULT NULL,
  "phone" TEXT(11) DEFAULT NULL,
  "email" TEXT(50) DEFAULT NULL,
  "status" TEXT(1) DEFAULT '0',
  "del_flag" TEXT(1) DEFAULT '0',
  "create_by" TEXT(64) DEFAULT '',
  "create_time" TEXT,
  "update_by" TEXT(64) DEFAULT '',
  "update_time" TEXT
);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_dict_data" (
  "dict_code" INTEGER(20) NOT NULL,
  "dict_sort" INTEGER(4) DEFAULT 0,
  "dict_label" TEXT(100),
  "dict_value" TEXT(100),
  "dict_type" TEXT(100),
  "css_class" TEXT(100),
  "list_class" TEXT(100),
  "is_default" TEXT(1) DEFAULT 'N',
  "status" TEXT(1) DEFAULT '0',
  "create_by" TEXT(64),
  "create_time" TEXT,
  "update_by" TEXT(64),
  "update_time" TEXT,
  "remark" TEXT(254),
  PRIMARY KEY ("dict_code")
);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_dict_type" (
  "dict_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "dict_name" TEXT(100),
  "dict_type" TEXT(100),
  "status" TEXT(1) DEFAULT '0',
  "create_by" TEXT(64),
  "create_time" TEXT,
  "update_by" TEXT(64),
  "update_time" TEXT,
  "remark" TEXT(500)
);

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_job" (
  "job_id" INTEGER(20) NOT NULL,
  "job_name" TEXT(64) NOT NULL,
  "job_group" TEXT(64) NOT NULL DEFAULT 'DEFAULT',
  "invoke_target" TEXT(500) NOT NULL,
  "cron_expression" TEXT(255),
  "misfire_policy" TEXT(20) DEFAULT '3',
  "concurrent" TEXT(1) DEFAULT '1',
  "status" TEXT(1) DEFAULT '0',
  "create_by" TEXT(64),
  "create_time" TEXT,
  "update_by" TEXT(64),
  "update_time" TEXT,
  "remark" TEXT(500),
  PRIMARY KEY ("job_id", "job_name", "job_group")
);

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_job_log" (
  "job_log_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "job_name" TEXT(64) NOT NULL,
  "job_group" TEXT(64) NOT NULL,
  "invoke_target" TEXT(500) NOT NULL,
  "job_message" TEXT(500),
  "status" TEXT(1) DEFAULT '0',
  "exception_info" TEXT(2000),
  "create_time" TEXT
);

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_logininfor" (
  "info_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "user_name" TEXT(50),
  "ipaddr" TEXT(128),
  "login_location" TEXT(255),
  "browser" TEXT(50),
  "os" TEXT(50),
  "status" TEXT(1) DEFAULT '0',
  "msg" TEXT(255),
  "login_time" TEXT
);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_menu" (
  "menu_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "menu_name" TEXT(50) NOT NULL,
  "parent_id" INTEGER(20) DEFAULT 0,
  "order_num" INTEGER(4) DEFAULT 0,
  "path" TEXT(200),
  "component" TEXT(255) DEFAULT NULL,
  "is_frame" INTEGER(1) DEFAULT 1,
  "is_cache" INTEGER(1) DEFAULT 0,
  "menu_type" TEXT(1) DEFAULT '',
  "visible" TEXT(1) DEFAULT '0',
  "status" TEXT(1) DEFAULT '0',
  "perms" TEXT(100),
  "icon" TEXT(100) DEFAULT '#',
  "create_by" TEXT(64),
  "create_time" TEXT,
  "update_by" TEXT(64),
  "update_time" TEXT,
  "remark" TEXT(500)
);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_notice" (
  "notice_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "notice_title" TEXT(50) NOT NULL,
  "notice_type" TEXT(1) NOT NULL,
  "notice_content" TEXT(2000),
  "status" TEXT(1) DEFAULT '0',
  "create_by" TEXT(64),
  "create_time" TEXT,
  "update_by" TEXT(64),
  "update_time" TEXT,
  "remark" TEXT(255)
);

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_oper_log" (
  "oper_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "title" TEXT(50),
  "business_type" INTEGER(2) DEFAULT 0,
  "method" TEXT(100),
  "request_method" TEXT(10),
  "operator_type" INTEGER(1) DEFAULT 0,
  "oper_name" TEXT(50),
  "dept_name" TEXT(50),
  "oper_url" TEXT(255),
  "oper_ip" TEXT(128),
  "oper_location" TEXT(255),
  "oper_param" TEXT(2000),
  "json_result" TEXT(2000),
  "status" INTEGER(1) DEFAULT 0,
  "error_msg" TEXT(2000),
  "oper_time" TEXT
);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_post" (
  "post_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "post_code" TEXT(64) NOT NULL,
  "post_name" TEXT(50) NOT NULL,
  "post_sort" INTEGER(4) NOT NULL,
  "status" TEXT(1) NOT NULL,
  "create_by" TEXT(64),
  "create_time" TEXT,
  "update_by" TEXT(64),
  "update_time" TEXT,
  "remark" TEXT(500)
);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_role" (
  "role_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "role_name" TEXT(30) NOT NULL,
  "role_key" TEXT(100) NOT NULL,
  "role_sort" INTEGER(4) NOT NULL,
  "data_scope" TEXT(1) DEFAULT '1',
  "menu_check_strictly" INTEGER(1) DEFAULT 1,
  "dept_check_strictly" INTEGER(1) DEFAULT 1,
  "status" TEXT(1) NOT NULL,
  "del_flag" TEXT(1) DEFAULT '0',
  "create_by" TEXT(64),
  "create_time" TEXT,
  "update_by" TEXT(64),
  "update_time" TEXT,
  "remark" TEXT(500)
);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_role_dept" (
  "role_id" INTEGER(20) NOT NULL,
  "dept_id" INTEGER(20) NOT NULL
);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_role_menu" (
  "role_id" INTEGER(20) NOT NULL,
  "menu_id" INTEGER(20) NOT NULL
);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_user" (
  "user_id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "dept_id" INTEGER(20),
  "user_name" TEXT(30) NOT NULL,
  "nick_name" TEXT(30) NOT NULL,
  "user_type" TEXT(2) DEFAULT '00',
  "email" TEXT(50),
  "phonenumber" TEXT(11),
  "sex" TEXT(1) DEFAULT '0',
  "avatar" TEXT(100),
  "password" TEXT(100),
  "status" TEXT(1) DEFAULT '0',
  "del_flag" TEXT(1) DEFAULT '0',
  "login_ip" TEXT(128),
  "login_date" TEXT,
  "create_by" TEXT(64),
  "create_time" TEXT,
  "update_by" TEXT(64),
  "update_time" TEXT,
  "remark" TEXT(500)
);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_user_post" (
  "user_id" INTEGER(20) NOT NULL,
  "post_id" INTEGER(20) NOT NULL
);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS "sys_user_role" (
  "user_id" INTEGER(20) NOT NULL,
  "role_id" INTEGER(20)
);

-- ----------------------------
-- Indexes structure for table qrtz_triggers
-- ----------------------------
CREATE INDEX IF NOT EXISTS "sched_name"
ON "qrtz_triggers" (
  "sched_name" ASC,
  "job_name" ASC,
  "job_group" ASC
);

PRAGMA foreign_keys = true;
