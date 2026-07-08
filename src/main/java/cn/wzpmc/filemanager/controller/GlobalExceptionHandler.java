package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.exceptions.ResponseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常处理器（处理AuthorizationException）
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<Result<Void>> handleResponseException(ResponseException e) {
        // 取出对应的result并返回即可
        Result<Void> result = e.getResult();
        return ResponseEntity.ok(result);
    }
}