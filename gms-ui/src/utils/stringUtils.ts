import { useI18n } from 'vue-i18n';

export function isValidString(data: any) {
  return typeof data === 'string' && data.trim() !== '';
}

export function timestampToChineseTime(timestamp: number | string) {
  const { locale } = useI18n();

  if (timestamp === -1) {
    return locale.value === 'en-US' ? 'Permanent' : '永久';
  }
  if (typeof timestamp === 'string') timestamp?.replace(' ', 'T');
  const date = new Date(timestamp);

  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();

  if (locale.value === 'en-US') {
    return `${year}-${month.toString().padStart(2, '0')}-${day
      .toString()
      .padStart(2, '0')} ${hours.toString().padStart(2, '0')}:${minutes
      .toString()
      .padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }
  return `${year}年${month}月${day}日 ${hours}时${minutes}分${seconds}秒`;
}
