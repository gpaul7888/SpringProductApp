import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../core/http/problem';
import { ProductApi } from './product-api';
import { Page, Product, ProductRequest, ProductSummary } from './product.models';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-products-page',
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './products-page.html',
  styleUrl: './products-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductsPage {
  private readonly api = inject(ProductApi);

  // List state
  protected readonly page = signal<Page<Product> | null>(null);
  protected readonly loading = signal(false);
  protected readonly listError = signal<string | null>(null);
  protected readonly search = new FormControl('', { nonNullable: true });
  private activeQuery = '';

  // Summary state (product-service combines its data with auth-service's)
  protected readonly summary = signal<ProductSummary | null>(null);

  // Form state - the same form is used to create and to edit
  protected readonly editing = signal<Product | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly form = inject(NonNullableFormBuilder).group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['', Validators.maxLength(1000)],
    price: [null as number | null, [Validators.required, Validators.min(0.01), Validators.max(99_999_999.99)]],
  });

  constructor() {
    this.loadPage(0);
    this.loadSummary();
  }

  protected applySearch(): void {
    this.activeQuery = this.search.value;
    this.loadPage(0);
  }

  protected goToPage(index: number): void {
    this.loadPage(index);
  }

  protected startEdit(product: Product): void {
    this.editing.set(product);
    this.formError.set(null);
    this.form.setValue({ name: product.name, description: product.description ?? '', price: product.price });
  }

  protected cancelEdit(): void {
    this.editing.set(null);
    this.formError.set(null);
    this.form.reset();
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { name, description, price } = this.form.getRawValue();
    const request: ProductRequest = { name, description: description || null, price: price as number };
    const editing = this.editing();
    const call = editing ? this.api.update(editing.id, request) : this.api.create(request);

    this.saving.set(true);
    this.formError.set(null);
    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancelEdit();
        this.refresh(); // the list now updates right after saving
      },
      error: (err: unknown) => {
        this.formError.set(errorMessage(err));
        this.saving.set(false);
      },
    });
  }

  protected remove(product: Product): void {
    if (!confirm(`Delete "${product.name}"?`)) {
      return;
    }
    this.api.delete(product.id).subscribe({
      next: () => {
        if (this.editing()?.id === product.id) {
          this.cancelEdit();
        }
        this.refresh();
      },
      error: (err: unknown) => this.listError.set(errorMessage(err)),
    });
  }

  private refresh(): void {
    this.loadPage(this.page()?.page ?? 0);
    this.loadSummary();
  }

  private loadPage(index: number): void {
    this.loading.set(true);
    this.listError.set(null);
    this.api.list(this.activeQuery, index, PAGE_SIZE).subscribe({
      next: (page) => {
        // Deleting the last item on a page leaves it empty: step back one page.
        if (page.content.length === 0 && page.page > 0) {
          this.loadPage(page.page - 1);
          return;
        }
        this.page.set(page);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.listError.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  private loadSummary(): void {
    this.api.summary().subscribe({
      next: (summary) => this.summary.set(summary),
      error: () => this.summary.set(null), // the summary is optional; the page still works without it
    });
  }
}
