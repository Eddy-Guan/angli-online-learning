package com.angli.online.service;

import com.angli.online.entity.Student;

import java.util.List;

public interface StudentService {

    Student addStudent(Long parentId, String name, Integer age, String grade, String gender);

    List<Student> getStudentsByParentId(Long parentId);

    Student getStudentById(Long id);

    Student updateStudent(Long id, String name, Integer age, String grade, String gender);

    void deleteStudent(Long id);

}
