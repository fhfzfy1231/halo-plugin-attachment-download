import {
  Node,
  ToolboxItem,
  VueNodeViewRenderer,
  mergeAttributes,
  type Editor,
  type ExtensionOptions,
  type Range,
} from '@halo-dev/richtext-editor'
import { markRaw } from 'vue'
import RiDownload2Line from '~icons/ri/download-2-line'
import AttachmentDownloadView from './AttachmentDownloadView.vue'

declare module '@halo-dev/richtext-editor' {
  interface Commands<ReturnType> {
    attachmentDownload: {
      insertAttachmentDownload: () => ReturnType
    }
  }
}

const CARD_STYLE = [
  'display:flex',
  'align-items:center',
  'justify-content:space-between',
  'gap:16px',
  'margin:16px 0',
  'padding:16px 18px',
  'border:1px solid #e5e7eb',
  'border-radius:12px',
  'background:#f8fafc',
].join(';')

const BUTTON_STYLE = [
  'display:inline-flex',
  'align-items:center',
  'justify-content:center',
  'min-height:40px',
  'padding:0 18px',
  'border-radius:9px',
  'background:#2563eb',
  'color:#fff',
  'font-weight:600',
  'text-decoration:none',
  'white-space:nowrap',
].join(';')

const AttachmentDownload = Node.create<ExtensionOptions>({
  name: 'attachment_download',
  group: 'block',
  atom: true,
  draggable: true,

  addAttributes() {
    return {
      // href is retained only so v1.0.x content can be detected and migrated.
      // It is intentionally never rendered by v1.1.0.
      href: {
        default: '',
        parseHTML: (element) => element.getAttribute('href') || '',
        renderHTML: () => ({}),
      },
      attachmentName: {
        default: '',
        parseHTML: (element) => element.getAttribute('data-attachment-name') || '',
        renderHTML: (attributes) =>
          attributes.attachmentName
            ? { 'data-attachment-name': String(attributes.attachmentName) }
            : {},
      },
      filename: {
        default: '',
        parseHTML: (element) =>
          element.getAttribute('data-filename') || element.getAttribute('filename') || '',
        renderHTML: (attributes) =>
          attributes.filename ? { 'data-filename': String(attributes.filename) } : {},
      },
      mediaType: {
        default: '',
        parseHTML: (element) =>
          element.getAttribute('data-media-type') || element.getAttribute('mediaType') || '',
        renderHTML: (attributes) =>
          attributes.mediaType ? { 'data-media-type': String(attributes.mediaType) } : {},
      },
      size: {
        default: 0,
        parseHTML: (element) =>
          Number(element.getAttribute('data-size') || element.getAttribute('size') || 0),
        renderHTML: (attributes) =>
          Number(attributes.size) > 0 ? { 'data-size': String(attributes.size) } : {},
      },
    }
  },

  addOptions() {
    return {
      getToolboxItems({ editor }: { editor: Editor }) {
        return {
          priority: 52,
          component: markRaw(ToolboxItem),
          props: {
            editor,
            icon: markRaw(RiDownload2Line),
            title: '附件下载',
            description: '从附件库选择文件并插入下载按钮',
            action: () => editor.chain().focus().insertAttachmentDownload().run(),
          },
        }
      },
      getCommandMenuItems() {
        return {
          priority: 82,
          icon: markRaw(RiDownload2Line),
          title: '附件下载',
          keywords: ['download', 'attachment', '附件', '下载'],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor.chain().focus().deleteRange(range).insertAttachmentDownload().run()
          },
        }
      },
    }
  },

  addCommands() {
    return {
      insertAttachmentDownload:
        () =>
        ({ commands }) =>
          commands.insertContent({ type: this.name }),
    }
  },

  addNodeView() {
    return VueNodeViewRenderer(AttachmentDownloadView as never)
  },

  parseHTML() {
    return [{ tag: 'div[data-halo-attachment-download]' }]
  },

  renderHTML({ HTMLAttributes, node }) {
    const attachmentName = String(node.attrs.attachmentName || '')
    const filename = String(node.attrs.filename || '附件')
    const size = Number(node.attrs.size || 0)
    const sizeLabel = formatFileSize(size)

    const description = sizeLabel ? `${filename} · ${sizeLabel}` : filename
    const safeAttributes = { ...HTMLAttributes }
    delete safeAttributes.href
    const wrapperAttrs = mergeAttributes(safeAttributes, {
      'data-halo-attachment-download': 'true',
      style: CARD_STYLE,
    })

    if (!attachmentName) {
      return [
        'div',
        wrapperAttrs,
        ['span', {}, node.attrs.href ? '附件需要重新选择后才能启用权限下载' : '附件未选择'],
      ]
    }

    const protectedHref = `/apis/api.attachment-download.foxbridge.team/v1alpha1/downloads/${encodeURIComponent(attachmentName)}`

    return [
      'div',
      wrapperAttrs,
      ['span', { style: 'min-width:0;overflow-wrap:anywhere;color:#334155' }, description],
      [
        'a',
        {
          href: protectedHref,
          download: filename,
          'data-halo-protected-download': 'true',
          style: BUTTON_STYLE,
        },
        '下载文件',
      ],
    ]
  },
})

function formatFileSize(size: number): string {
  if (!Number.isFinite(size) || size <= 0) return ''
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)} MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`
}

export default AttachmentDownload
