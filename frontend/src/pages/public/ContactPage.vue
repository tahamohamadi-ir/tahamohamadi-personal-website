<script setup>
import { computed, inject, nextTick, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { HTTP_CLIENT_KEY, PUBLIC_API_KEY } from 'src/services/apiContext'
import { primeCsrfToken } from 'src/services/csrf'

defineOptions({ name: 'ContactPage' })

const api = inject(PUBLIC_API_KEY)
const httpClient = inject(HTTP_CLIENT_KEY)
const route = useRoute()
const { t } = useI18n()
const locale = computed(() => route.meta.locale)
const form = reactive({ name: '', email: '', message: '' })
const errors = reactive({ name: '', email: '', message: '' })
const nameInput = ref(null)
const emailInput = ref(null)
const messageInput = ref(null)
const fields = {
  name: nameInput,
  email: emailInput,
  message: messageInput
}
const pending = ref(false)
const submitted = ref(false)
const failed = ref(false)

const FIELD_LIMITS = {
  name: 200,
  email: 320,
  message: 10000
}

function clearErrors() {
  for (const fieldName of Object.keys(errors)) {
    errors[fieldName] = ''
  }
}

function validationMessage(fieldName) {
  return t(`contact.validation.${fieldName}`)
}

function validate() {
  clearErrors()

  for (const [fieldName, limit] of Object.entries(FIELD_LIMITS)) {
    if (form[fieldName].trim().length === 0) {
      errors[fieldName] = validationMessage(`${fieldName}Required`)
    }
    else if (form[fieldName].length > limit) {
      errors[fieldName] = validationMessage(`${fieldName}TooLong`)
    }
  }

  return Object.values(errors).every((message) => message.length === 0)
}

async function focusFirstInvalidField() {
  await nextTick()

  for (const fieldName of Object.keys(errors)) {
    if (errors[fieldName]) {
      fields[fieldName].value?.focus()
      return
    }
  }
}

function applyServerValidationErrors(error) {
  const serverFields = error?.response?.data?.fields

  if (!Array.isArray(serverFields)) {
    return false
  }

  clearErrors()
  let hasKnownError = false

  for (const field of serverFields) {
    if (Object.hasOwn(errors, field?.field)) {
      errors[field.field] = validationMessage(`${field.field}Invalid`)
      hasKnownError = true
    }
  }

  return hasKnownError
}

function resetForm() {
  form.name = ''
  form.email = ''
  form.message = ''
  clearErrors()
}

async function submit() {
  if (pending.value) {
    return
  }

  submitted.value = false
  failed.value = false

  if (!validate()) {
    await focusFirstInvalidField()
    return
  }

  pending.value = true

  try {
    await primeCsrfToken(httpClient)
    await api.submitContact({
      name: form.name,
      email: form.email,
      message: form.message,
      language: locale.value
    })
    resetForm()
    submitted.value = true
  }
  catch (error) {
    failed.value = true

    if (applyServerValidationErrors(error)) {
      await focusFirstInvalidField()
    }
  }
  finally {
    pending.value = false
  }
}
</script>

<template>
  <section class="contact-page tm-container" aria-labelledby="contact-title">
    <header class="contact-page__header">
      <h1 id="contact-title" class="tm-page-title">
        {{ t('shell.navigation.contact') }}
      </h1>
      <p class="tm-page-copy">{{ t('contact.introduction') }}</p>
    </header>

    <p v-if="submitted" class="contact-page__status" role="status" aria-live="polite">
      {{ t('contact.success') }}
    </p>
    <p v-else-if="failed" class="contact-page__status" role="alert">
      {{ t('contact.failure') }}
    </p>
    <p v-if="pending" class="contact-page__status" role="status" aria-live="polite">
      {{ t('contact.pending') }}
    </p>

    <form class="contact-page__form" novalidate @submit.prevent="submit">
      <div v-if="Object.values(errors).some(Boolean)" class="contact-page__summary" role="alert">
        {{ t('contact.validation.summary') }}
      </div>

      <div class="contact-page__field">
        <label for="contact-name">{{ t('contact.fields.name') }}</label>
        <input
          id="contact-name"
          ref="nameInput"
          v-model="form.name"
          class="contact-page__control"
          name="name"
          type="text"
          autocomplete="name"
          :maxlength="FIELD_LIMITS.name"
          :aria-invalid="Boolean(errors.name)"
          :aria-describedby="errors.name ? 'contact-name-error' : undefined"
          :disabled="pending"
        >
        <p v-if="errors.name" id="contact-name-error" class="contact-page__error">
          {{ errors.name }}
        </p>
      </div>

      <div class="contact-page__field">
        <label for="contact-email">{{ t('contact.fields.email') }}</label>
        <input
          id="contact-email"
          ref="emailInput"
          v-model="form.email"
          class="contact-page__control"
          name="email"
          type="email"
          autocomplete="email"
          :maxlength="FIELD_LIMITS.email"
          :aria-invalid="Boolean(errors.email)"
          :aria-describedby="errors.email ? 'contact-email-error' : undefined"
          :disabled="pending"
        >
        <p v-if="errors.email" id="contact-email-error" class="contact-page__error">
          {{ errors.email }}
        </p>
      </div>

      <div class="contact-page__field">
        <label for="contact-message">{{ t('contact.fields.message') }}</label>
        <textarea
          id="contact-message"
          ref="messageInput"
          v-model="form.message"
          class="contact-page__control contact-page__message"
          name="message"
          rows="6"
          :maxlength="FIELD_LIMITS.message"
          :aria-invalid="Boolean(errors.message)"
          :aria-describedby="errors.message ? 'contact-message-error' : undefined"
          :disabled="pending"
        />
        <p v-if="errors.message" id="contact-message-error" class="contact-page__error">
          {{ errors.message }}
        </p>
      </div>

      <button class="contact-page__submit tm-interactive" type="submit" :disabled="pending">
        {{ pending ? t('contact.pending') : t('contact.submit') }}
      </button>
    </form>
  </section>
</template>

<style scoped lang="scss">
.contact-page,
.contact-page__form,
.contact-page__header,
.contact-page__field {
  display: grid;
}

.contact-page {
  gap: var(--tm-space-8);
  padding-block: var(--tm-content-block-start) var(--tm-content-block-end);
}

.contact-page__header,
.contact-page__form {
  gap: var(--tm-space-4);
  max-inline-size: var(--tm-prose-max-width);
}

.contact-page__field {
  gap: var(--tm-space-2);
}

.contact-page__control {
  inline-size: 100%;
  min-block-size: var(--tm-control-min-size);
  padding: var(--tm-space-3);
  border: 1px solid var(--tm-border-subtle);
  border-radius: var(--tm-radius-control);
  background: var(--tm-surface);
  color: var(--tm-text-primary);
  font: inherit;
}

.contact-page__message {
  resize: vertical;
}

.contact-page__submit {
  justify-self: start;
  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-4);
  border: 1px solid var(--tm-action-primary);
  border-radius: var(--tm-radius-control);
  background: var(--tm-action-primary);
  color: var(--tm-surface);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.contact-page__submit:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.contact-page__status,
.contact-page__summary,
.contact-page__error {
  margin: 0;
}

.contact-page__status,
.contact-page__summary {
  max-inline-size: var(--tm-prose-max-width);
  padding: var(--tm-space-3);
  border: 1px solid var(--tm-border-subtle);
}

.contact-page__error {
  color: var(--tm-danger);
}
</style>
