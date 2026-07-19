package ir.tahamohamadi.skill.api.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tahamohamadi.audit.event.AuditEvent;
import ir.tahamohamadi.audit.event.AuditEventRepository;
import ir.tahamohamadi.common.audit.AuthenticatedAuditActor;
import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.skill.SkillCategory;
import ir.tahamohamadi.skill.SkillCategoryRepository;
import ir.tahamohamadi.skill.SkillCategoryTranslation;
import ir.tahamohamadi.skill.SkillCategoryTranslationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminSkillCategoryService {
    private final SkillCategoryRepository categories;
    private final SkillCategoryTranslationRepository translations;
    private final AuditEventRepository audit;
    private final ObjectMapper mapper;
    private final AuthenticatedAuditActor actor;

    public AdminSkillCategoryService(SkillCategoryRepository categories, SkillCategoryTranslationRepository translations, AuditEventRepository audit, ObjectMapper mapper, AuthenticatedAuditActor actor) { this.categories = categories; this.translations = translations; this.audit = audit; this.mapper = mapper; this.actor = actor; }

    @Transactional(readOnly = true)
    public Page<AdminSkillCategoryResponse> list(int page, int size, String sort) {
        var pageable = AdminSkillPaging.page(page, size, sort, Set.of("sortOrder", "categoryKey"));
        Page<SkillCategory> result = categories.findByDeletedAtIsNull(pageable);
        Map<UUID, List<SkillCategoryTranslation>> byCategory = result.getContent().isEmpty() ? Map.of() : translations.findByCategoryIdInAndDeletedAtIsNull(result.getContent().stream().map(SkillCategory::getId).toList()).stream().collect(Collectors.groupingBy(value -> value.getCategory().getId()));
        return new PageImpl<>(result.getContent().stream().map(value -> response(value, byCategory.get(value.getId()))).toList(), pageable, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AdminSkillCategoryResponse get(UUID id) { return response(category(id)); }

    @Transactional
    public AdminSkillCategoryResponse create(AdminSkillCategoryRequest request) {
        SkillCategory category = categories.save(SkillCategory.create(UUID.randomUUID(), request.categoryKey(), request.sortOrder(), Instant.now()));
        saveTranslations(category, request);
        categories.flush();
        record("ADMIN_SKILL_CATEGORY_CREATED", category.getId());
        return response(category);
    }

    @Transactional
    public AdminSkillCategoryResponse update(UUID id, AdminSkillCategoryRequest request) {
        SkillCategory category = category(id);
        version(category, request.version());
        category.update(request.categoryKey(), request.sortOrder());
        saveTranslations(category, request);
        categories.flush();
        record("ADMIN_SKILL_CATEGORY_UPDATED", id);
        return response(category);
    }

    @Transactional
    public void deactivate(UUID id, long version) {
        SkillCategory category = category(id);
        version(category, version);
        category.deactivate();
        categories.flush();
        record("ADMIN_SKILL_CATEGORY_DEACTIVATED", id);
    }

    private SkillCategory category(UUID id) { return categories.findById(id).filter(value -> value.getDeletedAt() == null).orElseThrow(() -> new NoSuchElementException("Skill category not found")); }
    private void saveTranslations(SkillCategory category, AdminSkillCategoryRequest request) { save(category, LanguageCode.fa, request.fa()); save(category, LanguageCode.en, request.en()); }
    private void save(SkillCategory category, LanguageCode language, AdminSkillTranslationRequest request) { translations.findByCategoryIdAndLanguageCodeAndDeletedAtIsNull(category.getId(), language).ifPresentOrElse(value -> value.update(request.name(), request.description()), () -> translations.save(SkillCategoryTranslation.create(UUID.randomUUID(), category, language, request.name(), request.description(), Instant.now()))); }
    private AdminSkillCategoryResponse response(SkillCategory category) { return response(category, translations.findByCategoryIdAndDeletedAtIsNull(category.getId())); }
    private AdminSkillCategoryResponse response(SkillCategory category, List<SkillCategoryTranslation> values) { return new AdminSkillCategoryResponse(category.getId(), category.getCategoryKey(), category.getSortOrder(), category.isActive(), translation(values, LanguageCode.fa), translation(values, LanguageCode.en), category.getVersion()); }
    private AdminSkillTranslationRequest translation(List<SkillCategoryTranslation> values, LanguageCode language) { SkillCategoryTranslation value = values.stream().filter(item -> item.getLanguageCode() == language).findFirst().orElseThrow(() -> new NoSuchElementException("Skill category translation not found")); return new AdminSkillTranslationRequest(value.getName(), value.getDescription()); }
    private static void version(SkillCategory value, Long version) { if (version == null || value.getVersion() != version) throw new ObjectOptimisticLockingFailureException(SkillCategory.class, value.getId()); }
    private void record(String action, UUID id) { audit.save(AuditEvent.record(UUID.randomUUID(), Instant.now(), actor.required(), action, "SKILL_CATEGORY", id, "SUCCESS", null, null, mapper.createObjectNode().put("changedFields", "managed"))); }
}
