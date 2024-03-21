package com.example.backend.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.backend.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@TableName("image")
@AllArgsConstructor
public class Image implements BaseData {
    @TableId(type = IdType.AUTO)
    String url;
    String theme;
    String place;
    Date date;
    String hash;
    public Image(String url,String hash){
        this.url=url;
        this.hash=hash;
        this.theme=null;
        this.place=null;
        this.date=null;
    }
    public Image(String url){
        this.url=url;
        this.hash=null;
        this.theme=null;
        this.place=null;
        this.date=null;
    }

}
