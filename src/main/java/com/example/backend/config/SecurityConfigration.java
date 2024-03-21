package com.example.backend.config;

import com.example.backend.entity.RestBean;
import com.example.backend.entity.dto.Account;
import com.example.backend.entity.vo.response.AuthorizeVO;
import com.example.backend.filter.JwtAuthorrizeFilter;
import com.example.backend.service.AccountService;
import com.example.backend.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfigration {
    //jwt的创建校验等相关方法
    @Resource
    JwtUtils utils;

    //用于提供用户账户信息的比对查询等相关服务
    @Resource
    AccountService service;

    //jwt过滤器实现
    @Resource
    JwtAuthorrizeFilter jwtAuthorrizeFilter;

//    配置应用程序的 HTTP 安全配置，它允许您定义哪些 URL 路径需要认证、需要特定的权限、需要表单登录等。
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf->conf
                        .requestMatchers(("/api/auth/**"),("/error")).permitAll()
                        .anyRequest().authenticated())
                .formLogin(conf->conf
                        .loginProcessingUrl("/api/auth/login")
                        .failureHandler(this::onAuthenticationFailure)
                        .successHandler(this::onAuthenticationSuccess))
                .logout(conf->conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess))
                .exceptionHandling(conf->conf.authenticationEntryPoint(this::onUnauthorized)
                        .accessDeniedHandler(this::onAccessDeniedhandler))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf->conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthorrizeFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void onAccessDeniedhandler(HttpServletRequest request, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(RestBean.failure(403,"拒绝访问").asJsonString());
        System.out.println(RestBean.failure(403,"拒绝访问").asJsonString());

    }

    private void onUnauthorized(HttpServletRequest request, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(RestBean.failure(401,"未通过认证").asJsonString());
        System.out.println(RestBean.failure(401,"未通过认证").asJsonString());
    }

    private void onLogoutSuccess(HttpServletRequest request, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");

        PrintWriter writer = httpServletResponse.getWriter();
        String authorization=request.getHeader("Authorization");

        writer.write(RestBean.success(utils.invalidateJwt(authorization)).asJsonString());


    }

    private void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");

        //使用对象转换小工具进行
        //使用数据库中的用户信息进行校验
        User user=(User)authentication.getPrincipal();
        Account account = service.findAccountByNameOrEmail(user.getUsername());
        String token=utils.createJwt(user,account.getId(),account.getUsername());
        AuthorizeVO vo=account.asViewObject(AuthorizeVO.class,viewobject->{
            viewobject.setExpire(utils.expireTime());
            viewobject.setToken(token);
        });
        httpServletResponse.getWriter().write(RestBean.success(vo).asJsonString());
        System.out.println(RestBean.success("欢迎用户"+account.getUsername()+"登陆").asJsonString());
        System.out.println(RestBean.success(vo).asJsonString());

        //authentication.getPrincipal()获取userdetails
//        User user=(User)authentication.getPrincipal();
//        String token=utils.createJwt(user,1,"小明");
//        AuthorizeVO vo=new AuthorizeVO();
//        vo.setExpire(utils.expireTime());
//        vo.setRole("admin");
//        vo.setToken(token);
//        vo.setUsername(user.getUsername());
//        httpServletResponse.getWriter().write(RestBean.success(vo).asJsonString());
//        System.out.println(RestBean.success(vo).asJsonString());


       //最原始的success什么都不做 httpServletResponse.getWriter().write(RestBean.success().asJsonString());
       // System.out.println(RestBean.success().asJsonString());

    }

    private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(RestBean.failure(403,"登陆失败").asJsonString());
        System.out.println(RestBean.failure(403,"登陆失败").asJsonString());
    }
}
