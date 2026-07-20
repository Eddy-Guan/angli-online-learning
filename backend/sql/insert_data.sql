SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM course_chapters;
DELETE FROM enrollments;
DELETE FROM students;
DELETE FROM courses;
DELETE FROM course_applications;
DELETE FROM users;

SET FOREIGN_KEY_CHECKS = 1;

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