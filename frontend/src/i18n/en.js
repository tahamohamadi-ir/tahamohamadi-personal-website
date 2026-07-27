export default {
  language: {
    title: 'Choose your language',
    introduction: 'Select the language for this public profile.',
    fa: 'Persian',
    en: 'English'
  },
  public: {
    temporaryPage: 'Public page',
    tableOfContents: 'On this page',
    caseStudy: { role: 'Role', client: 'Client', team: 'Team', outcome: 'Outcome', gallery: 'Project gallery', viewProject: 'View case study' },
    richContent: {
      renderingFailure: 'This content cannot be displayed safely right now.'
    },
    detail: {
      published: 'Published',
      updated: 'Updated',
      authors: 'Authors',
      venue: 'Venue',
      year: 'Year',
      stage: 'Stage',
      doi: 'DOI',
      externalLink: 'Open publication'
    },
    pageIntroduction: {
      about: {
        pending: 'Biography and background content is being prepared.'
      },
      research: {
        pending: 'Research content is being prepared.'
      },
      skills: {
        pending: 'Structured skills data is not available yet.'
      },
      contact: {
        pending: 'The contact workflow is not available yet.'
      },
      unavailable: 'This page introduction is not available.'
    },
    translationUnavailable: 'Translation unavailable',
    translationUnavailableDescription: 'This content is not available in the requested language.',
    notFound: 'Page not found',
    notFoundDescription: 'The requested localized page does not exist.',
    returnHome: 'Return to the home page'
  },
  pageState: {
    loading: 'Loading content.',
    empty: 'No content is available yet.',
    recoverableFailure: 'Unable to load this content. Please try again.',
    offline: 'You are offline. Check your connection, then try again.',
    stale: 'This content may be outdated. Refresh to check for changes.',
    translationUnavailable: 'Translation unavailable.',
    retry: 'Retry',
    refresh: 'Refresh'
  },
  translationUnavailable: {
    viewAvailableTranslation: 'View available translation',
    returnToLocaleHome: 'Return to the home page'
  },
  resume: {
    current: 'Current',
    fileAction: 'Open resume document',
    entryTypes: {
      EDUCATION: 'Education',
      EXPERIENCE: 'Experience',
      RESEARCH: 'Research',
      AWARD: 'Award',
      CERTIFICATION: 'Certification'
    }
  },
  contact: {
    introduction: 'Use this form to send a message.',
    fields: {
      name: 'Name',
      email: 'Email address',
      message: 'Message'
    },
    submit: 'Send message',
    pending: 'Sending message…',
    success: 'Your message has been received.',
    failure: 'Your message could not be sent. Please try again.',
    validation: {
      summary: 'Please correct the highlighted fields.',
      nameRequired: 'Enter your name.',
      nameTooLong: 'Name must be 200 characters or fewer.',
      nameInvalid: 'Enter a valid name.',
      emailRequired: 'Enter your email address.',
      emailTooLong: 'Email address must be 320 characters or fewer.',
      emailInvalid: 'Enter a valid email address.',
      messageRequired: 'Enter a message.',
      messageTooLong: 'Message must be 10,000 characters or fewer.',
      messageInvalid: 'Enter a valid message.'
    }
  },
  collections: {
    pagination: {
      navigationLabel: 'Collection pagination',
      previous: 'Previous page',
      next: 'Next page',
      position: 'Page {current} of {total}'
    },
    publications: {
      authors: 'Authors',
      venue: 'Venue',
      publishedOn: 'Published',
      year: 'Year',
      stage: 'Stage',
      doi: 'DOI',
      externalLink: 'Open publication'
    }
  },
  shell: {
    skipToContent: 'Skip to main content',
    primaryNavigation: 'Primary navigation',
    openNavigation: 'Open navigation',
    closeNavigation: 'Close navigation',
    switchLanguage: 'Switch language',
    siteName: 'Taha Mohammadi',
    siteDescriptor: 'Human-Centered Systems Builder',
    menu: 'Menu',
    close: 'Close',
    navigation: {
      home: 'Home',
      work: 'Work',
      about: 'About',
      research: 'Research',
      skills: 'Skills',
      resume: 'Resume',
      writing: 'Writing',
      blog: 'Blog',
      portfolio: 'Portfolio',
      publications: 'Publications',
      contact: 'Contact'
    },
    footer: {
      navigationLabel: 'Footer navigation',
      statement: 'I research, design, and build complex systems for people.',
      availability: 'Research collaboration · Selected product work',
      rights: 'Taha Mohammadi. All rights reserved.'
    }
  },
  admin: {
    placeholder: 'Admin page placeholder',
    state: {
      loading: 'Loading content…',
      empty: 'No content has been added yet.',
      error: 'This content could not be loaded.',
      retry: 'Try again'
    },
    pagination: {
      label: 'Admin table pages',
      previous: 'Previous',
      next: 'Next',
      status: 'Page {page} of {total}'
    },
    publications: {
      title: 'Publications', description: 'Manage academic bibliographic records and translations.', create: 'Create publication', edit: 'Edit publication', save: 'Save publication',
      key: 'Publication key', stage: 'Publication stage', doi: 'DOI', externalUrl: 'External URL', date: 'Publication date', year: 'Year', coverMedia: 'Cover media asset ID', sortOrder: 'Sort order',
      translationTitle: 'Title', slug: 'Slug', authors: 'Authors', venue: 'Venue', abstract: 'Abstract', seoTitle: 'SEO title', seoDescription: 'SEO description'
    },
    resume: {
      title: 'Resume', description: 'Manage entries and the supported published document for each locale.', createEntry: 'Create entry', editEntry: 'Edit entry', saveEntry: 'Save entry',
      entryType: 'Entry type', startDate: 'Start date', endDate: 'End date', current: 'Current', sortOrder: 'Sort order', translationTitle: 'Title', organization: 'Organization', location: 'Location', entryDescription: 'Description',
      document: 'Resume document', locale: 'Locale', documentMedia: 'Resume document media', saveDocument: 'Save document'
    },
    socialLinks: {
      title: 'Social links', description: 'Manage the supported platform code, public URL, and display order.', create: 'Create social link', edit: 'Edit social link', save: 'Save social link',
      discard: 'Discard unsaved social-link changes?', conflict: 'This social link changed elsewhere. Reload it before saving.', active: 'Active', inactive: 'Inactive',
      platformCode: 'Platform code', platformCodeRequired: 'Platform code is required.', publicUrl: 'Public URL', urlHint: 'Only HTTP and HTTPS URLs are supported.', invalidUrl: 'Enter an HTTP or HTTPS URL.', sortOrder: 'Sort order',
      saved: 'Social link saved.', activated: 'Social link activated.', deactivated: 'Social link deactivated.'
    },
    portfolio: {
      title: 'Portfolio projects', description: 'Translations stay independent and project media is a supported cover asset.', create: 'Create project', edit: 'Edit project', save: 'Save project',
      discard: 'Discard unsaved portfolio changes?', conflict: 'This project changed elsewhere. Reload it before saving.', key: 'Project key', startDate: 'Start date', endDate: 'End date', projectUrl: 'Project URL', repositoryUrl: 'Repository URL', sortOrder: 'Sort order', coverMedia: 'Cover media', gallery: 'Project gallery', associatedSkills: 'Associated skills',
      translationTitle: 'Title', slug: 'Slug', summary: 'Summary', seoTitle: 'SEO title', seoDescription: 'SEO description'
    },
    skills: {
      title: 'Skills', description: 'Manage categories and skills with independent Persian and English labels.', categories: 'Categories', skills: 'Skills', active: 'Active', inactive: 'Inactive',
      discard: 'Discard unsaved skill changes?', conflict: 'This item changed elsewhere. Reload it before saving.', createCategory: 'Create category', editCategory: 'Edit category', newCategory: 'New category', saveCategory: 'Save category', categoryKey: 'Category key',
      createSkill: 'Create skill', editSkill: 'Edit skill', newSkill: 'New skill', saveSkill: 'Save skill', skillKey: 'Skill key', category: 'Category', sortOrder: 'Sort order', name: 'Name', itemDescription: 'Description', preview: 'Preview public skills',
      deactivate: 'Deactivate', deactivateTitle: 'Deactivate this item?', deactivateDescription: 'This will remove the item from its public placement.'
    },
    unsaved: { discard: 'Discard unsaved changes?', cancel: 'Cancel' },
    localeTabs: { sectionLabel: 'Content translations', tabListLabel: 'Content locale', persian: 'Persian', english: 'English', missing: 'Missing translation', missingStatus: 'Missing translation: {locale}.' },
    login: { productName: 'TahaMohamadi.ir', title: 'Admin sign in', description: 'Use your administrator account to manage presentation content.', email: 'Email address', emailRequired: 'Enter your email address.', password: 'Password', passwordRequired: 'Enter your password.', submit: 'Sign in' },
    dashboard: { title: 'Dashboard', description: 'A source-backed view of your current content operations.', createPage: 'Create page', summary: 'Content summary', pages: 'Pages', posts: 'Blog posts', media: 'Media assets', contactMessages: 'New contact messages', manage: 'Manage', nextActions: 'Next actions', continueEditing: 'Continue editing', managePages: 'Manage pages', managePosts: 'Manage blog posts', manageMedia: 'Manage media', translationWorkflow: 'Translation workflow', translationWorkflowDescription: 'Translation completeness is shown on each content record so missing work is never inferred from incomplete aggregate data.' },
    notFound: { title: 'Admin page not found', description: 'The requested administration page does not exist.', backToDashboard: 'Back to dashboard' },
    actions: { lifecycle: 'Content lifecycle actions', activation: 'Activation controls', publish: 'Publish', archive: 'Archive', activate: 'Activate', deactivate: 'Deactivate', preview: 'Preview public page', saving: 'Saving…', cancel: 'Cancel', confirm: 'Confirm {action}?', deactivateConfirmTitle: 'Deactivate this item?', deactivateConfirmDescription: 'This removes it from its public placement.' },
    chrome: { productName: 'TahaMohamadi.ir', name: 'Admin', logout: 'Log out', openNavigation: 'Open administration navigation', closeNavigation: 'Close administration navigation', toggleNavigation: 'Toggle administration navigation', navigationLabel: 'Administration navigation', content: 'Content' },
    navigationGroups: { workspace: 'Workspace', publishing: 'Publishing', profile: 'Profile', assets: 'Assets and inbox' },
    navigationItems: { dashboard: 'Dashboard', siteSettings: 'Site settings', navigation: 'Navigation', pages: 'Pages', blogPosts: 'Blog posts', blogCategories: 'Blog categories', blogTags: 'Blog tags', translationQueue: 'Translation queue', resume: 'Resume', publications: 'Publications', portfolio: 'Portfolio', skills: 'Skills', media: 'Media', socialLinks: 'Social links', featured: 'Featured content', contactMessages: 'Contact messages' },
    siteSettings: {
      title: 'Site settings', description: 'Controlled public identity and presentation options. Custom CSS is not supported.', identity: 'Public identity', identityHelp: 'Edit each locale independently. Missing text remains visibly incomplete instead of borrowing another language.', footer: 'Footer content', footerHelp: 'Keep the footer concise: a clear professional statement, availability, and rights notice.', presentation: 'Presentation assets', presentationHelp: 'Choose managed assets and the approved presentation preset for public routes.', saved: 'All changes saved', unsaved: 'Unsaved changes',
      siteName: 'Site name', tagline: 'Tagline', footerStatement: 'Footer statement', footerAvailability: 'Footer availability', footerRights: 'Footer rights', logo: 'Logo media', ogMedia: 'Open Graph media', theme: 'Theme preset', density: 'Layout density', save: 'Save settings', reload: 'Reload',
      themeOptions: { EDITORIAL_NAVY: 'Editorial navy' },
      densityOptions: { COMFORTABLE: 'Comfortable', STANDARD: 'Standard' }
    },
    featured: { title: 'Featured content', description: 'Feature published publications or portfolio projects on the public Home page.', create: 'Create featured content', edit: 'Edit featured content', save: 'Save featured content', discard: 'Discard unsaved featured-content changes?', conflict: 'This featured item changed elsewhere. Reload it before saving.', homeOrder: 'Home order {order}', active: 'Active', inactive: 'Inactive', placement: 'Public placement', placementHint: 'Featured content is shown in the Home placement.', targetType: 'Target type', publishedTarget: 'Published target', publication: 'Publication', project: 'Portfolio project', selectPublishedTarget: 'Select a published target.', missingTranslation: 'Missing translation', publicationTarget: '{key} / FA: {fa} / EN: {en}', projectTarget: '{key} / FA: {fa} / EN: {en}', sortOrder: 'Sort order', saved: 'Featured content saved.', activated: 'Featured content activated.', deactivated: 'Featured content deactivated.' },
    blogTaxonomy: {
      active: 'Active', inactive: 'Inactive', version: 'Version {version}', newVersion: 'new', name: 'Name', slug: 'Slug', sortOrder: 'Sort order', seoMetadata: 'SEO metadata', seoTitle: 'SEO title', seoDescription: 'SEO description',
      categories: { title: 'Blog categories', description: 'Maintain category labels and their independent public translations.', create: 'Create category', edit: 'Edit category', new: 'New category', key: 'Category key', save: 'Save category', conflict: 'This category changed elsewhere. Reload it before saving.' },
      tags: { title: 'Blog tags', description: 'Maintain tag labels and their independent public translations.', create: 'Create tag', edit: 'Edit tag', new: 'New tag', key: 'Tag key', save: 'Save tag', conflict: 'This tag changed elsewhere. Reload it before saving.' }
    },
    blogTranslationSource: 'Source language',
    blogTranslationStatuses: { MISSING: 'Missing', INCOMPLETE: 'Incomplete', COMPLETE: 'Complete', OUTDATED: 'Outdated' },
    translationQueue: { title: 'Translation queue', description: 'Review only content that needs a bilingual update. Source content is never copied over the target locale.', filter: 'Show', needsAttention: 'Needs attention', missing: 'Missing', outdated: 'Outdated', all: 'All recent posts', empty: 'No matching posts in the latest 100 records.', untitled: 'Untitled post', unavailable: 'Unavailable', sourceUpdated: 'Source updated {date}', sourceLanguage: 'Source: {locale}', openEditor: 'Open editor', selectItem: 'Select a post to compare both independent locales.', complete: 'Complete', incomplete: 'Incomplete', checklist: { title: 'Title', slug: 'Slug', body: 'Body', seo: 'SEO metadata' } },
    blogPosts: { title: 'Blog posts', description: 'Create, translate, preview, and publish posts from their source Markdown.', create: 'Create post', item: 'Blog post', edit: 'Edit blog post', new: 'New blog post', category: 'Category', tags: 'Tags', media: 'Attached media', titleField: 'Title', slug: 'Slug', excerpt: 'Excerpt', seoMetadata: 'SEO metadata', seoTitle: 'SEO title', seoDescription: 'SEO description', sourceLanguage: 'Source language', save: 'Save draft', version: 'Version {version}', newVersion: 'new', conflict: 'This post changed elsewhere. Reload it before saving.', revisions: 'Revision history', revision: 'Revision {number}', revisionHelp: 'Restoring creates a separate draft and never overwrites the current post.', restoreAsDraft: 'Restore as draft', refreshRevisions: 'Refresh revisions', noRevisions: 'No revisions yet.', revisionCompare: 'Compare revision', compareCurrent: 'Current', review: 'Editorial review', reviewHelp: 'Review keeps the post private while making its publication decision explicit.', submitForReview: 'Submit for review', returnToDraft: 'Return to draft', schedule: 'Schedule publication', scheduledFor: 'Publish at (your local time)', cancelSchedule: 'Cancel schedule' },
    markdown: { editorAndPreview: 'Markdown editor and preview', source: 'Markdown', safePreview: 'Safe Markdown preview', preview: 'Preview', previewError: 'The Markdown preview could not be rendered safely.' },
    articleEditor: { label: 'Article block editor', mode: 'Article editor mode', blockMode: 'Blocks', markdownMode: 'Markdown', edit: 'Edit blocks', preview: 'Preview article', readingTime: '{minutes} min read', headingLevel: 'Heading level', codeLanguage: 'Code language', imageSource: 'Image URL', imageAlt: 'Image alt text', imageCaption: 'Image caption', divider: 'Divider', moveUp: 'Move block {index} up', moveDown: 'Move block {index} down', remove: 'Remove block {index}', types: { paragraph: 'Paragraph', heading: 'Heading', quote: 'Quote', code: 'Code block', image: 'Image', divider: 'Divider', markdown: 'Markdown block' }, add: { paragraph: 'Add paragraph', heading: 'Add heading', quote: 'Add quote', code: 'Add code', image: 'Add image', divider: 'Add divider', markdown: 'Add Markdown' } },
    media: { title: 'Media', description: 'Upload approved assets, maintain bilingual metadata, and review orphaned files before archiving.', uploadTitle: 'Upload media', file: 'Media file', faAlt: 'Persian alt text', enAlt: 'English alt text', faCaption: 'Persian caption', enCaption: 'English caption', uploadProgress: 'Upload progress', upload: 'Upload media', orphanNotice: '{count} orphaned media assets can be reviewed below before archiving.', bytes: '{count} bytes', orphaned: 'Orphaned asset', localizedMetadata: 'Metadata is localized independently for public image use.', documentRepresentation: 'Document representation: {mimeType}', saveMetadata: 'Save metadata', archiveOrphan: 'Archive orphaned asset', archiveTitle: 'Archive this orphaned asset?', archiveDescription: 'It will no longer be available for public or CMS use.', usageNotice: 'This asset is used by {count} content item(s) and cannot be archived.', usageTypes: { PAGE_OPEN_GRAPH: 'Page Open Graph', BLOG_COVER: 'Blog cover', BLOG_INLINE: 'Blog content', PORTFOLIO_COVER: 'Portfolio cover', PUBLICATION_COVER: 'Publication cover', RESUME_DOCUMENT: 'Resume document' }, status: 'Status', allStatuses: 'All statuses', active: 'Active', archived: 'Archived' },
    mediaUsage: { notice: 'This asset is used by {count} content item(s) and cannot be archived.', types: { PAGE_OPEN_GRAPH: 'Page Open Graph', SETTINGS_LOGO: 'Site logo', SETTINGS_OPEN_GRAPH: 'Site Open Graph', COMPOSER_BLOCK: 'Page composer block', BLOG_COVER: 'Blog cover', BLOG_INLINE: 'Blog content', PORTFOLIO_COVER: 'Portfolio cover', PUBLICATION_COVER: 'Publication cover', RESUME_DOCUMENT: 'Resume document' } },
    mediaReplace: { action: 'Replace asset', title: 'Replace this asset?', description: 'Every registered use will move to the selected active asset, then this asset will be archived.', replacement: 'Replacement asset', confirm: 'Replace asset' },
    mediaSelector: { selection: 'Media selection', label: 'Media asset', optionLabel: '{name} ({mimeType})', retry: 'Retry media list', search: 'Search media', type: 'Type', allTypes: 'All supported types', types: { image: 'Images', document: 'PDF documents' }, selected: 'Selected: {name}', empty: 'No matching active media.', pagination: 'Media result pages', uploadFile: 'Upload a new file', upload: 'Upload and select', uploadProgress: 'Upload progress', invalidType: 'This file type is not allowed for this field.' },
    navigation: {
      title: 'Navigation', description: 'Each visible item must have separate Persian and English labels.', add: 'Add item', save: 'Save navigation',
      empty: 'No managed navigation items yet. The public fallback remains active until you save items.', key: 'Stable key', visible: 'Visible', external: 'External HTTPS link',
      externalTarget: 'HTTPS target', internalTarget: 'Localized internal path', externalHint: 'https://…', internalHint: '/{lang}/… (recommended), /fa/… or /en/…',
      faLabel: 'Label (fa)', enLabel: 'Label (en)', moveUp: 'Move item {index} up', moveDown: 'Move item {index} down', remove: 'Remove item {index}', reload: 'Reload'
    },
    pages: {
      title: 'Managed pages', description: 'Page translations and approved block composition are edited independently.', create: 'Create page', edit: 'Edit page',
      pageKey: 'Page key', titleField: 'Title', slug: 'Slug', summary: 'Summary', seoTitle: 'SEO title', seoDescription: 'SEO description', canonicalPath: 'Canonical path',
      save: 'Save page', missingTranslation: 'Missing translation', reload: 'Reload page'
    },
    contact: {
      title: 'Contact messages', description: 'Read messages safely as plain text and archive them when handled.', selected: 'Selected contact message', markRead: 'Mark as read', archive: 'Archive message', archiveTitle: 'Archive this message?', archiveDescription: 'It will remain in the archived queue for reference.'
    },
    navigationDialog: {
      invalidTarget: 'Use an HTTPS URL or localized internal route.', removeTitle: 'Remove this navigation item?', removeDescription: 'The item will be removed when you save navigation.', removeConfirm: 'Remove item'
    },
    caseStudy: { role: 'Role', client: 'Client', team: 'Team', outcome: 'Outcome' },
    composer: {
      title: 'Page composer', description: 'Approved blocks only. Each translation remains independent.', add: 'Add block', addSection: 'Add section', section: 'Section', sectionHeading: 'Section {index}', sectionAdded: 'Section {index} added.', sectionRemoved: 'Section {index} removed; its blocks moved to the first section.', sectionMoved: 'Section moved from {from} to {to}.', moveSectionUp: 'Move section {index} up', moveSectionDown: 'Move section {index} down', removeSection: 'Remove section {index}', preview: 'Preview composition', previewTitle: 'Composition preview', previewSavedDraft: 'Preview reflects the last saved Draft.', closePreview: 'Close composition preview', save: 'Save composition', reload: 'Reload blocks', autosave: { idle: '', pending: 'Draft changes will save shortly.', saving: 'Saving draft…', saved: 'Draft saved.', error: 'Draft was not saved. Review the fields and retry.', conflict: 'Draft changed elsewhere. Reload before saving again.' },
      empty: 'No blocks yet. Add a block to compose this page.', type: 'Block type', visible: 'Visible', media: 'Media asset', collection: 'Collection', limit: 'Items to show',
      eyebrowFa: 'Eyebrow (fa)', titleFa: 'Title (fa)', leadFa: 'Lead (fa)', actionLabelFa: 'Action label (fa)', actionPathFa: 'Action path (fa)', altFa: 'Media alt text (fa)',
      eyebrowEn: 'Eyebrow (en)', titleEn: 'Title (en)', leadEn: 'Lead (en)', actionLabelEn: 'Action label (en)', actionPathEn: 'Action path (en)', altEn: 'Media alt text (en)',
      actionHint: '/fa, /en, or HTTPS only', invalidActionPath: 'Use a localized internal path or HTTPS URL.', moveUp: 'Move block {index} up', moveDown: 'Move block {index} down', remove: 'Remove block {index}', removeTitle: 'Remove this block?', removeDescription: 'Its content will be removed when you save the composition.', cancel: 'Cancel', removeConfirm: 'Remove block', blockHeading: '{type} block {index}', added: 'Added block at position {index}.', removed: 'Removed block {index}.', moved: 'Moved block from position {from} to {to}.',
      blockTypes: { HERO: 'Hero', RICH_TEXT: 'Rich text', MEDIA: 'Media', MEDIA_TEXT: 'Text and media', CALL_TO_ACTION: 'Call to action', COLLECTION: 'Collection grid', SKILLS: 'Skills', RESUME: 'Resume', SOCIAL_LINKS: 'Social links', CONTACT: 'Contact' },
      collectionSources: { BLOG: 'Blog posts', PORTFOLIO: 'Portfolio projects', PUBLICATIONS: 'Publications' }
    }
  }
}
