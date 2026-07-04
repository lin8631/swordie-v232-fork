import { onMounted, onBeforeMount, onBeforeUnmount } from 'vue';
import { useAppStore } from '@/store';
import { addEventListen, removeEventListen } from '@/utils/event';

const WIDTH = 992;

function queryDevice() {
  const rect = document.body.getBoundingClientRect();
  return rect.width - 1 < WIDTH;
}

export default function useResponsive(immediate?: boolean) {
  const appStore = useAppStore();
  function resizeHandler() {
    if (!document.hidden) {
      const isMobile = queryDevice();
      appStore.toggleDevice(isMobile ? 'mobile' : 'desktop');
      appStore.toggleMenu(isMobile);
    }
  }
  onMounted(() => {
    addEventListen(window, 'resize', resizeHandler);
  });
  onBeforeMount(() => {
    addEventListen(window, 'resize', resizeHandler);
  });
  onBeforeUnmount(() => {
    removeEventListen(window, 'resize', resizeHandler);
  });
}
