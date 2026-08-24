export function formatDate(value: string) {
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

/** 목록·상세의 한 줄짜리 표기. 날짜만으로는 같은 날 올라온 글의 순서를 알 수 없다. */
export function formatDateTime(value: string) {
  return new Date(value).toLocaleString('ko-KR', {
    year: '2-digit',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
