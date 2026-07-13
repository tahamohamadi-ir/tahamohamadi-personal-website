package ir.tahamohamadi.portfolio.project.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.*;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.media.asset.*;
import ir.tahamohamadi.portfolio.project.*;
import ir.tahamohamadi.skill.*;
import org.springframework.data.domain.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminProjectService {
    private final PortfolioProjectRepository projects;
    private final PortfolioProjectTranslationRepository translations;
    private final PortfolioProjectSkillRepository projectSkills;
    private final MediaAssetRepository media;
    private final SkillRepository skills;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;
    private final AuthenticatedAuditActor actor;

    public AdminProjectService(PortfolioProjectRepository projects, PortfolioProjectTranslationRepository translations, PortfolioProjectSkillRepository projectSkills, MediaAssetRepository media, SkillRepository skills, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) {
        this.projects = projects;
        this.translations = translations;
        this.projectSkills = projectSkills;
        this.media = media;
        this.skills = skills;
        this.audit = audit;
        this.mapper = mapper;
        this.actor = actor;
    }

    @Transactional(readOnly = true)
    public Page<AdminProjectSummary> list(int page, int size, String sort) {
        Pageable pageable = AdminProjectPaging.page(page, size, sort);
        Page<PortfolioProject> result = projects.findByDeletedAtIsNull(pageable);
        if (result.isEmpty()) return new PageImpl<>(List.of(), pageable, 0);
        Map<UUID, List<PortfolioProjectTranslation>> localized = translations.findByProjectIdInAndDeletedAtIsNull(result.getContent().stream().map(PortfolioProject::getId).toList())
                .stream().collect(Collectors.groupingBy(value -> value.getProject().getId()));
        return new PageImpl<>(result.getContent().stream().map(project -> summary(project, localized.get(project.getId()))).toList(), pageable, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminProjectResponse get(UUID id) { return response(project(id)); }

    public AdminProjectResponse create(AdminProjectCreateRequest request) {
        actor.required();
        Instant now = Instant.now();
        PortfolioProject project = projects.save(PortfolioProject.create(UUID.randomUUID(), request.projectKey(), cover(request.coverMediaId()), request.startedOn(), request.endedOn(), request.projectUrl(), request.repositoryUrl(), request.sortOrder(), now));
        saveTranslations(project, request.fa(), request.en(), now);
        replaceSkills(project, request.skills());
        projects.flush();
        record("ADMIN_PROJECT_CREATED", project.getId());
        return response(project);
    }

    public AdminProjectResponse update(UUID id, AdminProjectUpdateRequest request) {
        PortfolioProject project = project(id);
        version(project, request.version());
        Instant now = Instant.now();
        project.update(request.projectKey(), cover(request.coverMediaId()), request.startedOn(), request.endedOn(), request.projectUrl(), request.repositoryUrl(), request.sortOrder(), now);
        saveTranslations(project, request.fa(), request.en(), now);
        replaceSkills(project, request.skills());
        projects.flush();
        record("ADMIN_PROJECT_UPDATED", id);
        return response(project);
    }

    public AdminProjectResponse publish(UUID id, long version) {
        PortfolioProject project = project(id);
        version(project, version);
        if (project.getStartedOn() == null) throw new IllegalArgumentException("startedOn is required before publishing");
        project.publish(Instant.now());
        projects.flush();
        record("ADMIN_PROJECT_PUBLISHED", id);
        return response(project);
    }

    public AdminProjectResponse archive(UUID id, long version) {
        PortfolioProject project = project(id);
        version(project, version);
        project.archive(Instant.now());
        projects.flush();
        record("ADMIN_PROJECT_ARCHIVED", id);
        return response(project);
    }

    public void delete(UUID id, long version) {
        PortfolioProject project = project(id);
        version(project, version);
        project.softDelete(actor.required(), Instant.now());
        projects.flush();
        record("ADMIN_PROJECT_DELETED", id);
    }

    private PortfolioProject project(UUID id) {
        return projects.findById(id).filter(value -> value.getDeletedAt() == null).orElseThrow(() -> new NoSuchElementException("Project not found"));
    }

    private MediaAsset cover(UUID id) {
        if (id == null) return null;
        return media.findByIdAndStatusAndDeletedAtIsNull(id, MediaAssetStatus.ACTIVE).orElseThrow(() -> new NoSuchElementException("Media asset not found"));
    }

    private void saveTranslations(PortfolioProject project, AdminProjectTranslationRequest fa, AdminProjectTranslationRequest en, Instant now) {
        List<PortfolioProjectTranslation> existing = translations.findByProjectIdAndDeletedAtIsNull(project.getId());
        saveTranslation(project, existing, LanguageCode.fa, fa, now);
        saveTranslation(project, existing, LanguageCode.en, en, now);
    }

    private void saveTranslation(PortfolioProject project, List<PortfolioProjectTranslation> existing, LanguageCode language, AdminProjectTranslationRequest value, Instant now) {
        existing.stream().filter(translation -> translation.getLanguageCode() == language).findFirst()
                .ifPresentOrElse(translation -> translation.update(value.title(), value.slug(), value.summary(), value.bodyMarkdown(), value.seoTitle(), value.seoDescription(), now),
                        () -> translations.save(PortfolioProjectTranslation.create(UUID.randomUUID(), project, language, value.title(), value.slug(), value.summary(), value.bodyMarkdown(), value.seoTitle(), value.seoDescription(), now)));
    }

    private void replaceSkills(PortfolioProject project, List<AdminProjectSkillReferenceRequest> references) {
        if (references.stream().map(AdminProjectSkillReferenceRequest::skillId).distinct().count() != references.size()) throw new IllegalArgumentException("Skill ids must be unique");
        if (references.stream().map(AdminProjectSkillReferenceRequest::sortOrder).distinct().count() != references.size()) throw new IllegalArgumentException("Skill sort orders must be unique");
        List<UUID> ids = references.stream().map(AdminProjectSkillReferenceRequest::skillId).toList();
        List<Skill> values = skills.findAllById(ids);
        if (values.size() != ids.size() || values.stream().anyMatch(value -> value.getDeletedAt() != null || !value.isActive())) throw new NoSuchElementException("Skill not found");
        Map<UUID, Skill> byId = values.stream().collect(Collectors.toMap(Skill::getId, value -> value));
        projectSkills.deleteAllByProjectId(project.getId());
        projectSkills.flush();
        projectSkills.saveAll(references.stream().map(reference -> PortfolioProjectSkill.assign(project, byId.get(reference.skillId()), reference.sortOrder())).toList());
    }

    private AdminProjectSummary summary(PortfolioProject project, List<PortfolioProjectTranslation> localized) {
        return new AdminProjectSummary(project.getId(), project.getProjectKey(), project.getStatus().name(), project.getSortOrder(), translation(localized, LanguageCode.fa), translation(localized, LanguageCode.en), project.getVersion());
    }

    private AdminProjectResponse response(PortfolioProject project) {
        List<PortfolioProjectTranslation> localized = translations.findByProjectIdAndDeletedAtIsNull(project.getId());
        List<AdminProjectSkillResponse> references = projectSkills.findByProjectIdWithSkillOrderBySortOrder(project.getId()).stream()
                .map(value -> new AdminProjectSkillResponse(value.getSkill().getId(), value.getSortOrder())).toList();
        return new AdminProjectResponse(project.getId(), project.getProjectKey(), project.getCoverMedia() == null ? null : project.getCoverMedia().getId(), project.getStatus().name(), project.getStartedOn(), project.getEndedOn(), project.getProjectUrl(), project.getRepositoryUrl(), project.getSortOrder(), translation(localized, LanguageCode.fa), translation(localized, LanguageCode.en), references, project.getVersion());
    }

    private AdminProjectTranslationRequest translation(List<PortfolioProjectTranslation> values, LanguageCode language) {
        PortfolioProjectTranslation value = values.stream().filter(translation -> translation.getLanguageCode() == language).findFirst().orElseThrow(() -> new NoSuchElementException("Project translation not found"));
        return new AdminProjectTranslationRequest(value.getTitle(), value.getSlug(), value.getSummary(), value.getBodyMarkdown(), value.getSeoTitle(), value.getSeoDescription());
    }

    private static void version(PortfolioProject project, long requested) {
        if (project.getVersion() != requested) throw new ObjectOptimisticLockingFailureException(PortfolioProject.class, project.getId());
    }

    private void record(String action, UUID id) {
        audit.save(AuditEvent.record(UUID.randomUUID(), Instant.now(), actor.required(), action, "PORTFOLIO_PROJECT", id, "SUCCESS", null, null, mapper.createObjectNode().put("changedFields", "managed")));
    }
}
