package com.angli.online.controller;

import com.angli.online.dto.response.ApiResponse;
import com.angli.online.entity.Student;
import com.angli.online.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parent/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ApiResponse<Student> addStudent(@RequestParam Long parentId, 
                                          @RequestParam String name,
                                          @RequestParam(required = false) Integer age,
                                          @RequestParam(required = false) String grade,
                                          @RequestParam(required = false) String gender) {
        Student student = studentService.addStudent(parentId, name, age, grade, gender);
        return ApiResponse.success(student);
    }

    @GetMapping("/parent/{parentId}")
    public ApiResponse<List<Student>> getStudentsByParentId(@PathVariable Long parentId) {
        List<Student> students = studentService.getStudentsByParentId(parentId);
        return ApiResponse.success(students);
    }

    @GetMapping("/{id}")
    public ApiResponse<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return ApiResponse.success(student);
    }

    @PutMapping("/{id}")
    public ApiResponse<Student> updateStudent(@PathVariable Long id,
                                              @RequestParam(required = false) String name,
                                              @RequestParam(required = false) Integer age,
                                              @RequestParam(required = false) String grade,
                                              @RequestParam(required = false) String gender) {
        Student student = studentService.updateStudent(id, name, age, grade, gender);
        return ApiResponse.success(student);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ApiResponse.success("删除成功");
    }

}
