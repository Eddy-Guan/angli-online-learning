package com.angli.online.service.impl;

import com.angli.online.entity.Student;
import com.angli.online.entity.User;
import com.angli.online.mapper.StudentMapper;
import com.angli.online.mapper.UserMapper;
import com.angli.online.service.StudentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final UserMapper userMapper;

    public StudentServiceImpl(StudentMapper studentMapper, UserMapper userMapper) {
        this.studentMapper = studentMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Student addStudent(Long parentId, String name, Integer age, String grade, String gender) {
        User parent = userMapper.selectById(parentId);
        if (parent == null) {
            throw new RuntimeException("家长不存在");
        }

        Student student = new Student();
        student.setParentId(parentId);
        student.setName(name);
        student.setAge(age);
        student.setGrade(grade);
        student.setGender(gender);

        studentMapper.insert(student);
        return student;
    }

    @Override
    public List<Student> getStudentsByParentId(Long parentId) {
        return studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getParentId, parentId));
    }

    @Override
    public Student getStudentById(Long id) {
        return studentMapper.selectById(id);
    }

    @Override
    public Student updateStudent(Long id, String name, Integer age, String grade, String gender) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }

        if (name != null) {
            student.setName(name);
        }
        if (age != null) {
            student.setAge(age);
        }
        if (grade != null) {
            student.setGrade(grade);
        }
        if (gender != null) {
            student.setGender(gender);
        }

        studentMapper.updateById(student);
        return student;
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        studentMapper.deleteById(id);
    }

}
