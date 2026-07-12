package ir.tahamohamadi.media.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.nio.file.Path;

@ConfigurationProperties("taha.media.storage")
public class MediaStorageProperties {
    private Path root = Path.of("./var/media");
    public Path getRoot() { return root; }
    public void setRoot(Path root) { this.root = root; }
}
