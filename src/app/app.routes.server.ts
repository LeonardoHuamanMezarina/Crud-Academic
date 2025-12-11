import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Ruta dinámica de edición: no tiene sentido prerenderizar todas las variantes
  // (depende de sesión/autorización). La marcamos como Server para que se renderice
  // bajo demanda en el servidor en vez de durante el build.
  {
    path: 'clientes/editar/:id',
    renderMode: RenderMode.Server
  },
  // Evitamos prerenderizar la ruta con parámetro 'pedidos/:id' — se renderiza bajo demanda en el servidor
  {
    path: 'pedidos/:id',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
