export interface SalonSummary {
  id: number;
  name: string;
  district: string;
  rating: number | null;
  reviewsCount: number | null;
  priceLevel: number | null;
}

export interface Salon {
  id: number;
  placeId: string;
  name: string;
  address: string;
  district: string;
  phone: string | null;
  website: string | null;
  services: string[] | null;
  priceLevel: number | null;
  rating: number | null;
  reviewsCount: number | null;
  lat: number | null;
  lng: number | null;
  updatedAt: number;
}

export interface SalonListResponse {
  items: SalonSummary[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export type SalonPatch = {
  name?: string;
  address?: string;
  district?: string;
  services?: string[];
  priceLevel?: number;
};

const BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080';

export async function getSalons(params: {
  district?: string;
  q?: string;
  sort?: string;
  page?: number;
  pageSize?: number;
} = {}): Promise<SalonListResponse> {
  const url = new URL('/api/salons', BASE);
  if (params.district) url.searchParams.set('district', params.district);
  if (params.q) url.searchParams.set('q', params.q);
  if (params.sort) url.searchParams.set('sort', params.sort);
  if (params.page) url.searchParams.set('page', String(params.page));
  if (params.pageSize) url.searchParams.set('pageSize', String(params.pageSize));
  const res = await fetch(url.toString(), { cache: 'no-store' });
  if (!res.ok) throw new Error('Failed to fetch salons');
  return res.json();
}

export async function getSalon(id: number): Promise<Salon> {
  const res = await fetch(`${BASE}/api/salons/${id}`, { cache: 'no-store' });
  if (res.status === 404) throw new NotFoundError();
  if (!res.ok) throw new Error('Failed to fetch salon');
  return res.json();
}

export async function getDistricts(): Promise<string[]> {
  const res = await fetch(`${BASE}/api/districts`, { cache: 'no-store' });
  if (!res.ok) throw new Error('Failed to fetch districts');
  return res.json();
}

export async function patchSalon(id: number, data: SalonPatch): Promise<Salon> {
  const res = await fetch(`${BASE}/api/salons/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new ApiError(res.status, (err as { error?: string }).error ?? 'Update failed');
  }
  return res.json();
}

export class NotFoundError extends Error {}
export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}
