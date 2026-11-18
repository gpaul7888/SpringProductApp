import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Page, Product, ProductRequest, ProductSummary } from './product.models';

/** Thin typed wrapper around product-service (reached through the gateway at /api/products). */
@Injectable({ providedIn: 'root' })
export class ProductApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/products';

  list(query: string, page: number, size: number): Observable<Page<Product>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'name,asc');
    if (query.trim()) {
      params = params.set('q', query.trim());
    }
    return this.http.get<Page<Product>>(this.baseUrl, { params });
  }

  summary(): Observable<ProductSummary> {
    return this.http.get<ProductSummary>(`${this.baseUrl}/summary`);
  }

  create(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, request);
  }

  update(id: number, request: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
