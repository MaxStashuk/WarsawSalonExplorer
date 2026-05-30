'use client';

import { useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { patchSalon, ApiError, type Salon } from '@/lib/api';

const INPUT_CLS =
  'w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500';
const LABEL_CLS = 'block text-sm font-medium text-gray-700 mb-1';

export function EditSalonForm({ salon }: { salon: Salon }) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const router = useRouter();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [name, setName] = useState(salon.name);
  const [address, setAddress] = useState(salon.address);
  const [district, setDistrict] = useState(salon.district);
  const [services, setServices] = useState(salon.services?.join(', ') ?? '');
  const [priceLevel, setPriceLevel] = useState(salon.priceLevel?.toString() ?? '');

  const open = () => {
    setName(salon.name);
    setAddress(salon.address);
    setDistrict(salon.district);
    setServices(salon.services?.join(', ') ?? '');
    setPriceLevel(salon.priceLevel?.toString() ?? '');
    setError(null);
    dialogRef.current?.showModal();
  };

  const close = () => dialogRef.current?.close();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await patchSalon(salon.id, {
        name,
        address,
        district,
        ...(services !== '' && {
          services: services.split(',').map((s) => s.trim()).filter(Boolean),
        }),
        ...(priceLevel !== '' && { priceLevel: parseInt(priceLevel) }),
      });
      close();
      toast.success('Salon updated');
      router.refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button
        onClick={open}
        className="w-full bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors font-medium"
      >
        Edit details
      </button>

      <dialog
        ref={dialogRef}
        className="rounded-xl shadow-2xl border-0 p-0 w-[min(90vw,28rem)] max-h-[90vh] overflow-y-auto"
        onCancel={(e) => { if (loading) e.preventDefault(); }}
      >
        <div className="p-6">
          <div className="flex justify-between items-center mb-5">
            <h2 className="text-lg font-semibold text-gray-900">Edit salon</h2>
            <button
              type="button"
              onClick={close}
              disabled={loading}
              className="text-gray-400 hover:text-gray-600 disabled:opacity-50 text-xl leading-none"
              aria-label="Close"
            >
              ✕
            </button>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-md text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className={LABEL_CLS}>Name</label>
              <input value={name} onChange={(e) => setName(e.target.value)} className={INPUT_CLS} required />
            </div>
            <div>
              <label className={LABEL_CLS}>Address</label>
              <input value={address} onChange={(e) => setAddress(e.target.value)} className={INPUT_CLS} required />
            </div>
            <div>
              <label className={LABEL_CLS}>District</label>
              <input value={district} onChange={(e) => setDistrict(e.target.value)} className={INPUT_CLS} required />
            </div>
            <div>
              <label className={LABEL_CLS}>
                Services{' '}
                <span className="text-gray-400 font-normal">(comma-separated)</span>
              </label>
              <input
                value={services}
                onChange={(e) => setServices(e.target.value)}
                placeholder="hair_care, beauty_salon, …"
                className={INPUT_CLS}
              />
            </div>
            <div>
              <label className={LABEL_CLS}>Price level</label>
              <select value={priceLevel} onChange={(e) => setPriceLevel(e.target.value)} className={INPUT_CLS}>
                <option value="">Unknown</option>
                <option value="1">€ — Inexpensive</option>
                <option value="2">€€ — Moderate</option>
                <option value="3">€€€ — Expensive</option>
                <option value="4">€€€€ — Very expensive</option>
              </select>
            </div>

            <div className="flex gap-3 pt-2">
              <button
                type="submit"
                disabled={loading}
                className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
              >
                {loading ? 'Saving…' : 'Save changes'}
              </button>
              <button
                type="button"
                onClick={close}
                disabled={loading}
                className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 transition-colors"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </dialog>
    </>
  );
}
