/// <reference types="vite/client" />
/// <reference types="unplugin-icons/types/vue" />

import type { DefineComponent } from 'vue'

declare module 'vue' {
  export interface GlobalComponents {
    AttachmentSelectorModal: DefineComponent<{
      visible?: boolean
      accepts?: string[]
      min?: number
      max?: number
    }>
  }
}

export {}
