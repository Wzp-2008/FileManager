package cn.wzpmc.filemanager.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Page<T> {
    private int total;
    private List<T> data;
}
