import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import { mount } from '@vue/test-utils'
import { Quasar } from 'quasar'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'

const workingDirectory = process.cwd()
const projectRoot = (
  workingDirectory.split(/[\\/]/).pop().toLowerCase() === 'frontend'
)
  ? resolve(workingDirectory, '..')
  : workingDirectory

const COMPONENT_PATH = 'frontend/src/components/shared/TmButton.vue'

async function loadTmButton () {
  const filePath = resolve(projectRoot, COMPONENT_PATH)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${COMPONENT_PATH}`)
  }

  const componentModule = await vi.importActual(filePath)

  return componentModule.default
}

async function mountButton ({
  props = {},
  slot = 'Continue'
} = {}) {
  const TmButton = await loadTmButton()
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/:pathMatch(.*)*',
        component: { template: '<div />' }
      }
    ]
  })

  await router.push('/en')
  await router.isReady()

  return mount(TmButton, {
    props,
    slots: {
      default: slot
    },
    global: {
      plugins: [Quasar, router]
    }
  })
}

describe('TmButton', () => {
  it('uses primary and medium as governed defaults', async () => {
    const wrapper = await mountButton()
    const button = wrapper.get('button')

    expect(wrapper.classes()).toContain('tm-button')
    expect(wrapper.classes()).toContain('tm-button--primary')
    expect(wrapper.classes()).toContain('tm-button--md')
    expect(button.attributes('type')).toBe('button')
  })

  it('supports every governed variant and size without ad hoc classes', async () => {
    const variants = [
      'primary',
      'secondary',
      'quiet',
      'text',
      'destructive'
    ]
    const sizes = ['sm', 'md', 'lg']

    for (const variant of variants) {
      const wrapper = await mountButton({
        props: { variant }
      })

      expect(wrapper.classes()).toContain(`tm-button--${variant}`)
    }

    for (const size of sizes) {
      const wrapper = await mountButton({
        props: { size }
      })

      expect(wrapper.classes()).toContain(`tm-button--${size}`)
    }
  })

  it('renders a Vue Router destination from the to prop', async () => {
    const wrapper = await mountButton({
      props: {
        to: { path: '/en/work' }
      },
      slot: 'View work'
    })
    const link = wrapper.get('a')

    expect(link.attributes('href')).toBe('/en/work')
    expect(link.text()).toContain('View work')
  })

  it('renders secure external-link attributes', async () => {
    const wrapper = await mountButton({
      props: {
        href: 'https://example.com/research',
        target: '_blank'
      },
      slot: 'External research'
    })
    const link = wrapper.get('a')

    expect(link.attributes('href')).toBe(
      'https://example.com/research'
    )
    expect(link.attributes('target')).toBe('_blank')
    expect(link.attributes('rel')).toBe('noopener noreferrer')
  })

  it('exposes a width-preserving loading state and busy semantics', async () => {
    const wrapper = await mountButton({
      props: {
        loading: true
      },
      slot: 'Save changes'
    })

    expect(wrapper.classes()).toContain('tm-button--loading')
    expect(wrapper.attributes('aria-busy')).toBe('true')
    expect(wrapper.attributes('aria-disabled')).toBe('true')
    expect(wrapper.text()).toContain('Save changes')
  })

  it('maps disabled state to native and accessible semantics', async () => {
    const wrapper = await mountButton({
      props: {
        disabled: true
      }
    })
    const button = wrapper.get('button')

    expect(button.attributes()).toHaveProperty('disabled')
    expect(wrapper.attributes('aria-disabled')).toBe('true')
    expect(wrapper.classes()).toContain('tm-button--disabled')
  })

  it('derives its accessible name from visible slot content', async () => {
    const wrapper = await mountButton({
      slot: 'Read the case study'
    })
    const button = wrapper.get('button')

    expect(button.text()).toContain('Read the case study')
    expect(button.text().trim()).not.toHaveLength(0)
  })

  it('forbids pill geometry and gradient styling', async () => {
    const source = readFileSync(
      resolve(projectRoot, COMPONENT_PATH),
      'utf8'
    )
    const wrapper = await mountButton()

    expect(wrapper.classes()).not.toContain('rounded')
    expect(wrapper.classes()).not.toContain('round')
    expect(source).not.toMatch(/linear-gradient|radial-gradient/)
    expect(source).not.toMatch(/\brounded\b|\bround\b/)
    expect(source).toContain('background-image: none')
  })

  it('emits click only while actionable', async () => {
    const actionable = await mountButton()

    await actionable.trigger('click')

    expect(actionable.emitted('click')).toHaveLength(1)

    for (const props of [
      { disabled: true },
      { loading: true }
    ]) {
      const inactive = await mountButton({ props })

      await inactive.trigger('click')

      expect(inactive.emitted('click')).toBeUndefined()
    }
  })
})
