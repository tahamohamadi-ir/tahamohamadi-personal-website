package ir.tahamohamadi.media.config;

import ir.tahamohamadi.media.storage.LocalFileSystemMediaStorage;
import ir.tahamohamadi.media.storage.MediaStorage;
import ir.tahamohamadi.media.validation.MediaPolicy;
import ir.tahamohamadi.media.validation.MediaValidationService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MediaStorageProperties.class)
public class MediaConfiguration {
    @Bean MediaStorage mediaStorage(MediaStorageProperties properties) { return new LocalFileSystemMediaStorage(properties.getRoot()); }
    @Bean MediaPolicy mediaPolicy() { return new MediaPolicy(); }
    @Bean MediaValidationService mediaValidationService(MediaPolicy policy) { return new MediaValidationService(policy); }
}
