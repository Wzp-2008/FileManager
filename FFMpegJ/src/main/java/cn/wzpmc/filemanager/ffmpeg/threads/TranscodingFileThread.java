package cn.wzpmc.filemanager.ffmpeg.threads;

import cn.wzpmc.filemanager.ffmpeg.FFMpegRuntime;
import cn.wzpmc.filemanager.ffmpeg.enums.VideoEncoder;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TranscodingFileThread extends Thread{
    private final static Pattern getDataPattern = Pattern.compile("frame=\\s*(\\d+)\\s+fps=([\\d\\.]+)\\s+q=([\\d\\.]+)\\s+size=\\s*(\\d+kB)\\s+time=([\\d:.]+)\\s+bitrate=([\\d\\.]+kbits/s)\\s+speed=([\\d\\.]+x)\\s*");
    private final File input;
    @Getter
    private final VideoEncoder outputEncoder;
    @Getter
    private final File output;
    private final Callback callback;
    private final FFMpegRuntime ffMpegRuntime;
    private VideoEncoder inputEncoder;
    @Getter
    private long totalFrames;
    @Getter
    private float progress;
    @Getter
    private long frames;
    @Getter
    private float fps;
    @SneakyThrows
    public TranscodingFileThread(File input, VideoEncoder outputEncoder, File output, Callback callback, FFMpegRuntime ffMpegRuntime){
        this.input = input;
        this.outputEncoder = outputEncoder;
        this.output = output;
        this.callback = callback;
        this.ffMpegRuntime = ffMpegRuntime;
        ProcessBuilder probeProcessBuilder = new ProcessBuilder();
        probeProcessBuilder.command(
                this.ffMpegRuntime.getFfprobePath(),
                "-v",
                "quiet",
                "-print_format",
                "json",
                "-show_streams",
                this.input.getAbsolutePath()
        );
        Process probeProcess = probeProcessBuilder.start();
        probeProcess.waitFor();
        String fileProbeResult = new String(probeProcess.getInputStream().readAllBytes());
        JSONObject probeResultObject = JSONObject.parseObject(fileProbeResult);
        JSONArray streams = probeResultObject.getJSONArray("streams");
        for (int i = 0; i < streams.size(); i++) {
            JSONObject stream = streams.getJSONObject(i);
            if (stream.getString("codec_type").equals("video")) {
                this.inputEncoder = VideoEncoder.getEncoder(stream.getString("codec_name"));
                this.totalFrames = Long.parseLong(stream.getString("nb_frames"));
            }
        }
        if (this.inputEncoder == null){
            throw new RuntimeException(new IOException("The input file isn't video file!"));
        }
        super.setName("Transcoding-File-Thread-" + input.getName());
    }

    @Override
    public void run() {
        try{
            if (this.output.exists()){
                boolean delete = this.output.delete();
                if (!delete){
                    return;
                }
            }
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(
                    this.ffMpegRuntime.getFfmpegPath(),
                    "-hide_banner",
                    "-vsync",
                    "0",
                    "-hwaccel",
                    "cuvid",
                    "-hwaccel_output_format",
                    "cuda",
                    "-c:v",
                    this.inputEncoder.decoderName,
                    "-i",
                    this.input.getAbsolutePath(),
                    "-c:v",
                    this.outputEncoder.encoderName,
                    "-c:a",
                    "copy",
                    this.output.getAbsolutePath()
            );
            System.out.println(String.join(" ", processBuilder.command()));
            Process start = processBuilder.start();
            BufferedInputStream in = new BufferedInputStream(start.getErrorStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
            String lineStr;
            while (start.isAlive()) {
                lineStr = inBr.readLine();
                if (lineStr == null){
                    this.frames = totalFrames;
                    progress = 1.0f;
                    return;
                }
                Matcher matcher = getDataPattern.matcher(lineStr);
                if (matcher.find()) {
                    this.frames = Long.parseLong(matcher.group(1));
                    this.fps = Float.parseFloat(matcher.group(2));
                    progress = (float) this.frames / totalFrames;
                }
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }finally {
            this.callback.onDone(this);
        }
    }

    @FunctionalInterface
    public interface Callback{
        void onDone(TranscodingFileThread thread);
    }
}
