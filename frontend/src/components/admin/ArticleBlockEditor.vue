<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import AdminMarkdownPreview from 'src/components/admin/AdminMarkdownPreview.vue'
import AdminMediaSelector from 'src/components/admin/AdminMediaSelector.vue'
import { documentToMarkdown, markdownToDocument, readingTimeMinutes } from 'src/services/articleDocument'

const props = defineProps({ modelValue: { type: String, default: '' }, document: { type: Object, default: null }, mediaIds: { type: Array, default: () => [] }, disable: Boolean })
const emit = defineEmits(['update:modelValue', 'update:document'])
const { t } = useI18n()
const blocks = ref(props.document?.blocks ?? markdownToDocument(props.modelValue).blocks)
const preview = ref(false)
const mediaOptions = computed(() => props.mediaIds.map((id) => ({ label: id, value: id })))

function sync() { emit('update:modelValue', documentToMarkdown({ blocks: blocks.value })); emit('update:document', { version: 1, blocks: blocks.value }) }
function add(type) {
  const values = {
    paragraph: { type: 'paragraph', value: '' }, heading: { type: 'heading', level: 2, value: '' },
    quote: { type: 'quote', value: '' }, code: { type: 'code', language: '', value: '' },
    image: { type: 'image', mediaId: null, alt: '', caption: '' }, divider: { type: 'divider' }, markdown: { type: 'markdown', value: '' }
  }
  blocks.value.push(values[type]); sync()
}
function remove(index) { blocks.value.splice(index, 1); sync() }
function move(index, direction) {
  const destination = index + direction
  if (destination < 0 || destination >= blocks.value.length) return
  const [block] = blocks.value.splice(index, 1)
  blocks.value.splice(destination, 0, block)
  sync()
}
function onKeydown(event, index) {
  if (!event.altKey || !event.ctrlKey) return
  if (event.key === 'ArrowUp') { event.preventDefault(); move(index, -1) }
  if (event.key === 'ArrowDown') { event.preventDefault(); move(index, 1) }
}

watch(() => props.modelValue, (value) => {
  if (!props.document && value !== documentToMarkdown({ blocks: blocks.value })) blocks.value = markdownToDocument(value).blocks
})
watch(() => props.document, (value) => { if (value?.blocks) blocks.value = value.blocks }, { deep: true })
</script>

<template>
  <section class="article-block-editor" :aria-label="t('admin.articleEditor.label')">
    <div class="article-block-editor__toolbar">
      <q-btn v-for="type in ['paragraph', 'heading', 'quote', 'code', 'image', 'divider', 'markdown']" :key="type" outline no-caps :disable="disable" :label="t(`admin.articleEditor.add.${type}`)" @click="add(type)" />
      <q-btn outline no-caps :label="preview ? t('admin.articleEditor.edit') : t('admin.articleEditor.preview')" @click="preview = !preview" />
      <span class="text-caption" aria-live="polite">{{ t('admin.articleEditor.readingTime', { minutes: readingTimeMinutes(modelValue) }) }}</span>
    </div>
    <section v-if="preview" class="article-block-editor__preview" :aria-label="t('admin.articleEditor.preview')">
      <MarkdownContent :markdown="modelValue" />
    </section>
    <ol v-else class="article-block-editor__blocks">
      <li v-for="(block, index) in blocks" :key="`${block.type}-${index}`" class="article-block-editor__block" @keydown="onKeydown($event, index)">
        <div class="article-block-editor__block-actions">
          <span class="text-caption">{{ t(`admin.articleEditor.types.${block.type}`) }}</span>
          <q-btn flat icon="keyboard_arrow_up" :aria-label="t('admin.articleEditor.moveUp', { index: index + 1 })" :disable="disable || index === 0" @click="move(index, -1)" />
          <q-btn flat icon="keyboard_arrow_down" :aria-label="t('admin.articleEditor.moveDown', { index: index + 1 })" :disable="disable || index + 1 === blocks.length" @click="move(index, 1)" />
          <q-btn flat color="negative" icon="delete" :aria-label="t('admin.articleEditor.remove', { index: index + 1 })" :disable="disable" @click="remove(index)" />
        </div>
        <q-input v-if="['paragraph', 'quote', 'markdown'].includes(block.type)" v-model="block.value" type="textarea" autogrow :disable="disable" :label="t(`admin.articleEditor.types.${block.type}`)" @update:model-value="sync" />
        <template v-else-if="block.type === 'heading'">
          <q-select v-model="block.level" :options="[1, 2, 3, 4, 5, 6]" :label="t('admin.articleEditor.headingLevel')" :disable="disable" @update:model-value="sync" />
          <q-input v-model="block.value" :label="t('admin.articleEditor.types.heading')" :disable="disable" @update:model-value="sync" />
        </template>
        <template v-else-if="block.type === 'code'">
          <q-input v-model="block.language" :label="t('admin.articleEditor.codeLanguage')" :disable="disable" @update:model-value="sync" />
          <q-input v-model="block.value" type="textarea" autogrow :label="t('admin.articleEditor.types.code')" :disable="disable" @update:model-value="sync" />
        </template>
        <template v-else-if="block.type === 'image'">
          <AdminMediaSelector v-model="block.mediaId" :allowed-types="['image']" :label="t('admin.blogPosts.media')" :disable="disable" @update:model-value="sync" />
          <q-input v-model="block.alt" :label="t('admin.articleEditor.imageAlt')" :disable="disable" @update:model-value="sync" />
          <q-input v-model="block.caption" :label="t('admin.articleEditor.imageCaption')" :disable="disable" @update:model-value="sync" />
        </template>
        <p v-else class="text-caption q-mb-none">{{ t('admin.articleEditor.divider') }}</p>
      </li>
    </ol>
  </section>
</template>

<style scoped>
.article-block-editor { display: grid; gap: var(--tm-space-4); }
.article-block-editor__toolbar { align-items: center; display: flex; flex-wrap: wrap; gap: var(--tm-space-2); }
.article-block-editor__blocks { display: grid; gap: var(--tm-space-3); list-style: none; margin: 0; padding: 0; }
.article-block-editor__block { border: 1px solid var(--tm-admin-border); border-radius: var(--tm-admin-panel-radius); display: grid; gap: var(--tm-space-3); padding: var(--tm-space-3); }
.article-block-editor__block-actions { align-items: center; display: flex; flex-wrap: wrap; gap: var(--tm-space-1); }
.article-block-editor__block-actions span { margin-inline-end: auto; }
.article-block-editor__preview { border: 1px solid var(--tm-admin-border); border-radius: var(--tm-admin-panel-radius); padding: var(--tm-space-4); }
</style>
