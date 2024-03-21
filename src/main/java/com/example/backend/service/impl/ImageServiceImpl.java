package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.dto.Image;
import com.example.backend.mapper.ImageMapper;
import com.example.backend.service.ImageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import com.alibaba.fastjson2.*;
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {
    @Override
    public List<Image> findAll(){
        return this.list();
    }
    @Override
    public boolean addOne(Image image){
        return this.save(image);
    }
    @Override
    public String upload(MultipartFile file) throws IOException {
        // 构建图床接口的 URL
        String imageUrl = "https://sm.ms/api/v2/upload";
        String name = file.getOriginalFilename();
        long size = file.getSize();
        String type = file.getContentType();
        String result =  "，文件名称：" + name + "文件大小：" + size + "，文件类型：" + type ;
        System.out.println(result);

        //判断文件大小和文件类型是否满足要求
        if(size>=5*1024*1024){
            return "图片体积过大";
        }
        if (!type.equals("image/jpeg") && !type.equals("image/png") && !type.equals("image/gif") && !type.equals("image/bmp") && !type.equals("image/webp") && !type.equals("image/svg+xml") && !type.equals("image/tiff") && !type.equals("image/x-icon")) {
            return "不支持的图片类型";
        }
        //使用ByteArrayResource对象创建资源对象
        ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
            @Override
            public long contentLength() {
                return file.getSize();
            }
        };

        // 构建请求体
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("smfile", fileAsResource);
        requestBody.add("format", "json");

        // 添加认证信息到请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "wn5amHHGKCgZwGgmq47JYElh5Wb4jCnR"); // 替换YourAuthToken为实际的认证token
        headers.set("Content-Type", "multipart/form-data");
        headers.set("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0");

        // 发送带有认证信息的POST请求到图床接口
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, Object>> requestEntity =  new HttpEntity<MultiValueMap<String, Object>>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(imageUrl, requestEntity, String.class);
        System.out.println(RestBean.success(response.getBody()).asJsonString());

        //使用fastjson解析返回的json，判断是否上传成功
        JSONObject data=JSON.parseObject(response.getBody());
        Boolean success=data.getBoolean("success");
        String code=data.getString("code");

        if (Boolean.TRUE.equals(success)) {
            JSONObject success_data=data.getJSONObject("data");
            String hash=success_data.getString("hash");
            String image = success_data.getString("url");
            Image img = new Image(image,hash);
            if (!existedImageByUrl(image)) {
                addOne(img);
                return image;
            } else {
                //数据库中已有图片
                return "存在重复图片";
            }
        }
        else {
            if (code.equals("image_repeated")) {
                String image = data.getString("images");
                Image img = new Image(image);
                if (!existedImageByUrl(image)) {
                    addOne(img);
                    return image;
                } else {
                    //图床中已存在重复图片,数据库中也存在
                    return "存在重复图片";
                }
            } else {
                return "上传图片失败，图床错误";
            }
        }

    }

    //根据url删除图片
    @Override
    public String deleteByUrl(String url) {
     if(this.remove(Wrappers.<Image>query().eq("url",url))) {
         System.out.println(RestBean.success(200).asJsonString());
         return RestBean.success(200).asJsonString();
     }
    else {
         System.out.println(RestBean.failure(501, "图片删除失败").asJsonString());
         return RestBean.failure(501, "图片删除失败").asJsonString();
     }
    }

    //判断是否已经存在图片
    private boolean existedImageByUrl(String url) {
        return this.baseMapper.exists(Wrappers.<Image>query().eq("url", url));
    }

}