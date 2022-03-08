package me.kycho.playchat.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class FileStoreTest {

    @Test
    @DisplayName("파일 저장 정상")
    void storeFileTest() throws IOException {

        // given
        String uploadPath = "./src/test/resources/upload/";
        File folder = new File(uploadPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        FileStore fileStore = new FileStore(uploadPath);

        MockMultipartFile imageFile = new MockMultipartFile(
            "profileImage", "imageForTest.png", MediaType.IMAGE_PNG_VALUE,
            new FileInputStream("./src/test/resources/static/imageForTest.png")
        );

        // when
        String storedFileName = fileStore.storeFile(imageFile);
        String storedFileFullPath = fileStore.getFullPath(storedFileName);
        File uploadedFile = new File(storedFileFullPath);

        // then
        assertThat(uploadedFile.exists()).isTrue();
        assertThat(uploadedFile.getName()).isEqualTo(storedFileName);

        uploadedFile.delete();
        folder.delete();
    }

    @Test
    @DisplayName("파일 삭제 정상")
    void deleteFileTest() throws IOException {

        // given
        String targetFileName = "existingFile.png";
        String fileDir = "./src/test/resources/static/";

        File file = new File("./src/test/resources/static/imageForTest.png");
        File existingFile = new File(fileDir + targetFileName);
        Files.copy(file.toPath(), existingFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // when
        FileStore fileStore = new FileStore(fileDir);
        boolean result = fileStore.deleteFile(targetFileName);

        // then
        assertThat(result).isTrue();
        assertThat(existingFile.exists()).isFalse();
    }

    @Test
    @DisplayName("파일 삭제 ERROR (존재하지 않는 파일 삭제)")
    void deleteFileErrorTest_notExist() throws IOException {

        // given
        String targetFileName = "notExistingFile.png";
        String fileDir = "./src/test/resources/static/";

        // when
        FileStore fileStore = new FileStore(fileDir);
        boolean result = fileStore.deleteFile(targetFileName);

        // then
        assertThat(result).isFalse();
    }
}
