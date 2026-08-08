<script setup>
import { useI18n } from 'vue-i18n'

defineProps({
  title: { type: String, required: true },
  description: { type: String, default: null },
  backPath: { type: String, default: null },
  status: { type: String, default: null },
  isDirty: Boolean,
  saving: Boolean,
  previewPath: { type: String, default: null }
})

const emit = defineEmits(['save', 'discard'])
const { t } = useI18n()
</script>

<template>
  <div class="admin-editor-shell">
    <!-- Top Sticky Shell Bar -->
    <header class="admin-editor-shell__bar">
      <div class="admin-editor-shell__left">
        <router-link v-if="backPath" :to="backPath" class="admin-editor-shell__back">
          <q-icon name="arrow_back" size="20px" />
        </router-link>
        <div>
          <div class="admin-editor-shell__title-row">
            <h1 class="admin-editor-shell__title">{{ title }}</h1>
            <q-chip
              v-if="status"
              dense
              size="12px"
              :color="status === 'PUBLISHED' ? 'positive' : 'warning'"
              text-color="white"
              class="q-ml-sm"
            >
              {{ status }}
            </q-chip>
            <q-chip
              v-if="isDirty"
              dense
              size="12px"
              color="orange-9"
              text-color="white"
              class="q-ml-xs"
            >
              {{ t('admin.siteSettings.unsaved') }}
            </q-chip>
          </div>
          <p v-if="description" class="admin-editor-shell__description">{{ description }}</p>
        </div>
      </div>

      <div class="admin-editor-shell__right">
        <slot name="header-actions" />
        <a
          v-if="previewPath"
          :href="previewPath"
          target="_blank"
          rel="noopener noreferrer"
          class="admin-editor-shell__preview-btn"
        >
          <q-icon name="open_in_new" size="18px" />
          <span>{{ t('admin.actions.preview') }}</span>
        </a>
        <q-btn
          color="primary"
          unelevated
          no-caps
          icon="save"
          :label="t('admin.siteSettings.save')"
          :loading="saving"
          @click="emit('save')"
        />
      </div>
    </header>

    <!-- Main Content Container -->
    <main class="admin-editor-shell__content">
      <slot />
    </main>

    <!-- Bottom Sticky Save Bar -->
    <footer class="admin-editor-shell__footer">
      <div class="admin-editor-shell__footer-status">
        <span v-if="isDirty" class="text-warning text-weight-medium">
          ● {{ t('admin.siteSettings.unsaved') }}
        </span>
        <span v-else class="text-positive text-weight-medium">
          ✓ {{ t('admin.siteSettings.saved') }}
        </span>
      </div>
      <div class="admin-editor-shell__footer-actions">
        <q-btn
          v-if="isDirty"
          flat
          no-caps
          color="grey-8"
          :label="t('admin.unsaved.cancel')"
          @click="emit('discard')"
        />
        <q-btn
          color="primary"
          unelevated
          no-caps
          icon="save"
          :label="t('admin.siteSettings.save')"
          :loading="saving"
          @click="emit('save')"
        />
      </div>
    </footer>
  </div>
</template>

<style scoped>
.admin-editor-shell {
  display: flex;
  flex-direction: column;
  gap: var(--tm-space-4, 16px);
  margin-inline: auto;
  max-inline-size: 1080px;
  padding-bottom: 80px;
  width: 100%;
}
.admin-editor-shell__bar {
  align-items: center;
  background: var(--tm-admin-surface, #ffffff);
  border: 1px solid var(--tm-admin-border, #cbd5e1);
  border-radius: 10px;
  display: flex;
  gap: 16px;
  justify-content: space-between;
  padding: 12px 20px;
  position: sticky;
  top: 12px;
  z-index: 100;
}
.admin-editor-shell__left {
  align-items: center;
  display: flex;
  gap: 12px;
}
.admin-editor-shell__back {
  align-items: center;
  border-radius: 50%;
  color: var(--tm-text-secondary, #64748b);
  display: flex;
  height: 36px;
  justify-content: center;
  text-decoration: none;
  transition: background 0.2s ease;
  width: 36px;
}
.admin-editor-shell__back:hover {
  background: var(--tm-surface-subtle, #f1f5f9);
  color: var(--tm-text-primary);
}
.admin-editor-shell__title-row {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
}
.admin-editor-shell__title {
  font-size: 1.25rem;
  font-weight: 700;
  line-height: 1.3;
  margin: 0;
}
.admin-editor-shell__description {
  color: var(--tm-text-secondary, #64748b);
  font-size: 0.85rem;
  margin: 2px 0 0;
}
.admin-editor-shell__right {
  align-items: center;
  display: flex;
  gap: 12px;
}
.admin-editor-shell__preview-btn {
  align-items: center;
  border: 1px solid var(--tm-admin-border, #cbd5e1);
  border-radius: 6px;
  color: var(--tm-text-primary);
  display: inline-flex;
  font-size: 13px;
  font-weight: 500;
  gap: 6px;
  padding: 6px 12px;
  text-decoration: none;
  transition: all 0.2s ease;
}
.admin-editor-shell__preview-btn:hover {
  border-color: var(--q-primary);
  color: var(--q-primary);
}
.admin-editor-shell__content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.admin-editor-shell__footer {
  align-items: center;
  background: var(--tm-admin-surface, #ffffff);
  border: 1px solid var(--tm-admin-border, #cbd5e1);
  border-radius: 10px;
  bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  left: 50%;
  max-width: 1040px;
  padding: 10px 20px;
  position: fixed;
  transform: translateX(-50%);
  width: calc(100% - 32px);
  z-index: 100;
}
.admin-editor-shell__footer-actions {
  display: flex;
  gap: 8px;
}
</style>
