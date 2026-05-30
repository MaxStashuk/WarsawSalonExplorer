import { notFound } from 'next/navigation';
import Link from 'next/link';
import { Star, Phone, Globe, ExternalLink, MapPin, Clock } from 'lucide-react';
import { getSalon, NotFoundError, type Salon } from '@/lib/api';
import { EditSalonForm } from '@/components/EditSalonForm';

interface PageProps {
  params: { id: string };
}

export default async function SalonPage({ params }: PageProps) {
  const id = parseInt(params.id);
  if (isNaN(id)) notFound();

  let salon: Salon;
  try {
    salon = await getSalon(id);
  } catch (e) {
    if (e instanceof NotFoundError) notFound();
    throw e;
  }

  return (
    <main className="max-w-4xl mx-auto px-4 py-8">
      <Link href="/" className="text-sm text-gray-500 hover:text-gray-700 hover:underline mb-6 inline-block">
        ← All salons
      </Link>

      <h1 className="text-3xl font-bold text-gray-900 mb-6">{salon.name}</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* Left: info */}
        <div className="md:col-span-2 space-y-6">
          <InfoRow icon={<MapPin className="w-4 h-4 text-gray-400 flex-shrink-0 mt-0.5" />}>
            <span className="text-gray-700">{salon.address}</span>
            <span className="inline-flex items-center gap-1 text-xs text-blue-700 bg-blue-50 px-2 py-0.5 rounded-full mt-1">
              {salon.district}
            </span>
          </InfoRow>

          {salon.phone && (
            <InfoRow icon={<Phone className="w-4 h-4 text-gray-400 flex-shrink-0" />}>
              <a href={`tel:${salon.phone}`} className="text-blue-600 hover:underline">
                {salon.phone}
              </a>
            </InfoRow>
          )}

          {salon.website && (
            <InfoRow icon={<Globe className="w-4 h-4 text-gray-400 flex-shrink-0" />}>
              <a
                href={salon.website}
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 hover:underline inline-flex items-center gap-1"
              >
                {new URL(salon.website).hostname}
                <ExternalLink className="w-3 h-3" />
              </a>
            </InfoRow>
          )}

          {salon.rating !== null && (
            <InfoRow icon={<Star className="w-4 h-4 fill-yellow-400 text-yellow-400 flex-shrink-0" />}>
              <span className="font-medium">{salon.rating.toFixed(1)}</span>
              {salon.reviewsCount !== null && (
                <span className="text-gray-500 text-sm ml-1">
                  ({salon.reviewsCount} reviews)
                </span>
              )}
              {salon.priceLevel !== null && salon.priceLevel > 0 && (
                <span className="text-gray-500 text-sm ml-3">
                  {'€'.repeat(salon.priceLevel)}
                </span>
              )}
            </InfoRow>
          )}

          {salon.services && salon.services.length > 0 && (
            <div>
              <p className="text-sm font-medium text-gray-500 mb-2">Services</p>
              <div className="flex flex-wrap gap-2">
                {salon.services.map((s) => (
                  <span
                    key={s}
                    className="text-xs bg-gray-100 text-gray-700 px-2.5 py-1 rounded-full"
                  >
                    {s.replace(/_/g, ' ')}
                  </span>
                ))}
              </div>
            </div>
          )}

          <InfoRow icon={<Clock className="w-4 h-4 text-gray-400 flex-shrink-0" />}>
            <span className="text-gray-500 text-sm">
              Last updated {new Date(salon.updatedAt).toLocaleDateString('en-GB', {
                day: 'numeric', month: 'long', year: 'numeric',
              })}
            </span>
          </InfoRow>
        </div>

        {/* Right: actions */}
        <div>
          <div className="bg-white border border-gray-200 rounded-xl p-5 space-y-3">
            <p className="text-sm font-medium text-gray-700">Manage this listing</p>
            <EditSalonForm salon={salon} />
          </div>
        </div>
      </div>
    </main>
  );
}

function InfoRow({
  icon,
  children,
}: {
  icon: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <div className="flex items-start gap-2">
      {icon}
      <div className="flex flex-col">{children}</div>
    </div>
  );
}
