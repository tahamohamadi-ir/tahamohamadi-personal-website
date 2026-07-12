package ir.tahamohamadi.media.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class LocalFileSystemMediaStorage implements MediaStorage {
    private final Path root;

    public LocalFileSystemMediaStorage(Path root) {
        try { this.root = root.toAbsolutePath().normalize(); Files.createDirectories(this.root); }
        catch (IOException exception) { throw new IllegalStateException("Cannot initialize media storage", exception); }
    }

    @Override public void store(String storageKey, InputStream source) throws IOException {
        Path target = resolve(storageKey); Files.createDirectories(target.getParent());
        Path temporary = resolve(".tmp/" + UUID.randomUUID());
        try (source) {
            Files.createDirectories(temporary.getParent());
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException exception) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    @Override public InputStream open(String storageKey) throws IOException { return Files.newInputStream(resolve(storageKey)); }
    @Override public void delete(String storageKey) throws IOException { Files.deleteIfExists(resolve(storageKey)); }

    private Path resolve(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.startsWith("/") || key.startsWith("\\")
                || key.matches("^[A-Za-z]:.*")) throw new IllegalArgumentException("Unsafe storage key");
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) throw new IllegalArgumentException("Storage key escapes root");
        return path;
    }
}
