export interface PageResult<T> {
  list: T[]
  page: number
  size: number
  total: number
}
