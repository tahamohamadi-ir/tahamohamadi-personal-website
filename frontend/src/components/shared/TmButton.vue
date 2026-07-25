<script setup>
import { computed } from 'vue'

defineOptions({
  name: 'TmButton'
})

const VARIANTS = [
  'primary',
  'secondary',
  'quiet',
  'text',
  'destructive'
]

const SIZES = ['sm', 'md', 'lg']
const TYPES = ['button', 'submit', 'reset']

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => [
      'primary',
      'secondary',
      'quiet',
      'text',
      'destructive'
    ].includes(value)
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => [
      'sm',
      'md',
      'lg'
    ].includes(value)
  },
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  to: {
    type: [String, Object],
    default: undefined
  },
  href: {
    type: String,
    default: undefined
  },
  target: {
    type: String,
    default: undefined
  },
  type: {
    type: String,
    default: 'button',
    validator: (value) => [
      'button',
      'submit',
      'reset'
    ].includes(value)
  }
})

const emit = defineEmits(['click'])

const inactive = computed(() => props.disabled || props.loading)

const filled = computed(() => (
  props.variant === 'primary' ||
  props.variant === 'destructive'
))

const navigationProps = computed(() => {
  const values = {}

  if (props.to !== undefined && props.to !== null) {
    values.to = props.to
  } else if (props.href) {
    values.href = props.href
  }

  if (props.target) {
    values.target = props.target
  }

  if (props.href && props.target === '_blank') {
    values.rel = 'noopener noreferrer'
  }

  return values
})

function handleClick (event) {
  if (inactive.value) {
    event?.preventDefault()
    event?.stopImmediatePropagation?.()
    return
  }

  emit('click', event)
}
</script>

<template>
  <q-btn
    v-bind="navigationProps"
    :class="[
      'tm-button',
      `tm-button--${variant}`,
      `tm-button--${size}`,
      {
        'tm-button--loading': loading,
        'tm-button--disabled': disabled
      }
    ]"
    :type="type"
    :loading="loading"
    :disable="inactive"
    :unelevated="filled"
    :flat="!filled"
    :aria-busy="loading ? 'true' : undefined"
    :aria-disabled="inactive ? 'true' : undefined"
    no-caps
    @click="handleClick"
  >
    <slot />
  </q-btn>
</template>

<style scoped lang="scss">
.tm-button {
  --tm-button-background: transparent;
  --tm-button-background-hover: transparent;
  --tm-button-border: transparent;
  --tm-button-border-hover: transparent;
  --tm-button-text: var(--tm-text-primary);
  --tm-button-text-hover: var(--tm-text-primary);

  min-inline-size: var(--tm-control-min-size);
  min-block-size: var(--tm-control-min-size);
  border: 1px solid var(--tm-button-border);
  border-radius: var(--tm-radius-control);
  background: var(--tm-button-background);
  background-image: none;
  color: var(--tm-button-text);
  box-shadow: none;
  font: inherit;
  font-weight: 700;
  letter-spacing: 0;
  line-height: 1.2;
  text-transform: none;
  transition:
    background-color var(--tm-motion-state) ease,
    border-color var(--tm-motion-state) ease,
    color var(--tm-motion-state) ease;
  touch-action: manipulation;
}

.tm-button:not(.disabled):not([aria-disabled='true']):hover {
  border-color: var(--tm-button-border-hover);
  background: var(--tm-button-background-hover);
  color: var(--tm-button-text-hover);
}

.tm-button--primary {
  --tm-button-background: var(--tm-action-primary);
  --tm-button-background-hover: var(--tm-action-primary-hover);
  --tm-button-border: var(--tm-action-primary);
  --tm-button-border-hover: var(--tm-action-primary-hover);
  --tm-button-text: var(--tm-surface);
  --tm-button-text-hover: var(--tm-surface);
}

.tm-button--secondary {
  --tm-button-background-hover: var(--tm-bg-surface-subtle);
  --tm-button-border: var(--tm-action-secondary);
  --tm-button-border-hover: var(--tm-action-secondary);
  --tm-button-text: var(--tm-action-secondary);
  --tm-button-text-hover: var(--tm-action-secondary);
}

.tm-button--quiet {
  --tm-button-background: var(--tm-bg-surface-subtle);
  --tm-button-background-hover: var(--tm-bg-accent-soft);
  --tm-button-text: var(--tm-text-primary);
  --tm-button-text-hover: var(--tm-text-primary);
}

.tm-button--text {
  --tm-button-background-hover: var(--tm-bg-surface-subtle);
  --tm-button-text: var(--tm-link);
  --tm-button-text-hover: var(--tm-link-hover);
}

.tm-button--destructive {
  --tm-button-background: var(--tm-danger);
  --tm-button-background-hover:
    color-mix(in srgb, var(--tm-danger) 88%, black);
  --tm-button-border: var(--tm-danger);
  --tm-button-border-hover:
    color-mix(in srgb, var(--tm-danger) 88%, black);
  --tm-button-text: var(--tm-surface);
  --tm-button-text-hover: var(--tm-surface);
}

.tm-button--sm {
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-3);
  font-size: 0.875rem;
}

.tm-button--md {
  min-block-size: var(--tm-control-min-size);
  padding-inline: var(--tm-space-4);
  font-size: 0.9375rem;
}

.tm-button--lg {
  min-block-size: 52px;
  padding-inline: var(--tm-space-5, 20px);
  font-size: 1rem;
}

.tm-button--loading {
  cursor: progress;
}

.tm-button--disabled,
.tm-button.disabled {
  opacity: 0.58;
}
</style>
