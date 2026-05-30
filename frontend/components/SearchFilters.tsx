'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useRef, useState } from 'react';

const INPUT_CLS =
  'border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white';

export function SearchFilters({ districts }: { districts: string[] }) {
  const params = useSearchParams();
  const router = useRouter();
  const debounce = useRef<ReturnType<typeof setTimeout>>();

  const [q, setQ] = useState(params.get('q') ?? '');

  const push = (key: string, value: string) => {
    const next = new URLSearchParams(params.toString());
    if (value) next.set(key, value);
    else next.delete(key);
    next.delete('page'); // reset to page 1 whenever a filter changes
    router.push(`/?${next.toString()}`);
  };

  const handleQ = (value: string) => {
    setQ(value);
    clearTimeout(debounce.current);
    debounce.current = setTimeout(() => push('q', value), 350);
  };

  const hasFilters =
    params.get('q') || params.get('district') || params.get('sort');

  return (
    <div className="flex gap-3 flex-wrap items-center">
      <input
        type="text"
        placeholder="Search by name…"
        value={q}
        onChange={(e) => handleQ(e.target.value)}
        className={INPUT_CLS}
        aria-label="Search salons by name"
      />

      <select
        value={params.get('district') ?? ''}
        onChange={(e) => push('district', e.target.value)}
        className={INPUT_CLS}
        aria-label="Filter by district"
      >
        <option value="">All districts</option>
        {districts.map((d) => (
          <option key={d} value={d}>{d}</option>
        ))}
      </select>

      <select
        value={params.get('sort') ?? ''}
        onChange={(e) => push('sort', e.target.value)}
        className={INPUT_CLS}
        aria-label="Sort salons"
      >
        <option value="">Highest rated</option>
        <option value="reviews">Most reviewed</option>
        <option value="name">Name (A–Z)</option>
      </select>

      <select
        value={params.get('pageSize') ?? '20'}
        onChange={(e) => push('pageSize', e.target.value)}
        className={INPUT_CLS}
        aria-label="Salons per page"
      >
        <option value="10">10 per page</option>
        <option value="20">20 per page</option>
        <option value="50">50 per page</option>
      </select>

      {hasFilters && (
        <a href="/" className="text-sm text-gray-500 hover:text-gray-700 hover:underline">
          Clear
        </a>
      )}
    </div>
  );
}
