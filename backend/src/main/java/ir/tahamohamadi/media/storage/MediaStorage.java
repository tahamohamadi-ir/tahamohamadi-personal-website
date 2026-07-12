package ir.tahamohamadi.media.storage;

import java.io.IOException;
import java.io.InputStream;

public interface MediaStorage {
    void store(String storageKey, InputStream source) throws IOException;
    InputStream open(String storageKey) throws IOException;
    void delete(String storageKey) throws IOException;
}
