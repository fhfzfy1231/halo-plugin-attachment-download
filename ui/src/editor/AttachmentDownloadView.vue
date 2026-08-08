<script setup lang="ts">
import type { Attachment } from '@halo-dev/api-client'
import { utils, type AttachmentLike } from '@halo-dev/ui-shared'
import { NodeViewWrapper } from '@halo-dev/richtext-editor'
import { ref } from 'vue'
import RiDownload2Line from '~icons/ri/download-2-line'
import RiFileLine from '~icons/ri/file-line'

const props = defineProps<{
  node: { attrs: Record<string, unknown> }
  updateAttributes: (attributes: Record<string, unknown>) => void
}>()
const selectorVisible = ref(false)

function onAttachmentSelect(attachments: AttachmentLike[]) {
  const attachment = attachments[0]
  if (!attachment) return

  const href = utils.attachment.getUrl(attachment) ?? ''
  props.updateAttributes({
    href,
    filename: getFilename(attachment, href),
    mediaType: getMediaType(attachment),
    size: getSize(attachment),
  })
  selectorVisible.value = false
}

function isFullAttachment(value: AttachmentLike): value is Attachment {
  return typeof value === 'object' && value !== null && 'spec' in value
}

function getFilename(attachment: AttachmentLike, href: string): string {
  if (isFullAttachment(attachment) && attachment.spec.displayName) {
    return attachment.spec.displayName
  }

  const url =
    typeof attachment === 'string' ? attachment : 'url' in attachment ? attachment.url : href
  try {
    const pathname = new URL(url, window.location.origin).pathname
    const segments = pathname.split('/').filter(Boolean)
    return decodeURIComponent(segments[segments.length - 1] || '附件')
  } catch {
    return '附件'
  }
}

function getMediaType(attachment: AttachmentLike): string {
  if (isFullAttachment(attachment)) return attachment.spec.mediaType ?? ''
  if (typeof attachment === 'object') return attachment.mediaType ?? ''
  return ''
}

function getSize(attachment: AttachmentLike): number {
  return isFullAttachment(attachment) ? (attachment.spec.size ?? 0) : 0
}
</script>

<template>
  <NodeViewWrapper class="attachment-download-editor" data-drag-handle>
    <div class="file-info">
      <RiFileLine class="file-icon" />
      <div class="file-text">
        <strong>{{ node.attrs.filename || '尚未选择附件' }}</strong>
        <span>{{ node.attrs.href || '从 Halo 附件库选择需要提供下载的文件' }}</span>
      </div>
    </div>

    <button type="button" class="select-button" @click="selectorVisible = true">
      <RiDownload2Line />
      {{ node.attrs.href ? '重新选择' : '选择附件' }}
    </button>

    <AttachmentSelectorModal
      v-model:visible="selectorVisible"
      :max="1"
      @select="onAttachmentSelect"
    />
  </NodeViewWrapper>
</template>

<style scoped>
.attachment-download-editor {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 16px 0;
  padding: 16px 18px;
  border: 1px solid #dbe3ee;
  border-radius: 12px;
  background: #f8fafc;
}

.file-info {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.file-icon {
  width: 24px;
  height: 24px;
  flex: 0 0 auto;
  color: #64748b;
}

.file-text {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
}

.file-text strong {
  overflow: hidden;
  color: #0f172a;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-text span {
  overflow: hidden;
  max-width: 560px;
  color: #64748b;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.select-button {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 7px;
  min-height: 38px;
  padding: 0 14px;
  border: 0;
  border-radius: 8px;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
  font-weight: 600;
}

.select-button:hover {
  background: #1d4ed8;
}
</style>
