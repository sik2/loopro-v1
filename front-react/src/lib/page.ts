/** back의 PageDto와 짝이 맞는 타입. page는 1부터 시작한다. */
export type Page<T> = {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}
