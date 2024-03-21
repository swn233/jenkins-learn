package com.example.backend.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;



@Component
@RabbitListener(queues = "test_mail")
public class MailQueueListener {
    @Resource
    JavaMailSender sender;
    @Value("${spring.mail.username}")
    String username;

    @RabbitHandler
    public void sendMailMessage(Map<String ,Object> data){
        String email=(String) data.get("email");
        Integer code=(Integer) data.get("code");
        String type=(String) data.get("type");
        System.out.println("这里是email listener"+email+" "+code+"type");
        SimpleMailMessage message=switch (type){
            case "register"->createMessage("欢迎注册","验证码为:"+code+"有效时间为3分钟",email);
            case "reset"->createMessage("你的密码重置邮件","验证码为:"+code+"有效时间为3分钟",email);
            default -> null;
        };
        if(message==null)return;
        sender.send(message);

    }

    private SimpleMailMessage createMessage(String title,String content,String email) {
        SimpleMailMessage message=new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }
}
