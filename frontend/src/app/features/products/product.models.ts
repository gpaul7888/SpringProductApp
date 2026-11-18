/** Mirrors product-service's ProductResponse record. */
export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  ownerId: string | null;
  ownerName: string | null;
  createdAt: string;
  updatedAt: string;
  editable: boolean;
}

export interface ProductRequest {
  name: string;
  description: string | null;
  price: number;
}

/** Mirrors product-service's PageResponse record. */
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Mirrors product-service's ProductSummary record. */
export interface ProductSummary {
  owner: { id: string; email: string | null; fullName: string; role: string | null };
  profileFromAuthService: boolean;
  myProductCount: number;
  myInventoryValue: number;
  totalProducts: number;
}
