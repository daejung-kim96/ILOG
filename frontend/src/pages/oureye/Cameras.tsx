import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

type PageResponse<T> = { items: T[]; page: number; size: number; totalElements: number; totalPages: number };
type Camera = { id: number; name: string; rtspUrl: string; streamName?: string | null; createdAt?: string };

async function http<T>(path: string): Promise<T> {
  const res = await fetch(path, { credentials: 'include' });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export default function Cameras(){
  const [q, setQ] = useState('');
  const [items, setItems] = useState<Camera[]>([]);
  const [err, setErr] = useState<string | null>(null);

  async function load(){
    try{
      const u = new URL('/api/cameras', location.origin);
      u.searchParams.set('page', '0'); u.searchParams.set('size', '50'); if (q) u.searchParams.set('q', q);
      const res = await http<PageResponse<Camera>>(u.toString());
      setItems(res.items);
    }catch(e:any){ setErr(e?.message||'목록 로드 실패'); }
  }

  useEffect(() => { load(); }, []);

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center gap-2">
        <h1 className="text-xl font-semibold">카메라 관리</h1>
        <Link to="/our-eye/cameras/new" className="ml-auto px-3 py-1 rounded bg-amber-600 text-white">카메라 등록</Link>
      </div>
      <div className="flex gap-2">
        <input className="border rounded px-2 py-1" placeholder="검색" value={q} onChange={e=>setQ(e.target.value)} />
        <button className="px-3 py-1 rounded bg-gray-200" onClick={load}>검색</button>
        {err && <span className="text-red-500 text-sm">{err}</span>}
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b">
              <th className="text-left p-2">이름</th>
              <th className="text-left p-2">스트림</th>
              <th className="text-left p-2">생성일</th>
              <th className="text-left p-2">액션</th>
            </tr>
          </thead>
          <tbody>
            {items.map(c => (
              <tr key={c.id} className="border-b">
                <td className="p-2">{c.name}</td>
                <td className="p-2">{c.streamName || ''}</td>
                <td className="p-2">{c.createdAt ? new Date(c.createdAt).toLocaleString() : ''}</td>
                <td className="p-2 flex gap-2">
                  <Link className="px-2 py-1 rounded bg-gray-200" to={`/our-eye?cameraId=${c.id}`}>보기</Link>
                  <Link className="px-2 py-1 rounded bg-gray-200" to={`/our-eye/settings/${c.id}`}>설정</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

