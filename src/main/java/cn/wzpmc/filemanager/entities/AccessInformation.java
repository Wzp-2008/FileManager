package cn.wzpmc.filemanager.entities;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JSONCompiled
public class AccessInformation {
    private long totalDownload;
    private long totalAccess;
    private Date date;
}