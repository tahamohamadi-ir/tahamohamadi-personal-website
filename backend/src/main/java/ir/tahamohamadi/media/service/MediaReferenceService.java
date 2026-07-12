package ir.tahamohamadi.media.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import java.util.UUID;

@Service @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class MediaReferenceService {
    private final EntityManager entityManager;
    public MediaReferenceService(EntityManager entityManager) { this.entityManager = entityManager; }
    public boolean isReferenced(UUID id) { return referenceCount(id) > 0; }
    public boolean isPubliclyReferenced(UUID id) {
        String sql = "select count(*) from (" +
                "select 1 from content_page_translation t join content_page p on p.id=t.content_page_id where t.og_media_id=:id and t.deleted_at is null and p.deleted_at is null and p.status='PUBLISHED' union all " +
                "select 1 from blog_post p where p.cover_media_id=:id and p.deleted_at is null and p.status='PUBLISHED' union all " +
                "select 1 from blog_post_media m join blog_post p on p.id=m.blog_post_id where m.media_asset_id=:id and p.deleted_at is null and p.status='PUBLISHED' union all " +
                "select 1 from portfolio_project p where p.cover_media_id=:id and p.deleted_at is null and p.status='PUBLISHED' union all " +
                "select 1 from publication p where p.cover_media_id=:id and p.deleted_at is null and p.content_status='PUBLISHED' union all " +
                "select 1 from resume_document d where d.media_asset_id=:id and d.deleted_at is null and d.status='PUBLISHED') x";
        return ((Number) entityManager.createNativeQuery(sql).setParameter("id", id).getSingleResult()).longValue() > 0;
    }
    private long referenceCount(UUID id) {
        String sql = "select (select count(*) from content_page_translation where og_media_id=:id) + " +
                "(select count(*) from blog_post where cover_media_id=:id) + (select count(*) from blog_post_media where media_asset_id=:id) + " +
                "(select count(*) from portfolio_project where cover_media_id=:id) + (select count(*) from publication where cover_media_id=:id) + " +
                "(select count(*) from resume_document where media_asset_id=:id)";
        return ((Number) entityManager.createNativeQuery(sql).setParameter("id", id).getSingleResult()).longValue();
    }
}
