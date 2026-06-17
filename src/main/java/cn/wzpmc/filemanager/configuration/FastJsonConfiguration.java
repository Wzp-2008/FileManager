package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 系统JSON解析配置
 */
@Configuration
@RequiredArgsConstructor
public class FastJsonConfiguration implements WebMvcConfigurer {

    private final FileManagerProperties properties;

    /**
     * 设置系统使用FastJSON进行json序列化与反序列化
     *
     * @param builder the builder to configure
     */
    @Override
    public void configureMessageConverters(HttpMessageConverters.@NonNull ServerBuilder builder) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = createFastjsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        config.setCharset(StandardCharsets.UTF_8);
        converter.setFastJsonConfig(config);
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
        builder.withJsonConverter(converter);
    }

    /**
     * 整合FastJSON配置
     *
     * @return FastJson序列/反序列化配置
     */
    private @NonNull FastJsonConfig createFastjsonConfig() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        // 设置日期格式
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        // 指定编码格式
        fastJsonConfig.setCharset(StandardCharsets.UTF_8);
        // 设置忽略掉没有属性的getter方法，设置格式化后输出
        JSONWriter.Feature[] writerFeatures = {JSONWriter.Feature.IgnoreNonFieldGetter, JSONWriter.Feature.PrettyFormat};
        if (!this.properties.isDev()) {
            // 若非调试模式，则去掉格式化输出
            writerFeatures = new JSONWriter.Feature[]{JSONWriter.Feature.IgnoreNonFieldGetter};
        }
        fastJsonConfig.setWriterFeatures(writerFeatures);
        return fastJsonConfig;
    }
}