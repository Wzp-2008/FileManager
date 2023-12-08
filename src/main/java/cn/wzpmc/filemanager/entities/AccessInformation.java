package cn.wzpmc.filemanager.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccessInformation {
    private long totalDownload;
    private long totalAccess;
    private Date date;
}
