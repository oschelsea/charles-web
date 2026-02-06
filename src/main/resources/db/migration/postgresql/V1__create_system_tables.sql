-- Flyway V1: 创建系统表 (PostgreSQL版本)
-- 此脚本用于初始化PostgreSQL数据库表结构

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
CREATE TABLE IF NOT EXISTS gen_table (
  table_id SERIAL PRIMARY KEY,
  table_name VARCHAR(200),
  table_comment VARCHAR(500),
  sub_table_name VARCHAR(64),
  sub_table_fk_name VARCHAR(64),
  class_name VARCHAR(100),
  tpl_category VARCHAR(200) DEFAULT 'crud',
  package_name VARCHAR(100),
  module_name VARCHAR(30),
  business_name VARCHAR(30),
  function_name VARCHAR(50),
  function_author VARCHAR(50),
  gen_type CHAR(1) DEFAULT '0',
  gen_path VARCHAR(200) DEFAULT '/',
  options VARCHAR(1000),
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(500)
);

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
CREATE TABLE IF NOT EXISTS gen_table_column (
  column_id SERIAL PRIMARY KEY,
  table_id VARCHAR(64),
  column_name VARCHAR(200),
  column_comment VARCHAR(500),
  column_type VARCHAR(100),
  java_type VARCHAR(500),
  java_field VARCHAR(200),
  is_pk CHAR(1),
  is_increment CHAR(1),
  is_required CHAR(1),
  is_insert CHAR(1),
  is_edit CHAR(1),
  is_list CHAR(1),
  is_query CHAR(1),
  query_type VARCHAR(200) DEFAULT 'EQ',
  html_type VARCHAR(200),
  dict_type VARCHAR(200),
  sort INTEGER,
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP
);

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_job_details (
  sched_name VARCHAR(120) NOT NULL,
  job_name VARCHAR(200) NOT NULL,
  job_group VARCHAR(200) NOT NULL,
  description VARCHAR(250),
  job_class_name VARCHAR(250) NOT NULL,
  is_durable CHAR(1) NOT NULL,
  is_nonconcurrent CHAR(1) NOT NULL,
  is_update_data CHAR(1) NOT NULL,
  requests_recovery CHAR(1) NOT NULL,
  job_data BYTEA,
  PRIMARY KEY (sched_name, job_name, job_group)
);

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_triggers (
  sched_name VARCHAR(120) NOT NULL,
  trigger_name VARCHAR(200) NOT NULL,
  trigger_group VARCHAR(200) NOT NULL,
  job_name VARCHAR(200) NOT NULL,
  job_group VARCHAR(200) NOT NULL,
  description VARCHAR(250),
  next_fire_time BIGINT,
  prev_fire_time BIGINT,
  priority INTEGER,
  trigger_state VARCHAR(16) NOT NULL,
  trigger_type VARCHAR(8) NOT NULL,
  start_time BIGINT NOT NULL,
  end_time BIGINT,
  calendar_name VARCHAR(200),
  misfire_instr SMALLINT,
  job_data BYTEA,
  PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_blob_triggers (
  sched_name VARCHAR(120) NOT NULL,
  trigger_name VARCHAR(200) NOT NULL,
  trigger_group VARCHAR(200) NOT NULL,
  blob_data BYTEA,
  PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_calendars (
  sched_name VARCHAR(120) NOT NULL,
  calendar_name VARCHAR(200) NOT NULL,
  calendar BYTEA NOT NULL,
  PRIMARY KEY (sched_name, calendar_name)
);

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_cron_triggers (
  sched_name VARCHAR(120) NOT NULL,
  trigger_name VARCHAR(200) NOT NULL,
  trigger_group VARCHAR(200) NOT NULL,
  cron_expression VARCHAR(200) NOT NULL,
  time_zone_id VARCHAR(80),
  PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_fired_triggers (
  sched_name VARCHAR(120) NOT NULL,
  entry_id VARCHAR(95) NOT NULL,
  trigger_name VARCHAR(200) NOT NULL,
  trigger_group VARCHAR(200) NOT NULL,
  instance_name VARCHAR(200) NOT NULL,
  fired_time BIGINT NOT NULL,
  sched_time BIGINT NOT NULL,
  priority INTEGER NOT NULL,
  state VARCHAR(16) NOT NULL,
  job_name VARCHAR(200),
  job_group VARCHAR(200),
  is_nonconcurrent CHAR(1),
  requests_recovery CHAR(1),
  PRIMARY KEY (sched_name, entry_id)
);

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_locks (
  sched_name VARCHAR(120) NOT NULL,
  lock_name VARCHAR(40) NOT NULL,
  PRIMARY KEY (sched_name, lock_name)
);

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_paused_trigger_grps (
  sched_name VARCHAR(120) NOT NULL,
  trigger_group VARCHAR(200) NOT NULL,
  PRIMARY KEY (sched_name, trigger_group)
);

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_scheduler_state (
  sched_name VARCHAR(120) NOT NULL,
  instance_name VARCHAR(200) NOT NULL,
  last_checkin_time BIGINT NOT NULL,
  checkin_interval BIGINT NOT NULL,
  PRIMARY KEY (sched_name, instance_name)
);

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_simple_triggers (
  sched_name VARCHAR(120) NOT NULL,
  trigger_name VARCHAR(200) NOT NULL,
  trigger_group VARCHAR(200) NOT NULL,
  repeat_count BIGINT NOT NULL,
  repeat_interval BIGINT NOT NULL,
  times_triggered BIGINT NOT NULL,
  PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
CREATE TABLE IF NOT EXISTS qrtz_simprop_triggers (
  sched_name VARCHAR(120) NOT NULL,
  trigger_name VARCHAR(200) NOT NULL,
  trigger_group VARCHAR(200) NOT NULL,
  str_prop_1 VARCHAR(512),
  str_prop_2 VARCHAR(512),
  str_prop_3 VARCHAR(512),
  int_prop_1 INTEGER,
  int_prop_2 INTEGER,
  long_prop_1 BIGINT,
  long_prop_2 BIGINT,
  dec_prop_1 DECIMAL(13,4),
  dec_prop_2 DECIMAL(13,4),
  bool_prop_1 CHAR(1),
  bool_prop_2 CHAR(1),
  PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_config (
  config_id SERIAL PRIMARY KEY,
  config_name VARCHAR(100),
  config_key VARCHAR(100),
  config_value VARCHAR(500),
  config_type CHAR(1) DEFAULT 'N',
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(500)
);

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dept (
  dept_id SERIAL PRIMARY KEY,
  parent_id BIGINT DEFAULT 0,
  ancestors VARCHAR(50),
  dept_name VARCHAR(30),
  order_num INTEGER DEFAULT 0,
  leader VARCHAR(20),
  phone VARCHAR(11),
  email VARCHAR(50),
  status CHAR(1) DEFAULT '0',
  del_flag CHAR(1) DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP
);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dict_data (
  dict_code SERIAL PRIMARY KEY,
  dict_sort INTEGER DEFAULT 0,
  dict_label VARCHAR(100),
  dict_value VARCHAR(100),
  dict_type VARCHAR(100),
  css_class VARCHAR(100),
  list_class VARCHAR(100),
  is_default CHAR(1) DEFAULT 'N',
  status CHAR(1) DEFAULT '0',
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(254)
);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dict_type (
  dict_id SERIAL PRIMARY KEY,
  dict_name VARCHAR(100),
  dict_type VARCHAR(100),
  status CHAR(1) DEFAULT '0',
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(500)
);

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_job (
  job_id BIGINT NOT NULL,
  job_name VARCHAR(64) NOT NULL,
  job_group VARCHAR(64) NOT NULL DEFAULT 'DEFAULT',
  invoke_target VARCHAR(500) NOT NULL,
  cron_expression VARCHAR(255),
  misfire_policy VARCHAR(20) DEFAULT '3',
  concurrent CHAR(1) DEFAULT '1',
  status CHAR(1) DEFAULT '0',
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(500),
  PRIMARY KEY (job_id, job_name, job_group)
);

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_job_log (
  job_log_id SERIAL PRIMARY KEY,
  job_name VARCHAR(64) NOT NULL,
  job_group VARCHAR(64) NOT NULL,
  invoke_target VARCHAR(500) NOT NULL,
  job_message VARCHAR(500),
  status CHAR(1) DEFAULT '0',
  exception_info VARCHAR(2000),
  create_time TIMESTAMP
);

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_logininfor (
  info_id SERIAL PRIMARY KEY,
  user_name VARCHAR(50),
  ipaddr VARCHAR(128),
  login_location VARCHAR(255),
  browser VARCHAR(50),
  os VARCHAR(50),
  status CHAR(1) DEFAULT '0',
  msg VARCHAR(255),
  login_time TIMESTAMP
);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_menu (
  menu_id SERIAL PRIMARY KEY,
  menu_name VARCHAR(50) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  order_num INTEGER DEFAULT 0,
  path VARCHAR(200),
  component VARCHAR(255),
  is_frame INTEGER DEFAULT 1,
  is_cache INTEGER DEFAULT 0,
  menu_type CHAR(1) DEFAULT '',
  visible CHAR(1) DEFAULT '0',
  status CHAR(1) DEFAULT '0',
  perms VARCHAR(100),
  icon VARCHAR(100) DEFAULT '#',
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(500)
);

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_notice (
  notice_id SERIAL PRIMARY KEY,
  notice_title VARCHAR(50) NOT NULL,
  notice_type CHAR(1) NOT NULL,
  notice_content VARCHAR(2000),
  status CHAR(1) DEFAULT '0',
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(255)
);

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_oper_log (
  oper_id SERIAL PRIMARY KEY,
  title VARCHAR(50),
  business_type INTEGER DEFAULT 0,
  method VARCHAR(100),
  request_method VARCHAR(10),
  operator_type INTEGER DEFAULT 0,
  oper_name VARCHAR(50),
  dept_name VARCHAR(50),
  oper_url VARCHAR(255),
  oper_ip VARCHAR(128),
  oper_location VARCHAR(255),
  oper_param VARCHAR(2000),
  json_result VARCHAR(2000),
  status INTEGER DEFAULT 0,
  error_msg VARCHAR(2000),
  oper_time TIMESTAMP
);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_post (
  post_id SERIAL PRIMARY KEY,
  post_code VARCHAR(64) NOT NULL,
  post_name VARCHAR(50) NOT NULL,
  post_sort INTEGER NOT NULL,
  status CHAR(1) NOT NULL,
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(500)
);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role (
  role_id SERIAL PRIMARY KEY,
  role_name VARCHAR(30) NOT NULL,
  role_key VARCHAR(100) NOT NULL,
  role_sort INTEGER NOT NULL,
  data_scope CHAR(1) DEFAULT '1',
  menu_check_strictly INTEGER DEFAULT 1,
  dept_check_strictly INTEGER DEFAULT 1,
  status CHAR(1) NOT NULL,
  del_flag CHAR(1) DEFAULT '0',
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(500)
);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role_dept (
  role_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL
);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role_menu (
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL
);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user (
  user_id SERIAL PRIMARY KEY,
  dept_id BIGINT,
  user_name VARCHAR(30) NOT NULL,
  nick_name VARCHAR(30) NOT NULL,
  user_type VARCHAR(2) DEFAULT '00',
  email VARCHAR(50),
  phonenumber VARCHAR(11),
  sex CHAR(1) DEFAULT '0',
  avatar VARCHAR(100),
  password VARCHAR(100),
  status CHAR(1) DEFAULT '0',
  del_flag CHAR(1) DEFAULT '0',
  login_ip VARCHAR(128),
  login_date TIMESTAMP,
  create_by VARCHAR(64),
  create_time TIMESTAMP,
  update_by VARCHAR(64),
  update_time TIMESTAMP,
  remark VARCHAR(500)
);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_post (
  user_id BIGINT NOT NULL,
  post_id BIGINT NOT NULL
);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT
);

-- ----------------------------
-- Indexes for qrtz_triggers
-- ----------------------------
CREATE INDEX IF NOT EXISTS idx_qrtz_triggers_sched_name ON qrtz_triggers (sched_name, job_name, job_group);
