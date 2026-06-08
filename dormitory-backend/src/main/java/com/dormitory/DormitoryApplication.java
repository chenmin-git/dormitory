package com.dormitory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智能宿舍管理系统启动类
 *
 * @author 毕业设计
 */
@SpringBootApplication
@MapperScan("com.dormitory.mapper")
public class DormitoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DormitoryApplication.class, args);
        System.out.println("\n==============================================");
        System.out.println("  智能宿舍管理系统启动成功！");
        System.out.println("  接口文档: http://localhost:8088/api/doc.html");
        System.out.println("==============================================\n");
    }
}
