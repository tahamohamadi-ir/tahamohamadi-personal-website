import { computed } from 'vue'

import { useAsyncPage } from './useAsyncPage'

const COLLECTION_SOURCES = new Set(['BLOG', 'PORTFOLIO', 'PUBLICATIONS'])

function normalizedType(block) {
  return typeof block?.type === 'string'
    ? block.type.toLowerCase().replaceAll('_', '-')
    : ''
}

function collectionSources(blocks) {
  return new Set(
    blocks
      .filter((block) => normalizedType(block) === 'collection')
      .map((block) => block?.settings?.source)
      .filter((source) => COLLECTION_SOURCES.has(source))
  )
}

async function optional(load, fallback) {
  try {
    return await load()
  }
  catch {
    return fallback
  }
}

/**
 * Fetches only the published, route-owned data required by approved page
 * blocks. The page itself remains authoritative: a temporary collection
 * failure must not hide a published About, Research, or custom page.
 */
export function useComposedPageData({ api, locale, slug, initialData, ssrKey }) {
  const pageData = useAsyncPage({
    api,
    load: async (currentApi) => {
      const page = await currentApi.getPage(locale.value, slug.value)
      const blocks = Array.isArray(page?.blocks) ? page.blocks : []
      const sources = collectionSources(blocks)
      const needsSkills = blocks.some((block) => normalizedType(block) === 'skills')
      const needsSocialLinks = blocks.some((block) => normalizedType(block) === 'social-links')

      const [posts, projects, publications, skills, socialLinks] = await Promise.all([
        sources.has('BLOG')
          ? optional(() => currentApi.listPosts(locale.value, { page: 0, size: 12 }), { items: [] })
          : Promise.resolve({ items: [] }),
        sources.has('PORTFOLIO')
          ? optional(() => currentApi.listPortfolio(locale.value, { page: 0, size: 12 }), { items: [] })
          : Promise.resolve({ items: [] }),
        sources.has('PUBLICATIONS')
          ? optional(() => currentApi.listPublications(locale.value, { page: 0, size: 12 }), { items: [] })
          : Promise.resolve({ items: [] }),
        needsSkills
          ? optional(() => currentApi.getSkills(locale.value), { items: [] })
          : Promise.resolve({ items: [] }),
        needsSocialLinks
          ? optional(() => currentApi.getSocialLinks(), { items: [] })
          : Promise.resolve({ items: [] })
      ])

      return {
        page,
        collectionItems: {
          BLOG: posts.items ?? [],
          PORTFOLIO: projects.items ?? [],
          PUBLICATIONS: publications.items ?? []
        },
        skills: skills.items ?? [],
        socialLinks: socialLinks.items ?? []
      }
    },
    isEmpty: (value) => {
      const page = value?.page
      const hasText = [page?.summary, page?.bodyMarkdown]
        .some((field) => typeof field === 'string' && field.trim().length > 0)
      return !hasText && !(Array.isArray(page?.blocks) && page.blocks.length > 0)
    },
    initialData: initialData === undefined ? undefined : {
      page: initialData,
      collectionItems: {},
      skills: [],
      socialLinks: []
    },
    ssrKey
  })

  return {
    ...pageData,
    page: computed(() => pageData.data.value?.page ?? null),
    collectionItems: computed(() => pageData.data.value?.collectionItems ?? {}),
    skills: computed(() => pageData.data.value?.skills ?? []),
    socialLinks: computed(() => pageData.data.value?.socialLinks ?? [])
  }
}
