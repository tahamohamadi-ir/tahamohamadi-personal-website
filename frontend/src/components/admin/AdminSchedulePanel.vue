<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: { type: String, default: '' },
  status: { type: String, required: true },
  disable: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'schedule', 'cancelSchedule'])

const { t } = useI18n()

const internalValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formattedDate = computed(() => {
  if (!internalValue.value) return ''
  const date = new Date(internalValue.value)
  if (isNaN(date.getTime())) return internalValue.value
  const formatter = new Intl.DateTimeFormat(t('admin.schedulePanel.locale'), {
    dateStyle: 'long',
    timeStyle: 'short'
  })
  return formatter.format(date)
})
</script>

<template>
  <section v-if="status === 'DRAFT' || status === 'IN_REVIEW' || status === 'SCHEDULED'" class="admin-schedule q-gutter-sm" :aria-label="t('admin.schedulePanel.schedule')">
    <p v-if="status === 'DRAFT' || status === 'IN_REVIEW'" class="text-caption q-mb-sm">{{ t('admin.schedulePanel.help') }}</p>
    <div v-if="status === 'DRAFT' || status === 'IN_REVIEW'" class="row items-center q-col-gutter-md">
      <div class="col-12 col-sm-6 col-md-4">
        <q-input v-model="internalValue" :label="t('admin.schedulePanel.scheduledFor')" :disable="disable" outlined dense>
          <template v-slot:prepend>
            <q-icon name="event" class="cursor-pointer">
              <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                <q-date v-model="internalValue" mask="YYYY-MM-DDTHH:mm">
                  <div class="row items-center justify-end">
                    <q-btn v-close-popup :label="t('admin.actions.close')" color="primary" flat />
                  </div>
                </q-date>
              </q-popup-proxy>
            </q-icon>
          </template>
          <template v-slot:append>
            <q-icon name="access_time" class="cursor-pointer">
              <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                <q-time v-model="internalValue" mask="YYYY-MM-DDTHH:mm" format24h>
                  <div class="row items-center justify-end">
                    <q-btn v-close-popup :label="t('admin.actions.close')" color="primary" flat />
                  </div>
                </q-time>
              </q-popup-proxy>
            </q-icon>
          </template>
        </q-input>
      </div>
      <div class="col-12 col-sm-auto">
        <q-btn outline no-caps icon="schedule" :disable="disable || !internalValue" :label="t('admin.schedulePanel.schedule')" @click="emit('schedule')" />
      </div>
    </div>
    <div v-else class="row items-center q-gutter-md">
      <p class="text-body2 q-mb-none col-12 col-sm-auto">
        <strong>{{ t('admin.schedulePanel.scheduledFor') }}:</strong> <time :datetime="internalValue">{{ formattedDate }}</time>
      </p>
      <q-btn class="col-auto" outline no-caps icon="event_busy" color="negative" :disable="disable" :label="t('admin.schedulePanel.cancelSchedule')" @click="emit('cancelSchedule')" />
    </div>
  </section>
</template>

<style scoped>
.admin-schedule {
  border-block: 1px solid var(--tm-admin-border);
  padding-block: var(--tm-space-4);
  margin-top: var(--tm-space-4);
}
</style>
