import { App } from 'vue';

export default function handleError(Vue: App, baseUrl: string) {
  if (!baseUrl) {
    return;
  }
  Vue.config.errorHandler = (err: unknown, instance: any, info: string) => {
  };
}
