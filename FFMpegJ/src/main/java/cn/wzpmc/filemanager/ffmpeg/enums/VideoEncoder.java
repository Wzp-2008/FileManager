package cn.wzpmc.filemanager.ffmpeg.enums;

public enum VideoEncoder {
    H264("h264", "h264_nvenc", "h264_cuvid"),
    HEVC("h265", "hevc_nvenc", "hevc_cuvid");
    public final String name;
    public final String encoderName;
    public final String decoderName;
    VideoEncoder(String name, String encoderName, String decoderName){
        this.name = name;
        this.encoderName = encoderName;
        this.decoderName = decoderName;
    }
    public static VideoEncoder getEncoder(String name){
        if (name.equals("h264")){
            return H264;
        }
        return HEVC;
    }
}
