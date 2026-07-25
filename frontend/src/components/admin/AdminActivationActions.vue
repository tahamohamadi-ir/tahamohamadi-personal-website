<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { canUsePublicPreview } from 'src/composables/adminContentInteractions'

const props = defineProps({
  active: Boolean,
  saving: Boolean,
  publicPreviewPath: { type: String, default: null }
})

const emit = defineEmits(['activate', 'deactivate'])
const { t } = useI18n()
const confirmDeactivate = ref(false)
const previewIsAvailable = computed(() => canUsePublicPreview(props.publicPreviewPath))

function deactivate() {
  confirmDeactivate.value = false
  emit('deactivate')
}
</script>

<template>
  <section class="admin-activation-actions" :aria-label="t('admin.actions.activation')">
    <a v-if="previewIsAvailable" :href="publicPreviewPath" target="_blank" rel="noopener noreferrer" class="text-primary">
      {{ t('admin.actions.preview') }}
    </a>
    <q-btn
      v-if="!active"
      type="button"
      outline
      color="positive"
      :label="t('admin.actions.activate')"
      :loading="saving"
      :disable="saving"
      @click="emit('activate')"
    />
    <q-btn
      v-else
      type="button"
      outline
      color="negative"
      :label="t('admin.actions.deactivate')"
      :loading="saving"
      :disable="saving"
      @click="confirmDeactivate = true"
    />
    <q-dialog v-model="confirmDeactivate">
      <q-card>
        <q-card-section class="text-h6">{{ t('admin.actions.deactivateConfirmTitle') }}</q-card-section>
        <q-card-section>{{ t('admin.actions.deactivateConfirmDescription') }}</q-card-section>
        <q-card-actions align="right">
          <q-btn flat :label="t('admin.actions.cancel')" @click="confirmDeactivate = false" />
          <q-btn color="negative" :label="t('admin.actions.deactivate')" :loading="saving" @click="deactivate" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </section>
</template>
