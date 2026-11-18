import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * Every page shows per-user data behind a login stored in the browser, so pages are rendered
 * in the browser. (Prerendering would bake data into HTML at build time and call the API
 * during the build.)
 */
export const serverRoutes: ServerRoute[] = [
  {
    path: '**',
    renderMode: RenderMode.Client,
  },
];
