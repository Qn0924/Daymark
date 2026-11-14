package com.nan.daymarkbackend.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.nan.daymarkbackend.common.BaseResponse;
import com.nan.daymarkbackend.common.ResultUtils;
import com.nan.daymarkbackend.exception.BusinessException;
import com.nan.daymarkbackend.exception.ErrorCode;
import com.nan.daymarkbackend.exception.ThrowUtils;
import com.nan.daymarkbackend.model.entity.User;
import com.nan.daymarkbackend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private UserService userService;

    @Value("${daymark.upload.dir}")
    private String uploadDir;

    @Value("${daymark.upload.url-prefix}")
    private String urlPrefix;

    /**
     * 本地上传文件
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        //校验参数
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //获取文件名称
        String originalName = file.getOriginalFilename();

        //拿到文件后缀
        String suffix;
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        } else {
            suffix = ".png";
        }

        //拼接文件名称
        String fileName = IdUtil.simpleUUID() + suffix;
        //按用户分目录
        String relativePath = loginUser.getId() + "/" + fileName;
        File dest = new File(uploadDir, relativePath);

        //写入磁盘
        try {
            FileUtil.writeBytes(file.getBytes(), dest);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件保存失败");
        }

        //返回前端
        return ResultUtils.success(buildUrl(relativePath, httpRequest));
    }

    /**
     * 通过 URL 下载图片存储
     */
    @PostMapping("/upload/url")
    public BaseResponse<String> uploadByUrl(@RequestParam("url") String url, HttpServletRequest httpRequest) {
        //校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "url 不能为空");
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 从 URL 提取后缀
        String suffix = ".png";
        //截掉？后的内容
        int queryIdx = url.indexOf('?');
        String pathPart;
        if (queryIdx > 0) {
            pathPart = url.substring(0, queryIdx);
        } else {
            pathPart = url;
        }
        //找最后一个点的位置
        int dotIdx = pathPart.lastIndexOf('.');
        //找到点取后缀
        if (dotIdx > 0) {
            //转换成小写
            String ext = pathPart.substring(dotIdx).toLowerCase();
            if (ext.length() <= 5) {
                //过滤奇形怪状的后缀
                suffix = ext;
            }
        }

        //重命名文件名字
        String fileName = IdUtil.simpleUUID() + suffix;
        //按用户分类
        String relativePath = loginUser.getId() + "/" + fileName;
        File dest = new File(uploadDir, relativePath);

        // 下载到本地
        try {
            HttpUtil.downloadFileFromUrl(url, dest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "下载图片失败");
        }
        ThrowUtils.throwIf(!dest.exists() || dest.length() == 0, ErrorCode.OPERATION_ERROR, "下载图片失败");

        return ResultUtils.success(buildUrl(relativePath, httpRequest));
    }

    /**
     * 拼出可访问的完整 URL
     */
    private String buildUrl(String relativePath, HttpServletRequest httpRequest) {
        String requestUrl = httpRequest.getRequestURL().toString();
        String baseUrl = requestUrl.substring(0, requestUrl.indexOf("/api/")) + "/api";
        return baseUrl + urlPrefix + "/" + relativePath;
    }
}