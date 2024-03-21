package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.dto.Account;
import com.example.backend.entity.vo.request.EmailRegisterVO;
import com.example.backend.entity.vo.request.EmailResetVO;
import com.example.backend.entity.vo.response.ConfirmResetVO;
import com.example.backend.mapper.AccountMapper;
import com.example.backend.service.AccountService;
import com.example.backend.utils.Const;
import com.example.backend.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper,Account> implements AccountService {

    @Resource
    BCryptPasswordEncoder encoder;

    @Resource
    FlowUtils flowUtils;
    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate template;

    @Override
    public Account findAccountByNameOrEmail(String text) {
        return this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();
    }

    //生成一个六位数的验证码用于注册
    @Override
    public String RegisterEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (this.verifyLimit(ip)) {
                Random random = new Random();
                int code = random.nextInt(899999) + 100000;
                Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
                amqpTemplate.convertAndSend("test_mail", data);
                System.out.println("发送邮箱为：" + data);
                template.opsForValue()
                        .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
                return null;
            } else {
                return "您的请求过于频繁，请稍后再试";
            }
        }
    }
    //根据ip地址进行过滤
    @Override
    public boolean verifyLimit(String ip){
        String key= Const.VERIFY_EMAIL_LIMIT+ip;
        return flowUtils.limitOnceCheck(key,60);
    }

    //UserDetailService中的重要方法，实现后SpringSecurity不会生成随机密码
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            Account account=this.findAccountByNameOrEmail(username);
            if (account==null){
                throw  new UsernameNotFoundException("用户名或密码错误");
            }
            return User.withUsername(username)
                    .password(account.getPassword())
                    .roles(account.getRole())
                    .build();
    }

    //return：提示操作后结果的字符串
    @Override
    public String registerEmailAccount(EmailRegisterVO vo){
        String email=vo.getEmail();
        String username=vo.getUsername();
        String code=template.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        if (code==null){
            return "请先获取验证码";
        }
        if (!code.equals(vo.getCode())){
            return "验证码错误，请重试";
        }
        if (this.existedAccountByEmail(email)){
            return "此邮箱已被其他用户注册，请更换一个新的邮箱";
        }
        if (this.existedAccountByUsername(username)){
            return "此用户名已被其他用户注册，请更换一个新的用户名";
        }
        String password=encoder.encode(vo.getPassword());
        Account account=new Account(null,username,password,email,"user",new Date());
        if (this.save(account)){
            template.delete(Const.VERIFY_EMAIL_DATA+email);
            return null;
        }
        else {
            return "内部错误，请联系管理员";
        }
    }

    private boolean existedAccountByUsername(String username) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username",username));
    }

    private boolean existedAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email));
    }


    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String code = template.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo){
        String email=vo.getEmail();
        String verify=this.resetConfirm(new ConfirmResetVO(vo.getEmail(),vo.getCode()));
        if (verify!=null){
            return verify;
        }
        String password=encoder.encode(vo.getPassword());
        boolean update=this.update().eq("email",email).set("password",password).update();
        if (update){
            template.delete(Const.VERIFY_EMAIL_DATA+email);
        }
        return null;
    }


}
