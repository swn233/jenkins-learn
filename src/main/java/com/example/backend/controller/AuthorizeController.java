package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.entity.vo.request.EmailRegisterVO;
import com.example.backend.entity.vo.request.EmailResetVO;
import com.example.backend.entity.vo.response.ConfirmResetVO;
import com.example.backend.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService accountService;
    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam  @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request){
        String message=accountService.RegisterEmailVerifyCode(type,email,request.getRemoteAddr());
        return message==null?RestBean.success():RestBean.failure(400,message);
    }

    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo){
        return this.messageHandle(()->accountService.registerEmailAccount(vo));
    }


    private  RestBean<Void> messageHandle(Supplier<String> action){
        String message=action.get();
        return message==null ?  RestBean.success() : RestBean.failure(400, message) ;
    }

    @PostMapping("/reset-confirm")
    public  RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo) {
        return this.messageHandle(()->accountService.resetConfirm(vo));
    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVO vo){
        return this.messageHandle(()->accountService.resetEmailAccountPassword(vo));
    }

}
