package com.example.upload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequestMapping(value = "/api/v1/upload")
@Slf4j
public class UserController {
    @GetMapping(value="test")
    public String test() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        log.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        log.debug("request id:{}",addr.getHostAddress());
        return addr.getHostAddress();
    }
}
