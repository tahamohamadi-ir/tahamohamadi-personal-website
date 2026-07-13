package ir.tahamohamadi.content.social.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.*;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.content.social.*;
import org.springframework.data.domain.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class AdminSocialLinkService {
    private final SocialLinkRepository links; private final AuditEventRepository audit; private final ObjectMapper mapper; private final AuthenticatedAuditActor actor;
    public AdminSocialLinkService(SocialLinkRepository links, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) { this.links=links; this.audit=audit; this.mapper=mapper; this.actor=actor; }
    @Transactional(readOnly=true) public Page<AdminSocialLinkResponse> list(int page,int size,String sort) { return links.findByDeletedAtIsNull(paging(page,size,sort)).map(this::response); }
    @Transactional(readOnly=true) public AdminSocialLinkResponse get(UUID id) { return response(link(id)); }
    public AdminSocialLinkResponse create(AdminSocialLinkRequest request) { SocialLink value=links.saveAndFlush(SocialLink.create(UUID.randomUUID(),request.platformCode(),request.url(),request.sortOrder(),Instant.now())); record("ADMIN_SOCIAL_LINK_CREATED",value.getId()); return response(value); }
    public AdminSocialLinkResponse update(UUID id,AdminSocialLinkRequest request) { SocialLink value=link(id); version(value,request.version()); value.update(request.platformCode(),request.url(),request.sortOrder(),Instant.now()); links.flush(); record("ADMIN_SOCIAL_LINK_UPDATED",id); return response(value); }
    public AdminSocialLinkResponse activate(UUID id,long requested) { SocialLink value=link(id); version(value,requested); value.activate(Instant.now()); links.flush(); record("ADMIN_SOCIAL_LINK_PUBLISHED",id); return response(value); }
    public AdminSocialLinkResponse deactivate(UUID id,long requested) { SocialLink value=link(id); version(value,requested); value.deactivate(Instant.now()); links.flush(); record("ADMIN_SOCIAL_LINK_DEACTIVATED",id); return response(value); }
    public void delete(UUID id,long requested) { SocialLink value=link(id); version(value,requested); value.softDelete(actor.required(),Instant.now()); links.flush(); record("ADMIN_SOCIAL_LINK_DELETED",id); }
    private SocialLink link(UUID id) { return links.findById(id).filter(value -> value.getDeletedAt()==null).orElseThrow(() -> new NoSuchElementException("Social link not found")); }
    private static void version(SocialLink value,Long requested) { if(requested==null || value.getVersion()!=requested) throw new ObjectOptimisticLockingFailureException(SocialLink.class,value.getId()); }
    private static Pageable paging(int page,int size,String requested) { String[] pieces=requested.split(",",-1); if(pieces.length!=2 || !Set.of("updatedAt","sortOrder","platformCode").contains(pieces[0])) throw new IllegalArgumentException("Unsupported sort"); try { return PageRequest.of(page,size,Sort.by(Sort.Direction.fromString(pieces[1]),pieces[0]).and(Sort.by("id"))); } catch(IllegalArgumentException e) { throw new IllegalArgumentException("Unsupported sort"); } }
    private AdminSocialLinkResponse response(SocialLink value) { return new AdminSocialLinkResponse(value.getId(),value.getPlatformCode(),value.getUrl(),value.getSortOrder(),value.isActive(),value.getVersion()); }
    private void record(String action,UUID id) { audit.save(AuditEvent.record(UUID.randomUUID(),Instant.now(),actor.required(),action,"SOCIAL_LINK",id,"SUCCESS",null,null,mapper.createObjectNode().put("changedFields","managed"))); }
}
