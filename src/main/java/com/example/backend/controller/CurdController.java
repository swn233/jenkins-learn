package com.example.backend.controller;

import com.example.backend.entity.RestBean;
import com.example.backend.service.ImageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/data")
public class CurdController {
    @Resource
    ImageService imageService;

    @GetMapping("/image")
    public String imageGet(){
        return RestBean.success(imageService.findAll()).asJsonString();
    }

    @PostMapping("/upload")
    public String GetImageUrl(@RequestPart("file") MultipartFile file,
                              HttpServletRequest request) throws IOException {
      return imageService.upload(file);
    }

    @PostMapping("/delete")
    public String DeleteImageByUrl(@RequestParam String url,
                              HttpServletRequest request) throws IOException {
        return imageService.deleteByUrl(url);
    }

}
