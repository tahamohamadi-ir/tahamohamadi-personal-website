<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import {
  canUsePublicPreview,
  supportedLifecycleActions
} from 'src/composables/adminContentInteractions'

const props = defineProps({
  status: { type: String, required: true },
  saving: Boolean,
  publicPreviewPath: { type: String, default: null }
})

const emit = defineEmits(['publish', 'archive'])
const { t } = useI18n()
const pendingAction = ref(null)
const confirmationDialog = ref(null)
const allowedActions = computed(() => supportedLifecycleActions(props.status))
const previewIsAvailable = computed(() => canUsePublicPreview(props.publicPreviewPath))

function label(action) { return t(`admin.actions.${action}`) }
function requestAction(action) { pendingAction.value = action }
function closeConfirmation() { pendingAction.value = null }
function confirm() {
  const action = pendingAction.value
  closeConfirmation()
  emit(action)
}

function trapDialogFocus(event) {
  if (event.key !== 'Tab') return
  const controls = [...(confirmationDialog.value?.querySelectorAll('button:not([disabled])') ?? [])]
  if (controls.length < 2) return
  const first = controls[0]
  const last = controls.at(-1)
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault()
    last.focus()
  }
  else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault()
    first.focus()
  }
}

watch(pendingAction, async (action) => {
  if (!action) return
  await nextTick()
  confirmationDialog.value?.querySelector('[data-cancel]')?.focus()
})
</script>

<template>
  <section class="admin-lifecycle-actions" :aria-label="t('admin.actions.lifecycle')">
    <span class="admin-lifecycle-actions__status">{{ status }}</span>
    <a v-if="previewIsAvailable" :href="publicPreviewPath" target="_blank" rel="noopener noreferrer" class="text-primary">
      {{ t('admin.actions.preview') }}
    </a>
    <button
      v-for="action in allowedActions"
      :key="action"
      :data-action="action"
      type="button"
      :disabled="saving"
      @click="requestAction(action)"
    >{{ saving ? t('admin.actions.saving') : label(action) }}</button>
    <div v-if="pendingAction" ref="confirmationDialog" class="admin-confirmation" role="dialog" aria-modal="true" aria-labelledby="admin-lifecycle-confirmation" tabindex="-1" @keydown.esc.stop="closeConfirmation" @keydown="trapDialogFocus">
      <p id="admin-lifecycle-confirmation">{{ t('admin.actions.confirm', { action: label(pendingAction).toLowerCase() }) }}</p>
      <button data-cancel type="button" @click="closeConfirmation">{{ t('admin.actions.cancel') }}</button>
      <button data-confirm type="button" @click="confirm">{{ label(pendingAction) }}</button>
    </div>
  </section>
</template>

<style scoped>
.admin-lifecycle-actions { align-items: center; display: flex; flex-wrap: wrap; gap: var(--tm-space-3); }
.admin-lifecycle-actions__status { background: var(--tm-action-primary); border-radius: var(--tm-radius-control); color: var(--tm-white); font-size: .75rem; font-weight: 700; padding: var(--tm-space-1) var(--tm-space-2); }
button { background: transparent; border: 1px solid var(--tm-admin-border); border-radius: var(--tm-admin-control-radius); color: var(--tm-text-primary); cursor: pointer; font: inherit; min-block-size: var(--tm-control-min-size); padding-inline: var(--tm-space-4); }
button:not(:disabled):hover { border-color: var(--tm-action-primary); color: var(--tm-action-primary); }
button:focus-visible { outline: 3px solid var(--tm-focus-ring); outline-offset: 2px; }
button:disabled { cursor: not-allowed; opacity: .65; }
.admin-confirmation { align-items: center; border: 1px solid var(--tm-action-primary); border-radius: var(--tm-radius-dialog); display: flex; flex-basis: 100%; flex-wrap: wrap; gap: var(--tm-space-3); padding: var(--tm-space-4); }
.admin-confirmation p { flex-basis: 100%; margin: 0; }
</style>
