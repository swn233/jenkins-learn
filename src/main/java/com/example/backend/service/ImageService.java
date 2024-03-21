package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.dto.Image;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService extends IService<Image> {

    List<Image> findAll();

    boolean addOne(Image image);

    //@file:需要上传的文件
    String upload(MultipartFile file) throws IOException;

    //@url:图片url
    String deleteByUrl(String url);
}
