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
      href: { default: '' },
      filename: { default: '' },
      mediaType: { default: '' },
      size: { default: 0 },
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
    const href = String(node.attrs.href || '')
    const filename = String(node.attrs.filename || '附件')
    const size = Number(node.attrs.size || 0)
    const sizeLabel = formatFileSize(size)

    const description = sizeLabel ? `${filename} · ${sizeLabel}` : filename
    const wrapperAttrs = mergeAttributes(HTMLAttributes, {
      'data-halo-attachment-download': 'true',
      style: CARD_STYLE,
    })

    if (!href) {
      return ['div', wrapperAttrs, ['span', {}, '附件未选择']]
    }

    return [
      'div',
      wrapperAttrs,
      ['span', { style: 'min-width:0;overflow-wrap:anywhere;color:#334155' }, description],
      [
        'a',
        {
          href,
          download: filename,
          'data-halo-download-link': 'true',
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
