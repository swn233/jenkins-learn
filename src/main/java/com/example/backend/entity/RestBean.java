package com.example.backend.entity;
import com.alibaba.fastjson2.*;

//用于向前端返回运行信息和数据，将返回数据转换成json
public record RestBean<T>(int code,T data,String message) {
    //有可能需要返回数据
    public static <T> RestBean<T> success(T data){
        return new RestBean<>(200,data,"success");
    }

    public static <T> RestBean<T> success(){
        return success(null);
    }

    public static <T> RestBean<T> failure(int code,String message){
        return new RestBean<>(code,null,message);
    }

    //用fastjson将返回给前端的数据转换成json格式
    public String asJsonString(){
        return JSONObject.toJSONString(this,JSONWriter.Feature.WriteNulls);
    }

}
