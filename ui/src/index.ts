import { definePlugin } from '@halo-dev/ui-shared'

export default definePlugin({
  components: {},
  routes: [],
  extensionPoints: {
    'default:editor:extension:create': async () => {
      const { AttachmentDownloadExtension } = await import('./editor')
      return [AttachmentDownloadExtension]
    },
  },
})
