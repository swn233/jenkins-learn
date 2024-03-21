package com.example.backend.entity.vo.response;

import lombok.Data;

import java.util.Date;

//用户响应信息
@Data
public class AuthorizeVO {
    String username;
    String role;
    String token;
    Date expire;
}
