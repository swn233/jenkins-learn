package com.example.backend;

import com.example.backend.entity.dto.Image;
import com.example.backend.service.ImageService;
import com.example.backend.service.impl.ImageServiceImpl;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackendApplicationTests {
    //JavaMailSender是专门用于发送邮件的对象，自动配置类已经提供了Bean
//    @Autowired
//    JavaMailSender sender;
//
//    @Test
//    void testEmail() {
//        //SimpleMailMessage是一个比较简易的邮件封装，支持设置一些比较简单内容
//        SimpleMailMessage message = new SimpleMailMessage();
//        //设置邮件标题
//        message.setSubject("【南京信息工程大学教务处】关于近期学校对您的处分决定");
//        //设置邮件内容
//        message.setText("同学您好，经监控和教务巡查发现，您近期存在旷课、迟到、早退、上课刷抖音行为，" +
//                "现已通知相关辅导员，请手写5000字书面检讨，并于2023年10月7号前交到辅导员办公室。");
//        //设置邮件发送给谁，可以多个，这里就发给你的QQ邮箱
//        message.setTo("1476707975@qq.com");
//        //邮件发送者，这里要与配置文件中的保持一致
//        message.setFrom("18061946436@163.com");
//        //OK，万事俱备只欠发送
//        sender.send(message);
//    }
//    @Test
//    void testEncoder() {
//        System.out.println(new BCryptPasswordEncoder().encode("visitor"));
//    }
//    @LocalServerPort
//    private int port;
//
//    private final TestRestTemplate restTemplate = new TestRestTemplate();
//
//    @Test
//    void testUpload() throws IOException {
//        // 构建图床接口的 URL
//        String imageUrl = "https://sm.ms/api/v2/upload";
//
//        // 替换为实际的图片文件的绝对路径
//        String absolutePath = "C:\\Users\\14767\\Pictures\\iu\\img-163774686227877dbeb59c88e478aa4a033fee9b2649e.jpg";
//        File imageFile = new File(absolutePath);
//
//// 创建一个测试用的 MultipartFile
//        MockMultipartFile file = new MockMultipartFile(
//                "smfile",
//                imageFile.getName(),
//                MediaType.IMAGE_JPEG_VALUE,
//                Files.readAllBytes(imageFile.toPath())
//        );
//
//        // 构建请求体
//        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
//        requestBody.add("smfile", new ByteArrayResource(file.getBytes()) {
//            @Override
//            public String getFilename() {
//                return file.getOriginalFilename();
//            }
//
//            @Override
//            public long contentLength() {
//                return file.getSize();
//            }
//        });
//        requestBody.add("format", "json");
//
//        // 发送请求并获取响应
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.set("Authorization", "wn5amHHGKCgZwGgmq47JYElh5Wb4jCnR"); // 替换YourAuthToken为实际的认证token
//        headers.set("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0");
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//        ResponseEntity<String> responseEntity = restTemplate.exchange(imageUrl, HttpMethod.POST, requestEntity, String.class);
//
//        // 打印请求头
//        System.out.println("Request Headers:");
//        System.out.println("---------------");
//        headers.forEach((key, value) -> System.out.println(key + ": " + value));
//        System.out.println("Request Body:");
//        System.out.println(requestBody);
//        System.out.println("---------------");
//
//        // 输出响应信息
//        System.out.println("Response Code: " + responseEntity.getStatusCode());
//        System.out.println("Response Body: " + responseEntity.getBody());
//    }
//
//
//    @Test
//    void testDelete() {
//        // 构建图床删除接口的 URL，假设图床提供的删除接口为 DELETE /api/v2/delete/{imageId}
//        String deleteUrl = "https://sm.ms/api/v2/delete/{imageId}";
//
//        // 替换为实际的图片ID
//        String imageId = "your_image_id"; // 替换为实际的图片ID
//
//        // 构建请求体
//        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
//        requestBody.add("imageId", imageId);
//
//        // 设置请求头
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "wn5amHHGKCgZwGgmq47JYElh5Wb4jCnR"); // 替换YourAuthToken为实际的认证token
//        headers.set("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0");
//
//        // 发送 DELETE 请求并获取响应
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//        ResponseEntity<String> responseEntity = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
//
//        // 打印请求头
//        System.out.println("Request Headers:");
//        System.out.println("---------------");
//        headers.forEach((key, value) -> System.out.println(key + ": " + value));
//        System.out.println("Request Body:");
//        System.out.println(requestBody);
//        System.out.println("---------------");
//
//        // 输出响应信息
//        System.out.println("Response Code: " + responseEntity.getStatusCode());
//        System.out.println("Response Body: " + responseEntity.getBody());
//    }


}






