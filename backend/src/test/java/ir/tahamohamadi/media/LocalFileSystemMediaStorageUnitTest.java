package ir.tahamohamadi.media;

import ir.tahamohamadi.media.storage.LocalFileSystemMediaStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileSystemMediaStorageUnitTest {

    @TempDir
    Path root;

    @Test
    void storesOnlyGeneratedKeysUnderItsConfiguredRoot() throws Exception {
        LocalFileSystemMediaStorage storage = new LocalFileSystemMediaStorage(root);
        storage.store("a/b/c.pdf", new ByteArrayInputStream("pdf".getBytes()));

        assertThat(storage.open("a/b/c.pdf").readAllBytes()).isEqualTo("pdf".getBytes());
        assertThatThrownBy(() -> storage.open("../outside.pdf")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.store("C:\\outside.pdf", new ByteArrayInputStream(new byte[0])))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
