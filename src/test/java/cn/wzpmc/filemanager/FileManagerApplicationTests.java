package cn.wzpmc.filemanager;

import cn.wzpmc.filemanager.utils.stream.SerialFileInputStream;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

class FileManagerApplicationTests {
	private static final File baseFolder = new File("run");

	@Test
	void serialFileStreamTest() throws IOException {
		File outputFile = new File(baseFolder, "out.txt");
		if (outputFile.exists()) {
			outputFile.delete();
		}
		try (SerialFileInputStream serialFileInputStream = new SerialFileInputStream(List.of(new File(baseFolder, "a.txt"), new File(baseFolder, "b.txt"), new File(baseFolder, "c.txt"), new File(baseFolder, "d.txt"))); FileOutputStream fos = new FileOutputStream(outputFile)) {
			byte[] buf = new byte[1026];
			int read;
			while ((read = serialFileInputStream.read(buf)) != -1) {
				fos.write(buf, 0, read);
			}
		}
	}

}
