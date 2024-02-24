package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * qps测试
 */
@RestController
public class QpsTestController {

    // #boot 默认为tomcat提供200个线程   假设一个请求50ms处理时间  理想情况下 1s可以处理20次  200个线程  QPS 20*200 =  < 4000qps
    /**
     *
     * @return
     */
    @GetMapping("test")
    public String qpsTest() {
        System.out.println("test:"+Thread.currentThread().getName());
        return "ok";
    }


    @GetMapping("test2")
    public String qpsTest2() {
        // dosth. 处理处理 操作数据库 操作redis ....  50ms
        try {
            System.out.println("test2:"+Thread.currentThread().getName());
            Thread.sleep(50L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "ok";
    }

}
