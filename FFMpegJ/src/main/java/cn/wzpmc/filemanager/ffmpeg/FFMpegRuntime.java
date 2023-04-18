package cn.wzpmc.filemanager.ffmpeg;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.tika.Tika;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class FFMpegRuntime {
    private final Runtime runtime;
    private String ffmpegPath;
    private String ffprobePath;
    public FFMpegRuntime(){
        this.runtime = Runtime.getRuntime();
    }
    @SneakyThrows
    public boolean check() {
        Process exec = this.runtime.exec(new String[]{"whereis", "ffmpeg", "ffprobe"});
        exec.waitFor();
        String[] lines = new String(exec.getInputStream().readAllBytes()).split("\n");
        try {
            this.ffmpegPath = lines[0].replace("ffmpeg:", "").split(" ")[1];
            this.ffprobePath = lines[1].replace("ffprobe:", "").split(" ")[1];
        }catch (ArrayIndexOutOfBoundsException e){
            return false;
        }
        return true;
    }
    public Map<String, Object> getVideoInfo(File file) {
        try {
            Tika tika = new Tika();
            String contentType = tika.detect(file);
            if (contentType == null || !contentType.startsWith("video")) {
                return null;
            }
            ProcessBuilder processBuilder = new ProcessBuilder("ffprobe", "-v", "quiet", "-print_format", "json", "-show_streams", file.getAbsolutePath());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            process.waitFor();
            JSONObject jsonObject = JSONObject.parseObject(stringBuilder.toString());
            JSONArray streams = jsonObject.getJSONArray("streams");
            Map<String, Object> chineseVideoInfo = new LinkedHashMap<>();
            int audioStreamCount = 1;
            for (int i = 0; i < streams.size(); i++) {
                JSONObject stream = streams.getJSONObject(i);
                if ("video".equals(stream.getString("codec_type"))) {
                    Map<String, String> videoStreamResult = new LinkedHashMap<>();
                    videoStreamResult.put("编码格式", stream.getString("codec_name"));
                    videoStreamResult.put("宽度", stream.getString("width"));
                    videoStreamResult.put("高度", stream.getString("height"));
                    chineseVideoInfo.put("视频流", videoStreamResult);
                } else if ("audio".equals(stream.getString("codec_type"))) {
                    Map<String, String> audioStreamResult = new LinkedHashMap<>();
                    audioStreamResult.put("音频编码格式", stream.getString("codec_name"));
                    audioStreamResult.put("采样率", stream.getString("sample_rate") + " Hz");
                    audioStreamResult.put("声道数", stream.getString("channels"));
                    chineseVideoInfo.put("音频流#" + audioStreamCount, audioStreamResult);
                    audioStreamCount++;
                }
            }
            return chineseVideoInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
