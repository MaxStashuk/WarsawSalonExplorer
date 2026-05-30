'use client';

import { useRouter, useSearchParams } from 'next/navigation';

interface Props {
  page: number;
  totalPages: number;
}

export function Pagination({ page, totalPages }: Props) {
  const params = useSearchParams();
  const router = useRouter();

  if (totalPages <= 1) return null;

  const go = (p: number) => {
    const next = new URLSearchParams(params.toString());
    next.set('page', String(p));
    router.push(`/?${next.toString()}`);
  };

  return (
    <div className="flex items-center justify-center gap-3 mt-8">
      <button
        onClick={() => go(page - 1)}
        disabled={page <= 1}
        className="px-3 py-1.5 border border-gray-300 rounded-md text-sm hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
      >
        ← Prev
      </button>
      <span className="text-sm text-gray-600">
        Page {page} of {totalPages}
      </span>
      <button
        onClick={() => go(page + 1)}
        disabled={page >= totalPages}
        className="px-3 py-1.5 border border-gray-300 rounded-md text-sm hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
      >
        Next →
      </button>
    </div>
  );
}
