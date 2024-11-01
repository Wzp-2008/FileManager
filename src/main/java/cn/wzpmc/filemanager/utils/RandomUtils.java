package cn.wzpmc.filemanager.utils;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@NoArgsConstructor
public class RandomUtils {
    public String generatorRandomString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int c = new Random().nextInt(33, 126);
            builder.append((char) c);
        }
        return builder.toString();
    }
}