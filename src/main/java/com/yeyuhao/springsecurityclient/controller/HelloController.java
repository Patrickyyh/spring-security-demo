package com.yeyuhao.springsecurityclient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String helloWorld(){
        return "Hello, welcome the spring-security demo";
    }
}
