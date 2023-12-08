package cn.wzpmc.filemanager.entities;

import cn.wzpmc.filemanager.enums.HttpCodes;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Data
public class Result<T> {
    private int status;
    private String message;
    private T data;
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    public static<T> Result<T> success(){
        return success(null);
    }
    public static<T> Result<T> success(T data){
        Result<T> rData = new Result<>();
        rData.setStatus(HttpCodes.HTTP_CODES200.getCode());
        rData.setMessage(HttpCodes.HTTP_CODES200.getMessage());
        rData.setData(data);
        return rData;
    }
    public static<T> Result<T> failed(HttpCodes httpCodes){
        return failed(httpCodes, null);
    }
    public static<T> Result<T> failed(HttpCodes httpCodes, T data){
        Result<T> rData = new Result<>();
        rData.setStatus(httpCodes.getCode());
        rData.setMessage(httpCodes.getMessage());
        rData.setData(data);
        return rData;
    }
    public static<T> Result<T> copyFailed(Result<?> original){
        Result<T> rData = new Result<>();
        rData.setStatus(original.getStatus());
        rData.setMessage(original.getMessage());
        rData.setTimestamp(original.getTimestamp());
        return rData;
    }


    public static<T> Result<Page<T>> page(int total, List<T> data){
        return Result.success(new Page<>(total, data));
    }

    public void writeToResponse(HttpServletResponse response){
        try(ServletOutputStream outputStream = response.getOutputStream()){
            writeToOutputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeToOutputStream(OutputStream stream) throws IOException {
        stream.write(JSON.toJSONString(this).getBytes(StandardCharsets.UTF_8));
    }
}