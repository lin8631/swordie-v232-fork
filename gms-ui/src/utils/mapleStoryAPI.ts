export function getIconUrl(
  category: string,
  id: string | number,
  location = 'GMS',
  version = '232'
): string {
  if (!id || id <= 0) return '';
  return `https://maplestory.io/api/${location}/${version}/${category}/${id}/icon`;
}
