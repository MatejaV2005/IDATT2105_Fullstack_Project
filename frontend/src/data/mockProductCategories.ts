export interface ProductCategory {
  id: number
  name: string
  description: string
}

export const mockProductCategories: ProductCategory[] = [
  { id: 10, name: 'Fisk', description: 'Fisk og sjomat' },
  { id: 11, name: 'Kjott', description: 'Kjott og farseprodukter' },
  { id: 12, name: 'Kylling', description: 'Fjaerfe og egg' },
  { id: 13, name: 'Meieri', description: 'Melk, ost og yoghurt' },
  { id: 14, name: 'Saus', description: 'Varme og kalde sauser' },
]

export function getMockProductCategoryName(categoryId: number): string {
  return (
    mockProductCategories.find((category) => category.id === categoryId)?.name ||
    `Produktkategori ${categoryId}`
  )
}
