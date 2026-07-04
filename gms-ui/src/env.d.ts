/// <reference types="vite/client" />
declare module '*.vue' {
  import type { DefineComponent } from 'vue';
  const component: DefineComponent<{}, {}, any>;
  export default component;
}
declare module '*.svg' {
  const content: any;
  export default content;
}

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
}
