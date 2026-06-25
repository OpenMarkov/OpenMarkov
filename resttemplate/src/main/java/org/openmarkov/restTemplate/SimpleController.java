package org.openmarkov.restTemplate;

import org.springframework.web.bind.annotation.*;

@RestController
public class SimpleController {
    
    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello, World!";
    }
    
    @PostMapping("/doSomething")
    public ExampleData postExample(@RequestBody ExampleData input) {
        input.name += " (Edited)";
        input.status = "Success";
        if (input.department == null) {
            ExampleData.Department newDepartment = new ExampleData.Department();
            newDepartment.name = "IT";
            newDepartment.number = 15;
            input.department = newDepartment;
        }
        ;
        return input;
    }
    
    @PostMapping("/changeDepartmentNumber/{newDeptNo}")
    public ExampleData uriArgumentExample(@PathVariable int newDeptNo, @RequestBody ExampleData input) {
        if (input.department == null) {
            ExampleData.Department newDepartment = new ExampleData.Department();
            newDepartment.name = "IT";
            newDepartment.number = 0;
            input.department = newDepartment;
        }
        ;
        input.department.number = newDeptNo;
        return input;
    }
    
    
}