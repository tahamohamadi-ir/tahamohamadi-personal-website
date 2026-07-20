<script setup>
import { computed, ref } from 'vue'

import {
  canUsePublicPreview,
  supportedLifecycleActions
} from 'src/composables/adminContentInteractions'

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  saving: Boolean,
  publicPreviewPath: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['publish', 'archive'])
const pendingAction = ref(null)
const allowedActions = computed(() => supportedLifecycleActions(props.status))
const previewIsAvailable = computed(() => canUsePublicPreview(props.publicPreviewPath))

function label(action) {
  return action === 'publish' ? 'Publish' : 'Archive'
}

function confirm() {
  const action = pendingAction.value
  pendingAction.value = null
  emit(action)
}
</script>

<template>
  <section class="admin-lifecycle-actions" aria-label="Content lifecycle actions">
    <span class="admin-lifecycle-actions__status">{{ status }}</span>
    <a
      v-if="previewIsAvailable"
      :href="publicPreviewPath"
      target="_blank"
      rel="noopener noreferrer"
      class="text-primary"
    >
      Preview public page
    </a>
    <button
      v-for="action in allowedActions"
      :key="action"
      :data-action="action"
      type="button"
      :disabled="saving"
      @click="pendingAction = action"
    >
      {{ saving ? 'Saving…' : label(action) }}
    </button>
    <div v-if="pendingAction" class="admin-confirmation" role="dialog" aria-modal="true">
      <p>Confirm {{ label(pendingAction).toLowerCase() }}?</p>
      <button type="button" @click="pendingAction = null">Cancel</button>
      <button data-confirm type="button" @click="confirm">{{ label(pendingAction) }}</button>
    </div>
  </section>
</template>

<style scoped>
.admin-lifecycle-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: center;
}

.admin-lifecycle-actions__status {
  padding: 0.25rem 0.5rem;
  border-radius: 999px;
  background: var(--q-primary);
  color: white;
  font-size: 0.75rem;
  font-weight: 700;
}

button {
  min-block-size: 2.75rem;
  padding-inline: 1rem;
  border: 1px solid currentColor;
  border-radius: 0.25rem;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.admin-confirmation {
  display: flex;
  flex-basis: 100%;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: center;
  padding: 1rem;
  border: 1px solid var(--q-primary);
  border-radius: 0.5rem;
}

.admin-confirmation p {
  flex-basis: 100%;
  margin: 0;
}
</style>
