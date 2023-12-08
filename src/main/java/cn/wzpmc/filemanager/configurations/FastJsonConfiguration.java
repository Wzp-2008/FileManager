package cn.wzpmc.filemanager.configurations;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class FastJsonConfiguration implements WebMvcConfigurer {
    private static final boolean debug = true;
    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastJsonConfig.setCharset(StandardCharsets.UTF_8);
        JSONWriter.Feature[] writerFeatures = {JSONWriter.Feature.MapSortField, JSONWriter.Feature.IgnoreNonFieldGetter, JSONWriter.Feature.PrettyFormat};
        if (!debug) {
            writerFeatures = new JSONWriter.Feature[]{JSONWriter.Feature.MapSortField, JSONWriter.Feature.IgnoreNonFieldGetter};
        }
        fastJsonConfig.setWriterFeatures(writerFeatures);
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converter.setFastJsonConfig(fastJsonConfig);
        converters.add(0, converter);
    }
}
