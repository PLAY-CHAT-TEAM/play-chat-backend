package me.kycho.playchat.common;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStore {

    private final String fileDir;

    public FileStore(@Value("${file.dir}") String fileDir) {
        this.fileDir = fileDir;
    }

    public String storeFile(MultipartFile multipartFile) throws IOException {

        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String uuid = UUID.randomUUID().toString();
        String originalFileName = multipartFile.getOriginalFilename();
        String storeFileName = uuid + "_" + originalFileName;

        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return storeFileName;
    }

    public boolean deleteFile(String filename) {
        File file = new File(fileDir + filename);
        return file.delete();
    }

    public String getFullPath(String filename) {
        return fileDir + filename;
    }
}
