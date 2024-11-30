package cn.wzpmc.filemanager.entities;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Data
@JSONCompiled
public class Result<T> {
    private int status;
    private String msg;
    private T data;
    private long timestamp;
    protected Result() {
        this.timestamp = System.currentTimeMillis();
    }
    protected Result(int status, String msg, T data) {
        this();
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
    protected Result(HttpStatus status) {
        this();
        this.status = status.value();
        this.msg = status.getReasonPhrase();
    }
    protected Result(HttpStatus status, String msg) {
        this(status);
        this.msg = msg;
    }
    protected Result(HttpStatus status, T data) {
        this(status);
        this.data = data;
    }
    protected Result(HttpStatus status, String msg, T data) {
        this(status, msg);
        this.data = data;
    }
    public static <T> Result<T> success() {
        return new Result<>(HttpStatus.OK);
    }
    public static <T> Result<T> success(String msg) {
        return new Result<>(HttpStatus.OK, msg);
    }
    public static <T> Result<T> success(T data) {
        return new Result<>(HttpStatus.OK, data);
    }
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(HttpStatus.OK, msg, data);
    }
    public static <T> Result<T> failed() {
        return new Result<>(HttpStatus.FORBIDDEN);
    }
    public static <T> Result<T> failed(String msg) {
        return new Result<>(HttpStatus.FORBIDDEN, msg);
    }
    public static <T> Result<T> failed(HttpStatus status) {
        return new Result<>(status);
    }
    public static <T> Result<T> failed(HttpStatus status, String msg) {
        return new Result<>(status, msg);
    }
    public static <T> Result<T> create() {
        return new Result<>();
    }
    public Result<T> status(int status) {
        this.status = status;
        return this;
    }
    public Result<T> status(HttpStatus status) {
        this.status = status.value();
        this.msg = status.getReasonPhrase();
        return this;
    }
    public Result<T> msg(String msg) {
        this.msg = msg;
        return this;
    }
    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    public void writeToResponse(HttpServletResponse response){
        response.addHeader("Content-Type", "application/json; charset=utf-8");
        try(ServletOutputStream outputStream = response.getOutputStream()){
            writeToOutputStream(outputStream);
        } catch (IOException e) {
            log.trace("写出到流失败，", e);
        }
    }
    public void writeToOutputStream(OutputStream stream) throws IOException {
        stream.write(JSON.toJSONString(this).getBytes(StandardCharsets.UTF_8));
    }
}