<script setup>
import { computed, ref } from 'vue'

import { canUsePublicPreview } from 'src/composables/adminContentInteractions'

const props = defineProps({
  active: Boolean,
  saving: Boolean,
  publicPreviewPath: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['activate', 'deactivate'])
const confirmDeactivate = ref(false)
const previewIsAvailable = computed(() => canUsePublicPreview(props.publicPreviewPath))

function deactivate() {
  confirmDeactivate.value = false
  emit('deactivate')
}
</script>

<template>
  <section class="admin-activation-actions" aria-label="Activation controls">
    <a
      v-if="previewIsAvailable"
      :href="publicPreviewPath"
      target="_blank"
      rel="noopener noreferrer"
      class="text-primary"
    >
      Preview public page
    </a>
    <q-btn
      v-if="!active"
      type="button"
      outline
      color="positive"
      label="Activate"
      :loading="saving"
      :disable="saving"
      @click="emit('activate')"
    />
    <q-btn
      v-else
      type="button"
      outline
      color="negative"
      label="Deactivate"
      :loading="saving"
      :disable="saving"
      @click="confirmDeactivate = true"
    />
    <q-dialog v-model="confirmDeactivate">
      <q-card>
        <q-card-section class="text-h6">Deactivate this item?</q-card-section>
        <q-card-section>This removes it from its public placement.</q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" @click="confirmDeactivate = false" />
          <q-btn color="negative" label="Deactivate" :loading="saving" @click="deactivate" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </section>
</template>
