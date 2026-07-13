package ir.tahamohamadi.skill.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.skill.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminSkillService {
    private final SkillRepository skills;
    private final SkillTranslationRepository translations;
    private final SkillCategoryRepository categories;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;
    private final AuthenticatedAuditActor actor;

    public AdminSkillService(SkillRepository skills, SkillTranslationRepository translations, SkillCategoryRepository categories, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) { this.skills = skills; this.translations = translations; this.categories = categories; this.audit = audit; this.mapper = mapper; this.actor = actor; }

    @Transactional(readOnly = true)
    public Page<AdminSkillResponse> list(int page, int size, String sort) {
        var pageable = AdminSkillPaging.page(page, size, sort, Set.of("sortOrder", "skillKey"));
        Page<Skill> result = skills.findByDeletedAtIsNull(pageable);
        Map<UUID, List<SkillTranslation>> bySkill = result.getContent().isEmpty() ? Map.of() : translations.findBySkillIdInAndDeletedAtIsNull(result.getContent().stream().map(Skill::getId).toList()).stream().collect(Collectors.groupingBy(value -> value.getSkill().getId()));
        return new PageImpl<>(result.getContent().stream().map(value -> response(value, bySkill.get(value.getId()))).toList(), pageable, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminSkillResponse get(UUID id) { return response(skill(id)); }

    @Transactional
    public AdminSkillResponse create(AdminSkillRequest request) {
        Skill skill = skills.save(Skill.create(UUID.randomUUID(), request.skillKey(), category(request.categoryId()), request.sortOrder(), Instant.now()));
        saveTranslations(skill, request);
        skills.flush();
        record("ADMIN_SKILL_CREATED", skill.getId());
        return response(skill);
    }

    @Transactional
    public AdminSkillResponse update(UUID id, AdminSkillRequest request) {
        Skill skill = skill(id);
        version(skill, request.version());
        skill.update(request.skillKey(), category(request.categoryId()), request.sortOrder());
        saveTranslations(skill, request);
        skills.flush();
        record("ADMIN_SKILL_UPDATED", id);
        return response(skill);
    }

    @Transactional
    public void deactivate(UUID id, long version) {
        Skill skill = skill(id);
        version(skill, version);
        skill.deactivate();
        skills.flush();
        record("ADMIN_SKILL_DEACTIVATED", id);
    }

    private Skill skill(UUID id) { return skills.findById(id).filter(value -> value.getDeletedAt() == null).orElseThrow(() -> new NoSuchElementException("Skill not found")); }
    private SkillCategory category(UUID id) { return categories.findById(id).filter(value -> value.getDeletedAt() == null && value.isActive()).orElseThrow(() -> new NoSuchElementException("Skill category not found")); }
    private void saveTranslations(Skill skill, AdminSkillRequest request) { save(skill, LanguageCode.fa, request.fa()); save(skill, LanguageCode.en, request.en()); }
    private void save(Skill skill, LanguageCode language, AdminSkillTranslationRequest request) { translations.findBySkillIdAndLanguageCodeAndDeletedAtIsNull(skill.getId(), language).ifPresentOrElse(value -> value.update(request.name(), request.description()), () -> translations.save(SkillTranslation.create(UUID.randomUUID(), skill, language, request.name(), request.description(), Instant.now()))); }
    private AdminSkillResponse response(Skill skill) { return response(skill, translations.findBySkillIdAndDeletedAtIsNull(skill.getId())); }
    private AdminSkillResponse response(Skill skill, List<SkillTranslation> values) { return new AdminSkillResponse(skill.getId(), skill.getCategory().getId(), skill.getSkillKey(), skill.getSortOrder(), skill.isActive(), translation(values, LanguageCode.fa), translation(values, LanguageCode.en), skill.getVersion()); }
    private AdminSkillTranslationRequest translation(List<SkillTranslation> values, LanguageCode language) { SkillTranslation value = values.stream().filter(item -> item.getLanguageCode() == language).findFirst().orElseThrow(() -> new NoSuchElementException("Skill translation not found")); return new AdminSkillTranslationRequest(value.getName(), value.getDescription()); }
    private static void version(Skill value, Long version) { if (version == null || value.getVersion() != version) throw new ObjectOptimisticLockingFailureException(Skill.class, value.getId()); }
    private void record(String action, UUID id) { audit.save(AuditEvent.record(UUID.randomUUID(), Instant.now(), actor.required(), action, "SKILL", id, "SUCCESS", null, null, mapper.createObjectNode().put("changedFields", "managed"))); }
}
