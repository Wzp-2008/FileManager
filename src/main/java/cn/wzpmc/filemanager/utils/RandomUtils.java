package cn.wzpmc.filemanager.utils;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 随机相关的工具类
 */
@Component
@NoArgsConstructor
public class RandomUtils {
    /**
     * 生成随机字符串（包含符号，从ASCII 33 ~ 126都可能生成）
     *
     * @param length 随机字符串长度
     * @return 对应的随机字符串
     */
    public String generatorRandomString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int c = new Random().nextInt(33, 126);
            builder.append((char) c);
        }
        return builder.toString();
    }

    /**
     * 生成随机文件名（只有大小写字母以及部分符号（文件名安全））
     *
     * @param length 对应长度
     * @return 随机字符串
     */
    public String generatorRandomFileName(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            double random = Math.random();
            int c = new Random().nextInt(97, 122);
            if (random < 0.3) {
                c = new Random().nextInt(48, 57);
            } else if (random < 0.6) {
                c = new Random().nextInt(65, 90);
            }
            builder.append((char) c);
        }
        return builder.toString();
    }
}