package ir.tahamohamadi.content.social;
import ir.tahamohamadi.common.persistence.AuditedSoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="social_link") @Getter @NoArgsConstructor(access=AccessLevel.PROTECTED)
public class SocialLink extends AuditedSoftDeletableEntity {
    @Column(name="platform_code",nullable=false,length=64) private String platformCode;
    @Column(nullable=false,length=2048) private String url;
    @Column(name="sort_order",nullable=false) private int sortOrder;
    @Column(name="is_active",nullable=false) private boolean active;
    private SocialLink(UUID id,String platformCode,String url,int sortOrder,Instant createdAt){initialize(id,createdAt);update(platformCode,url,sortOrder,createdAt);active=true;}
    public static SocialLink create(UUID id,String platformCode,String url,int sortOrder,Instant createdAt){return new SocialLink(id,platformCode,url,sortOrder,createdAt);}
    public void update(String platformCode,String url,int sortOrder,Instant at){this.platformCode=requireNonBlank(platformCode,"platformCode");this.url=requireNonBlank(url,"url");if(sortOrder<0)throw new IllegalArgumentException("sortOrder must be nonnegative");this.sortOrder=sortOrder;updatedAt=java.util.Objects.requireNonNull(at,"at");}
    public void activate(Instant at){active=true; updatedAt=java.util.Objects.requireNonNull(at,"at");}
    public void deactivate(Instant at){active=false; updatedAt=java.util.Objects.requireNonNull(at,"at");}
}
