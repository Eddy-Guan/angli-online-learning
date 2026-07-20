CREATE DATABASE IF NOT EXISTS angli_online DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE angli_online;

DROP TABLE IF EXISTS push_records;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS daily_summaries;
DROP TABLE IF EXISTS checkins;
DROP TABLE IF EXISTS evaluations;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS course_chapters;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(11) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role ENUM('PARENT', 'TEACHER', 'ADMIN') NOT NULL COMMENT '角色',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    tags VARCHAR(200) DEFAULT NULL COMMENT '个人标签(最多3个，逗号分隔)',
    status ENUM('PENDING', 'ACTIVE', 'DISABLED') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE students (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT UNSIGNED NOT NULL COMMENT '家长ID',
    name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    age INT DEFAULT NULL COMMENT '年龄',
    grade VARCHAR(20) DEFAULT NULL COMMENT '年级',
    gender ENUM('MALE', 'FEMALE') DEFAULT NULL COMMENT '性别',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

CREATE TABLE courses (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT UNSIGNED NOT NULL COMMENT '授课老师ID',
    title VARCHAR(200) NOT NULL COMMENT '课程名称',
    description TEXT DEFAULT NULL COMMENT '课程简介',
    category VARCHAR(50) DEFAULT NULL COMMENT '课程分类',
    price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '课程价格',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    total_hours INT NOT NULL COMMENT '总课时',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
    status ENUM('PENDING', 'PUBLISHED', 'ENDED') DEFAULT 'PENDING' COMMENT '状态',
    is_recommended TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
    recommend_order INT DEFAULT 0 COMMENT '推荐排序',
    enrollment_count INT DEFAULT 0 COMMENT '报名人数',
    favorite_count INT DEFAULT 0 COMMENT '收藏人数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status),
    INDEX idx_is_recommended (is_recommended),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

CREATE TABLE course_chapters (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    chapter_name VARCHAR(200) NOT NULL COMMENT '章节名称',
    chapter_order INT NOT NULL COMMENT '章节排序',
    duration VARCHAR(20) DEFAULT NULL COMMENT '时长(分钟)',
    content TEXT DEFAULT NULL COMMENT '章节内容',
    is_completed TINYINT(1) DEFAULT 0 COMMENT '是否完成',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_course_id (course_id),
    INDEX idx_chapter_order (chapter_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程章节表';

CREATE TABLE orders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '下单用户ID',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    student_id BIGINT UNSIGNED DEFAULT NULL COMMENT '学生ID(家长代下单)',
    amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    status ENUM('PENDING', 'PAID', 'REFUNDED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '订单状态',
    pay_method ENUM('WECHAT', 'ALIPAY') DEFAULT NULL COMMENT '支付方式',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE enrollments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    student_id BIGINT UNSIGNED DEFAULT NULL COMMENT '学生ID',
    status ENUM('ENROLLED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'ENROLLED' COMMENT '选课状态',
    progress INT DEFAULT 0 COMMENT '学习进度(%)',
    enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_course (user_id, course_id, student_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课记录表';

CREATE TABLE favorites (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_course (user_id, course_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

CREATE TABLE evaluations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '评价用户ID',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    student_id BIGINT UNSIGNED DEFAULT NULL COMMENT '学生ID',
    rating INT NOT NULL COMMENT '评分(1-5)',
    comment TEXT DEFAULT NULL COMMENT '评价内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_course (user_id, course_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL,
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

CREATE TABLE checkins (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    student_id BIGINT UNSIGNED DEFAULT NULL COMMENT '学生ID',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, student_id, checkin_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_checkin_date (checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

CREATE TABLE daily_summaries (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT UNSIGNED NOT NULL COMMENT '发送老师ID',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    content TEXT NOT NULL COMMENT '总结内容',
    summary_date DATE NOT NULL COMMENT '总结日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_summary_date (summary_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日总结表';

CREATE TABLE push_records (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT UNSIGNED NOT NULL COMMENT '发送老师ID',
    course_id BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    course_title VARCHAR(200) NOT NULL COMMENT '课程名称',
    summary TEXT DEFAULT NULL COMMENT '总结内容',
    homework TEXT DEFAULT NULL COMMENT '作业内容',
    good_students VARCHAR(500) DEFAULT NULL COMMENT '表现优秀学生',
    target_user_ids TEXT COMMENT '推送对象用户ID列表(逗号分隔)',
    target_count INT DEFAULT 0 COMMENT '推送人数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '推送时间',
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_course_id (course_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推送记录表';

CREATE TABLE course_applications (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT UNSIGNED NOT NULL COMMENT '教师ID',
    title VARCHAR(200) NOT NULL COMMENT '课程名称',
    subject VARCHAR(50) DEFAULT NULL COMMENT '学科',
    grade VARCHAR(50) DEFAULT NULL COMMENT '年级',
    total_hours INT DEFAULT NULL COMMENT '总课时',
    description TEXT DEFAULT NULL COMMENT '课程简介',
    chapters_json TEXT DEFAULT NULL COMMENT '章节列表(JSON)',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '状态',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    reviewed_at DATETIME DEFAULT NULL COMMENT '审核时间',
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status),
    INDEX idx_applied_at (applied_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程申请表';

CREATE TABLE messages (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
    type ENUM('DAILY_SUMMARY', 'HOMEWORK', 'NOTIFICATION') NOT NULL COMMENT '消息类型',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT DEFAULT NULL COMMENT '消息内容',
    related_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID(课程ID/公告ID)',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    read_at DATETIME DEFAULT NULL COMMENT '阅读时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

CREATE TABLE notifications (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    target_role ENUM('PARENT', 'TEACHER', 'ALL') NOT NULL COMMENT '发布对象',
    is_published TINYINT(1) DEFAULT 0 COMMENT '是否发布',
    published_at DATETIME DEFAULT NULL COMMENT '发布时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_is_published (is_published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

INSERT INTO users (phone, password, real_name, role, status) VALUES 
('13800138000', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '系统管理员', 'ADMIN', 'ACTIVE'),
('13800138001', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '张老师', 'TEACHER', 'ACTIVE'),
('13800138002', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '李老师', 'TEACHER', 'ACTIVE'),
('13800138003', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '王家长', 'PARENT', 'ACTIVE'),
('13800138004', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '陈家长', 'PARENT', 'ACTIVE'),
('13800138005', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '王老师', 'TEACHER', 'ACTIVE'),
('13800138006', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '赵老师', 'TEACHER', 'ACTIVE'),
('13800138007', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '刘家长', 'PARENT', 'ACTIVE'),
('13800138008', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '周家长', 'PARENT', 'ACTIVE'),
('13800138009', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '吴家长', 'PARENT', 'ACTIVE'),
('13800138010', '$2a$10$ectipqYXYiVJ39/4sOrnxu60AYeHWTTqGv2zUTwPuzQPpnYmUJjUG', '郑家长', 'PARENT', 'ACTIVE');

INSERT INTO students (parent_id, name, age, grade, gender) VALUES
(4, '小明', 12, '初一', 'MALE'),
(4, '小红', 10, '五年级', 'FEMALE'),
(5, '小华', 14, '初二', 'MALE');

INSERT INTO courses (teacher_id, title, description, category, price, original_price, total_hours, cover_image, status, is_recommended, enrollment_count, favorite_count) VALUES
(2, '初一数学 · 暑期培优班', '针对初一学生的数学暑期培优课程，涵盖代数、几何等核心知识点', '数学', 1280.00, 1880.00, 16, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=math%20learning%20education%20classroom%20mathematics%20abstract&image_size=square', 'PUBLISHED', 1, 0, 0),
(2, '初二英语 · 语法突破班', '系统讲解初二英语语法，帮助学生突破语法难点', '英语', 960.00, 1460.00, 12, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=english%20learning%20education%20language%20study&image_size=square', 'PUBLISHED', 1, 0, 0),
(3, '初三物理 · 力学专题', '深入讲解初三物理力学知识，备战中考', '物理', 1120.00, 1620.00, 14, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=physics%20learning%20education%20science%20mechanics&image_size=square', 'PUBLISHED', 0, 0, 0),
(3, '高一化学 · 预科入门', '帮助初三毕业生提前学习高一化学基础知识', '化学', 800.00, 1200.00, 10, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chemistry%20learning%20education%20science%20laboratory&image_size=square', 'PUBLISHED', 1, 0, 0),
(2, '初一语文 · 阅读写作', '提升初一学生的阅读理解和写作能力', '语文', 880.00, 1280.00, 12, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20learning%20education%20literature%20reading&image_size=square', 'PENDING', 0, 0, 0);

INSERT INTO course_chapters (course_id, chapter_name, chapter_order, duration, content) VALUES
(1, '第一章 有理数', 1, '45', '讲解有理数的概念和运算'),
(1, '第二章 整式的加减', 2, '45', '整式的加减运算'),
(1, '第三章 一元一次方程', 3, '60', '一元一次方程的解法'),
(1, '第四章 几何图形初步', 4, '45', '几何图形的基本概念'),
(2, '第一章 一般现在时', 1, '40', '一般现在时的用法'),
(2, '第二章 一般过去时', 2, '40', '一般过去时的用法'),
(2, '第三章 一般将来时', 3, '40', '一般将来时的用法'),
(2, '第四章 现在进行时', 4, '40', '现在进行时的用法');