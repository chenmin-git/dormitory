-- ======================================================================
-- 智能宿舍管理系统 数据库脚本
-- SpringBoot + Vue3 + 讯飞星火AI 毕业设计
-- 初始账号密码统一为：123456 （BCrypt 加密存储）
-- ======================================================================

CREATE DATABASE IF NOT EXISTS dormitory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dormitory;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    real_name   VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    role        VARCHAR(20)  NOT NULL COMMENT '角色:ADMIN/MANAGER/STUDENT',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    gender      VARCHAR(10)  DEFAULT '男' COMMENT '性别',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1正常 0禁用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ----------------------------
-- 2. 楼栋表
-- ----------------------------
DROP TABLE IF EXISTS building;
CREATE TABLE building (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50) NOT NULL COMMENT '楼栋名称',
    gender_type VARCHAR(10) NOT NULL DEFAULT '男' COMMENT '住宿性别:男/女',
    manager_id  BIGINT      DEFAULT NULL COMMENT '宿管员用户id',
    floor_count INT         NOT NULL DEFAULT 6 COMMENT '楼层数',
    remark      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME    DEFAULT NULL,
    update_time DATETIME    DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '楼栋表';

-- ----------------------------
-- 3. 房间表
-- ----------------------------
DROP TABLE IF EXISTS room;
CREATE TABLE room (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    building_id BIGINT      NOT NULL COMMENT '楼栋id',
    room_no     VARCHAR(20) NOT NULL COMMENT '房间号',
    floor       INT         NOT NULL DEFAULT 1 COMMENT '楼层',
    capacity    INT         NOT NULL DEFAULT 4 COMMENT '可住人数',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态:1可用 0停用',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME    DEFAULT NULL,
    update_time DATETIME    DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_building (building_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '房间表';

-- ----------------------------
-- 4. 床位表
-- ----------------------------
DROP TABLE IF EXISTS bed;
CREATE TABLE bed (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    room_id     BIGINT      NOT NULL COMMENT '房间id',
    bed_no      VARCHAR(20) NOT NULL COMMENT '床位号',
    student_id  BIGINT      DEFAULT NULL COMMENT '入住学生id(空=未分配)',
    status      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态:0空闲 1已住',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME    DEFAULT NULL,
    update_time DATETIME    DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_room (room_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '床位表';

-- ----------------------------
-- 5. 学生表
-- ----------------------------
DROP TABLE IF EXISTS student;
CREATE TABLE student (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT      NOT NULL COMMENT '关联用户id',
    student_no  VARCHAR(30) NOT NULL COMMENT '学号',
    real_name   VARCHAR(50) NOT NULL COMMENT '姓名',
    gender      VARCHAR(10) DEFAULT '男' COMMENT '性别',
    class_name  VARCHAR(50) DEFAULT NULL COMMENT '班级',
    major       VARCHAR(50) DEFAULT NULL COMMENT '专业',
    college     VARCHAR(50) DEFAULT NULL COMMENT '学院',
    phone       VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    bed_id      BIGINT      DEFAULT NULL COMMENT '床位id(空=未入住)',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME    DEFAULT NULL,
    update_time DATETIME    DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学生表';

-- ----------------------------
-- 6. 报修表
-- ----------------------------
DROP TABLE IF EXISTS repair;
CREATE TABLE repair (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id  BIGINT       NOT NULL COMMENT '报修学生id',
    room_id     BIGINT       DEFAULT NULL COMMENT '房间id',
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    description VARCHAR(500) NOT NULL COMMENT '问题描述',
    ai_category VARCHAR(20)  DEFAULT NULL COMMENT 'AI分类:水电/网络/家具/门窗/其他',
    ai_priority VARCHAR(20)  DEFAULT NULL COMMENT 'AI优先级:紧急/一般/低',
    status      VARCHAR(20)  NOT NULL DEFAULT '待派单' COMMENT '状态:待派单/维修中/已完成',
    handler_id  BIGINT       DEFAULT NULL COMMENT '处理人(宿管)id',
    handle_remark VARCHAR(255) DEFAULT NULL COMMENT '处理备注',
    finish_time DATETIME     DEFAULT NULL COMMENT '完成时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_student (student_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '报修表';

-- ----------------------------
-- 7. 请假/晚归记录表
-- ----------------------------
DROP TABLE IF EXISTS leave_record;
CREATE TABLE leave_record (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id  BIGINT       NOT NULL COMMENT '学生id',
    type        VARCHAR(20)  NOT NULL DEFAULT '请假' COMMENT '类型:请假/晚归',
    reason      VARCHAR(255) NOT NULL COMMENT '事由',
    start_time  DATETIME     NOT NULL COMMENT '开始时间',
    end_time    DATETIME     NOT NULL COMMENT '结束时间',
    status      VARCHAR(20)  NOT NULL DEFAULT '待审批' COMMENT '状态:待审批/通过/驳回',
    approver_id BIGINT       DEFAULT NULL COMMENT '审批人id',
    approve_remark VARCHAR(255) DEFAULT NULL COMMENT '审批意见',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_student (student_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '请假/晚归记录表';

-- ----------------------------
-- 8. 公告表
-- ----------------------------
DROP TABLE IF EXISTS announcement;
CREATE TABLE announcement (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(100) NOT NULL COMMENT '标题',
    content      TEXT         NOT NULL COMMENT '内容',
    publisher_id BIGINT       NOT NULL COMMENT '发布人id',
    target       VARCHAR(50)  NOT NULL DEFAULT '全体' COMMENT '范围:全体/楼栋名',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time  DATETIME     DEFAULT NULL,
    update_time  DATETIME     DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告表';

-- ----------------------------
-- 9. 卫生检查表
-- ----------------------------
DROP TABLE IF EXISTS hygiene_check;
CREATE TABLE hygiene_check (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    room_id     BIGINT       NOT NULL COMMENT '房间id',
    checker_id  BIGINT       NOT NULL COMMENT '检查人(宿管)id',
    score       INT          NOT NULL DEFAULT 0 COMMENT '评分(0-100)',
    comment     VARCHAR(255) DEFAULT NULL COMMENT '评语',
    check_date  DATE         NOT NULL COMMENT '检查日期',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_room (room_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '卫生检查表';

-- ----------------------------
-- 10. FAQ 知识库（智能问答上下文）
-- ----------------------------
DROP TABLE IF EXISTS faq;
CREATE TABLE faq (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    question    VARCHAR(255) NOT NULL COMMENT '问题',
    answer      VARCHAR(500) NOT NULL COMMENT '答案',
    category    VARCHAR(50)  DEFAULT NULL COMMENT '分类',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'FAQ知识库';

-- ----------------------------
-- 11. AI 对话记录表
-- ----------------------------
DROP TABLE IF EXISTS ai_chat_log;
CREATE TABLE ai_chat_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '用户id',
    question    VARCHAR(500) NOT NULL COMMENT '问题',
    answer      TEXT         COMMENT '回答',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI对话记录表';

-- ----------------------------
-- 12. AI 数据分析报告历史
-- ----------------------------
DROP TABLE IF EXISTS ai_report;
CREATE TABLE ai_report (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    content     TEXT         NOT NULL COMMENT '报告内容(Markdown)',
    creator_id  BIGINT       DEFAULT NULL COMMENT '生成人id',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI数据分析报告历史';

-- ======================================================================
-- 初始化数据  （所有账号密码均为 123456）
-- ======================================================================

-- 用户：1超管 + 2宿管 + 5学生
INSERT INTO sys_user (id, username, password, real_name, role, phone, gender, status, create_time, update_time) VALUES
(1, 'admin',    '$2b$10$Y4J/Pt2WDOWHYWMph6jgxOF6j9D64vi717p6OjXMKiqZR4ZbgKNAy', '超级管理员', 'ADMIN',   '13800000000', '男', 1, NOW(), NOW()),
(2, 'manager1', '$2b$10$Y4J/Pt2WDOWHYWMph6jgxOF6j9D64vi717p6OjXMKiqZR4ZbgKNAy', '张宿管',     'MANAGER', '13800000001', '男', 1, NOW(), NOW()),
(3, 'manager2', '$2b$10$Y4J/Pt2WDOWHYWMph6jgxOF6j9D64vi717p6OjXMKiqZR4ZbgKNAy', '李宿管',     'MANAGER', '13800000002', '女', 1, NOW(), NOW()),
(4, 'stu001',   '$2b$10$Y4J/Pt2WDOWHYWMph6jgxOF6j9D64vi717p6OjXMKiqZR4ZbgKNAy', '王小明',     'STUDENT', '13900000001', '男', 1, NOW(), NOW()),
(5, 'stu002',   '$2b$10$Y4J/Pt2WDOWHYWMph6jgxOF6j9D64vi717p6OjXMKiqZR4ZbgKNAy', '陈大壮',     'STUDENT', '13900000002', '男', 1, NOW(), NOW()),
(6, 'stu003',   '$2b$10$Y4J/Pt2WDOWHYWMph6jgxOF6j9D64vi717p6OjXMKiqZR4ZbgKNAy', '赵静',       'STUDENT', '13900000003', '女', 1, NOW(), NOW()),
(7, 'stu004',   '$2b$10$Y4J/Pt2WDOWHYWMph6jgxOF6j9D64vi717p6OjXMKiqZR4ZbgKNAy', '刘洋',       'STUDENT', '13900000004', '男', 1, NOW(), NOW()),
(8, 'stu005',   '$2b$10$Y4J/Pt2WDOWHYWMph6jgxOF6j9D64vi717p6OjXMKiqZR4ZbgKNAy', '孙丽',       'STUDENT', '13900000005', '女', 1, NOW(), NOW());

-- 楼栋
INSERT INTO building (id, name, gender_type, manager_id, floor_count, remark, create_time, update_time) VALUES
(1, '1号楼(男)', '男', 2, 6, '男生公寓', NOW(), NOW()),
(2, '2号楼(女)', '女', 3, 6, '女生公寓', NOW(), NOW());

-- 房间（楼栋1：101-104，楼栋2：201-202）
INSERT INTO room (id, building_id, room_no, floor, capacity, status, create_time, update_time) VALUES
(1, 1, '101', 1, 4, 1, NOW(), NOW()),
(2, 1, '102', 1, 4, 1, NOW(), NOW()),
(3, 1, '103', 1, 4, 1, NOW(), NOW()),
(4, 1, '104', 1, 4, 1, NOW(), NOW()),
(5, 2, '201', 2, 4, 1, NOW(), NOW()),
(6, 2, '202', 2, 4, 1, NOW(), NOW());

-- 床位（房间1有4个床位，部分已分配；其余房间各4床位空闲）
INSERT INTO bed (id, room_id, bed_no, student_id, status, create_time, update_time) VALUES
(1, 1, '1床', 1, 1, NOW(), NOW()),
(2, 1, '2床', 2, 1, NOW(), NOW()),
(3, 1, '3床', NULL, 0, NOW(), NOW()),
(4, 1, '4床', NULL, 0, NOW(), NOW()),
(5, 2, '1床', NULL, 0, NOW(), NOW()),
(6, 2, '2床', NULL, 0, NOW(), NOW()),
(7, 2, '3床', NULL, 0, NOW(), NOW()),
(8, 2, '4床', NULL, 0, NOW(), NOW()),
(9, 5, '1床', 3, 1, NOW(), NOW()),
(10, 5, '2床', NULL, 0, NOW(), NOW()),
(11, 5, '3床', NULL, 0, NOW(), NOW()),
(12, 5, '4床', NULL, 0, NOW(), NOW());

-- 学生（stu001,stu002 已入住男生楼101；stu003 入住女生楼201；stu004,stu005 未入住）
INSERT INTO student (id, user_id, student_no, real_name, gender, class_name, major, college, phone, bed_id, create_time, update_time) VALUES
(1, 4, '2021001', '王小明', '男', '计科2101', '计算机科学与技术', '信息学院', '13900000001', 1,    NOW(), NOW()),
(2, 5, '2021002', '陈大壮', '男', '计科2101', '计算机科学与技术', '信息学院', '13900000002', 2,    NOW(), NOW()),
(3, 6, '2021003', '赵静',   '女', '软件2102', '软件工程',         '信息学院', '13900000003', 9,    NOW(), NOW()),
(4, 7, '2021004', '刘洋',   '男', '网络2103', '网络工程',         '信息学院', '13900000004', NULL, NOW(), NOW()),
(5, 8, '2021005', '孙丽',   '女', '软件2102', '软件工程',         '信息学院', '13900000005', NULL, NOW(), NOW());

-- 报修示例
INSERT INTO repair (id, student_id, room_id, title, description, ai_category, ai_priority, status, handler_id, create_time, update_time) VALUES
(1, 1, 1, '宿舍水龙头漏水', '卫生间水龙头一直滴水，关不紧，地上都是水', '水电', '紧急', '维修中', 2, NOW(), NOW()),
(2, 2, 1, '网线无法上网',   '网口插上后电脑显示无网络连接，重启也没用',   '网络', '一般', '待派单', NULL, NOW(), NOW()),
(3, 3, 5, '衣柜门坏了',     '衣柜柜门合页松动，门关不上',                 '家具', '低',   '已完成', 3, NOW(), NOW());

-- 请假/晚归示例
INSERT INTO leave_record (id, student_id, type, reason, start_time, end_time, status, approver_id, create_time, update_time) VALUES
(1, 1, '请假', '周末回家',     '2026-06-06 08:00:00', '2026-06-08 20:00:00', '通过',   2, NOW(), NOW()),
(2, 2, '晚归', '社团活动结束晚', '2026-06-07 22:30:00', '2026-06-07 23:30:00', '待审批', NULL, NOW(), NOW());

-- 公告示例
INSERT INTO announcement (id, title, content, publisher_id, target, create_time, update_time) VALUES
(1, '关于做好期末宿舍安全检查的通知', '请各位同学配合宿管做好用电安全自查，禁止使用大功率电器。', 1, '全体', NOW(), NOW()),
(2, '本周日宿舍卫生大扫除', '本周日上午9点进行宿舍卫生大检查，请提前打扫。', 2, '1号楼(男)', NOW(), NOW());

-- 卫生检查示例
INSERT INTO hygiene_check (id, room_id, checker_id, score, comment, check_date, create_time, update_time) VALUES
(1, 1, 2, 85, '整体整洁，地面有少量垃圾', '2026-06-01', NOW(), NOW()),
(2, 2, 2, 92, '非常干净，继续保持', '2026-06-01', NOW(), NOW()),
(3, 5, 3, 78, '桌面较乱，需整理', '2026-06-01', NOW(), NOW());

-- FAQ 知识库
INSERT INTO faq (id, question, answer, category, create_time, update_time) VALUES
(1, '宿舍门禁时间是几点？', '宿舍门禁时间为每晚23:00，超过时间需办理晚归登记。', '日常管理', NOW(), NOW()),
(2, '如何申请报修？', '登录系统后在"报修管理"中提交报修单，系统会自动分类并由宿管派单处理。', '报修', NOW(), NOW()),
(3, '可以使用哪些电器？', '允许使用台灯、电脑、手机充电器等小功率电器，禁止使用热得快、电磁炉等大功率电器。', '安全', NOW(), NOW()),
(4, '如何请假？', '在"请假/晚归"中提交申请，填写事由和时间，等待宿管审批通过。', '请假', NOW(), NOW()),
(5, '宿舍卫生检查标准是什么？', '检查内容包括地面、桌面、床铺整洁度等，满分100分，低于60分需整改。', '卫生', NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;
