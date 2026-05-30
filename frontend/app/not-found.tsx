import Link from 'next/link';

export default function NotFound() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-4 text-center px-4">
      <h1 className="text-2xl font-bold text-gray-900">Salon not found</h1>
      <p className="text-gray-500">This salon doesn&apos;t exist or has been removed.</p>
      <Link href="/" className="text-blue-600 hover:underline text-sm">
        ← Back to all salons
      </Link>
    </div>
  );
}
