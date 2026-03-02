package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FastJsonConfiguration implements WebMvcConfigurer {

    private final FileManagerProperties properties;

    @Override
    public void configureMessageConverters(@NonNull HttpMessageConverters.ServerBuilder builder) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = createFastjsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        config.setCharset(StandardCharsets.UTF_8);
        converter.setFastJsonConfig(config);
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
        builder.withJsonConverter(converter);
    }

    private @NonNull FastJsonConfig createFastjsonConfig() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastJsonConfig.setCharset(StandardCharsets.UTF_8);
        JSONWriter.Feature[] writerFeatures = {JSONWriter.Feature.IgnoreNonFieldGetter, JSONWriter.Feature.PrettyFormat};
        if (!this.properties.isDev()) {
            writerFeatures = new JSONWriter.Feature[]{JSONWriter.Feature.IgnoreNonFieldGetter};
        }
        fastJsonConfig.setWriterFeatures(writerFeatures);
        return fastJsonConfig;
    }
}