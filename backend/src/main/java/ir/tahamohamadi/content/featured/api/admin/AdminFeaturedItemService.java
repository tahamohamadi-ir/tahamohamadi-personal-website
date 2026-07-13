package ir.tahamohamadi.content.featured.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.*;
import ir.tahamohamadi.blog.post.*;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.ContentStatus;
import ir.tahamohamadi.content.featured.*;
import ir.tahamohamadi.portfolio.project.*;
import ir.tahamohamadi.publication.*;
import org.springframework.data.domain.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class AdminFeaturedItemService {
    private final FeaturedItemRepository featured; private final BlogPostRepository posts; private final PortfolioProjectRepository projects; private final PublicationRepository publications; private final AuditEventRepository audit; private final ObjectMapper mapper; private final AuthenticatedAuditActor actor;
    public AdminFeaturedItemService(FeaturedItemRepository featured, BlogPostRepository posts, PortfolioProjectRepository projects, PublicationRepository publications, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) { this.featured=featured; this.posts=posts; this.projects=projects; this.publications=publications; this.audit=audit; this.mapper=mapper; this.actor=actor; }
    @Transactional(readOnly = true) public Page<AdminFeaturedItemResponse> list(int page, int size, String sort) { return featured.findByDeletedAtIsNull(pageing(page,size,sort)).map(this::response); }
    @Transactional(readOnly = true) public AdminFeaturedItemResponse get(UUID id) { return response(item(id)); }
    public AdminFeaturedItemResponse create(AdminFeaturedItemRequest request) { Target target=target(request); FeaturedItem value=featured.saveAndFlush(FeaturedItem.create(UUID.randomUUID(),request.slotKey(),target.post(),target.project(),target.publication(),request.sortOrder(),request.startsAt(),request.endsAt(),Instant.now())); record("ADMIN_FEATURED_ITEM_CREATED",value.getId()); return response(value); }
    public AdminFeaturedItemResponse update(UUID id, AdminFeaturedItemRequest request) { FeaturedItem value=item(id); version(value,request.version()); Target target=target(request); value.update(request.slotKey(),target.post(),target.project(),target.publication(),request.sortOrder(),request.startsAt(),request.endsAt(),Instant.now()); featured.flush(); record("ADMIN_FEATURED_ITEM_UPDATED",id); return response(value); }
    public AdminFeaturedItemResponse activate(UUID id, long requested) { FeaturedItem value=item(id); version(value,requested); requirePublished(value); value.activate(Instant.now()); featured.flush(); record("ADMIN_FEATURED_ITEM_PUBLISHED",id); return response(value); }
    public AdminFeaturedItemResponse deactivate(UUID id, long requested) { FeaturedItem value=item(id); version(value,requested); value.deactivate(Instant.now()); featured.flush(); record("ADMIN_FEATURED_ITEM_DEACTIVATED",id); return response(value); }
    public void delete(UUID id, long requested) { FeaturedItem value=item(id); version(value,requested); value.softDelete(actor.required(),Instant.now()); featured.flush(); record("ADMIN_FEATURED_ITEM_DELETED",id); }
    private FeaturedItem item(UUID id) { return featured.findById(id).filter(value -> value.getDeletedAt()==null).orElseThrow(() -> new NoSuchElementException("Featured item not found")); }
    private Target target(AdminFeaturedItemRequest request) { return switch (request.targetType()) { case BLOG_POST -> { BlogPost post=posts.findById(request.targetId()).filter(value -> value.getDeletedAt()==null && value.getStatus()==ContentStatus.PUBLISHED).orElseThrow(() -> new IllegalArgumentException("Featured target must be published")); yield new Target(post,null,null); } case PORTFOLIO_PROJECT -> { PortfolioProject project=projects.findById(request.targetId()).filter(value -> value.getDeletedAt()==null && value.getStatus()==ContentStatus.PUBLISHED).orElseThrow(() -> new IllegalArgumentException("Featured target must be published")); yield new Target(null,project,null); } case PUBLICATION -> { Publication publication=publications.findById(request.targetId()).filter(value -> value.getDeletedAt()==null && value.getContentStatus()==ContentStatus.PUBLISHED).orElseThrow(() -> new IllegalArgumentException("Featured target must be published")); yield new Target(null,null,publication); } }; }
    private void requirePublished(FeaturedItem value) { if (value.getBlogPost()!=null && (value.getBlogPost().getDeletedAt()!=null || value.getBlogPost().getStatus()!=ContentStatus.PUBLISHED) || value.getPortfolioProject()!=null && (value.getPortfolioProject().getDeletedAt()!=null || value.getPortfolioProject().getStatus()!=ContentStatus.PUBLISHED) || value.getPublication()!=null && (value.getPublication().getDeletedAt()!=null || value.getPublication().getContentStatus()!=ContentStatus.PUBLISHED)) throw new IllegalStateException("Featured target must be published"); }
    private static void version(FeaturedItem value, Long requested) { if (requested==null || value.getVersion()!=requested) throw new ObjectOptimisticLockingFailureException(FeaturedItem.class,value.getId()); }
    private static Pageable pageing(int page, int size, String requested) { String[] pieces=requested.split(",",-1); if(pieces.length!=2 || !Set.of("updatedAt","sortOrder","slotKey").contains(pieces[0])) throw new IllegalArgumentException("Unsupported sort"); try { return PageRequest.of(page,size,Sort.by(Sort.Direction.fromString(pieces[1]),pieces[0]).and(Sort.by("id"))); } catch (IllegalArgumentException e) { throw new IllegalArgumentException("Unsupported sort"); } }
    private AdminFeaturedItemResponse response(FeaturedItem value) { if(value.getBlogPost()!=null) return new AdminFeaturedItemResponse(value.getId(),value.getSlotKey(),FeaturedTargetType.BLOG_POST,value.getBlogPost().getId(),value.getSortOrder(),value.getStartsAt(),value.getEndsAt(),value.isActive(),value.getVersion()); if(value.getPortfolioProject()!=null) return new AdminFeaturedItemResponse(value.getId(),value.getSlotKey(),FeaturedTargetType.PORTFOLIO_PROJECT,value.getPortfolioProject().getId(),value.getSortOrder(),value.getStartsAt(),value.getEndsAt(),value.isActive(),value.getVersion()); return new AdminFeaturedItemResponse(value.getId(),value.getSlotKey(),FeaturedTargetType.PUBLICATION,value.getPublication().getId(),value.getSortOrder(),value.getStartsAt(),value.getEndsAt(),value.isActive(),value.getVersion()); }
    private void record(String action, UUID id) { audit.save(AuditEvent.record(UUID.randomUUID(),Instant.now(),actor.required(),action,"FEATURED_ITEM",id,"SUCCESS",null,null,mapper.createObjectNode().put("changedFields","managed"))); }
    private record Target(BlogPost post, PortfolioProject project, Publication publication) { }
}
