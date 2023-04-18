package cn.wzpmc.filemanager.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CountableList<T> {
    private int count;
    private List<T> data;
}
