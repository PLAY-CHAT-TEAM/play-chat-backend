package me.kycho.playchat.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class FileStoreTest {

    @Test
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
}