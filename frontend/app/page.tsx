import { Suspense } from 'react';
import Link from 'next/link';
import { Star, MapPin } from 'lucide-react';
import { getSalons, getDistricts, type SalonSummary } from '@/lib/api';
import { SearchFilters } from '@/components/SearchFilters';

interface PageProps {
  searchParams: { district?: string; q?: string; sort?: string };
}

export default async function Page({ searchParams }: PageProps) {
  const [{ items, total }, districts] = await Promise.all([
    getSalons({
      district: searchParams.district,
      q: searchParams.q,
      sort: searchParams.sort,
    }),
    getDistricts(),
  ]);

  return (
    <main className="max-w-6xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-1">Warsaw Beauty Salons</h1>
        <p className="text-gray-500 text-sm">Hair &amp; beauty salons across Warsaw</p>
      </div>

      <div className="mb-6">
        <Suspense>
          <SearchFilters districts={districts} />
        </Suspense>
      </div>

      <p className="text-sm text-gray-500 mb-4">
        Showing {items.length} of {total} salons
      </p>

      {items.length === 0 ? (
        <div className="text-center py-20 text-gray-500">
          <p className="text-lg mb-2">No salons match your filters.</p>
          <a href="/" className="text-blue-600 hover:underline text-sm">
            Clear filters
          </a>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {items.map((salon) => (
            <SalonCard key={salon.id} salon={salon} />
          ))}
        </div>
      )}
    </main>
  );
}

function SalonCard({ salon }: { salon: SalonSummary }) {
  return (
    <div className="bg-white border border-gray-200 rounded-xl p-5 hover:shadow-md transition-shadow flex flex-col gap-3">
      <div>
        <h2 className="font-semibold text-gray-900 leading-snug line-clamp-2 mb-1">
          {salon.name}
        </h2>
        <span className="inline-flex items-center gap-1 text-xs text-blue-700 bg-blue-50 px-2 py-0.5 rounded-full">
          <MapPin className="w-3 h-3" />
          {salon.district}
        </span>
      </div>

      <div className="flex items-center gap-3 mt-auto">
        {salon.rating !== null ? (
          <span className="flex items-center gap-1 text-sm">
            <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
            <span className="font-medium">{salon.rating.toFixed(1)}</span>
            {salon.reviewsCount !== null && (
              <span className="text-gray-400 text-xs">({salon.reviewsCount})</span>
            )}
          </span>
        ) : (
          <span className="text-sm text-gray-400">No rating</span>
        )}

        {salon.priceLevel !== null && salon.priceLevel > 0 && (
          <span className="text-sm text-gray-500">{'€'.repeat(salon.priceLevel)}</span>
        )}

        <Link
          href={`/salons/${salon.id}`}
          className="ml-auto text-sm font-medium text-blue-600 hover:text-blue-800"
        >
          View →
        </Link>
      </div>
    </div>
  );
}
