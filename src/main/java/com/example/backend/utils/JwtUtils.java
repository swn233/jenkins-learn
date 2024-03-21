package com.example.backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    //jwt秘钥
    @Value("${spring.security.jwt.key}")
    private String key;
    //jwt过期时间，七天
    @Value("${spring.security.jwt.expire}")
    private int expire;

    //使用redis数据库存储黑名单
    @Resource
    StringRedisTemplate template;

    //由于USERDetails中只有username和password，所以id和昵称等信息需要额外传入
    public String createJwt(UserDetails details,int id,String username){
        //创建HMAC256加密算法对象
        Algorithm algorithm =Algorithm.HMAC256(key);
        return JWT.create()
                //增加一个随机的id方便将jwt加入黑名单中
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id",id)
                .withClaim("name",username)
                .withClaim("authority",details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expireTime())
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    //计算过期时间
    public Date expireTime(){
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.HOUR,expire*24);
        return calendar.getTime();
    }

    //根据Jwt验证并解析用户信息
    //@headerToken:请求头中Authorization的值
    public DecodedJWT resolveJwt(String headerToken){
        String token=this.convertToken(headerToken);
        if (token==null)return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();

        try {
            DecodedJWT verify = jwtVerifier.verify(token);  //对JWT令牌进行验证，看看是否被修改
            if (this.isInvalidToken(verify.getId()))return null;//判断是否在黑名单中
            Date expiresAt=verify.getExpiresAt();//判断是否过期
            return new Date().after(expiresAt)?null:verify;
        }catch (JWTVerificationException e){
            e.printStackTrace();
            return null;
        }


    }

    //从请求头中的Authorization字段提取出jwt
    private String convertToken(String headerToken){
        if (headerToken==null||!headerToken.startsWith("Bearer ")){
            return null;
        }
        return headerToken.substring(7);
    }

    //将解码后的jwt转换成userdetails对象，从而用于构建验证
    public UserDetails toUser(DecodedJWT jwt) {
        Map<String,Claim>  claims=jwt.getClaims();
        return User.withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authority").asArray(String.class))
                .build();
    }

    public Object toId(DecodedJWT jwt) {
        Map<String, Claim> claims=jwt.getClaims();
        return claims.get("id").asInt();
    }

    //利用redis建立黑名单机制,用户退出后无法再使用这个token，在token过期后删除
    public String invalidateJwt(String headerToken){
        String token=this.convertToken(headerToken);
        if(token==null){
            return "token为空";
        }
        Algorithm algorithm=Algorithm.HMAC256(key);
        //通过指定的算法构造一个jwt解析器
        JWTVerifier jwtVerifier=JWT.require(algorithm).build();
        try{
            DecodedJWT jwt=jwtVerifier.verify(token);
            //这里获取的是jwtid，使用uuid进行唯一标识，而不是负载中的id字段
            String id =jwt.getId();
            //将对应id的jwt拉黑
            return deleteToken(id,jwt.getExpiresAt());

        }catch (JWTVerificationException e){
            return "JWT错误";
        }
    }

    //根据jwt过期时间判断是否删除，加入黑名单
    //@time：过期时间
    public String deleteToken(String uuid,Date time){
        if (isInvalidToken(uuid)){
            return "已退出，请勿重复退出";
        }
        Date now=new Date();
        //gettime返回1970到现在的毫秒数，将剩余的过期时间加入到redis中
        long expire=Math.max(time.getTime()-now.getTime(),0);
        template.opsForValue().set(Const.JWT_BLACK_LIST+uuid,"",expire, TimeUnit.MILLISECONDS);
        long days = TimeUnit.MILLISECONDS.toDays(expire);
        long hours = TimeUnit.MILLISECONDS.toHours(expire) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(expire) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(expire) % 60;

        System.out.println(Const.JWT_BLACK_LIST + uuid + " 过期时间剩余：" + days + "天 " + hours + "小时 " + minutes + "分钟 " + seconds + "秒");
        return "退出成功";
    }

    //判断jwt是否在黑名单中
    private boolean isInvalidToken(String uuid){
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST+uuid));
    }


}
