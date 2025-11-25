import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

async function http<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, { credentials: 'include', headers: { 'Content-Type': 'application/json' }, ...init });
  if (!res.ok) { const t = await res.text().catch(()=> ''); throw new Error(`HTTP ${res.status}: ${t}`); }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return (await res.json()) as T;
  // @ts-ignore
  return undefined as T;
}

export default function CameraNew(){
  const nav = useNavigate();
  const [name, setName] = useState('');
  const [rtspUrl, setRtspUrl] = useState('');
  const [streamName, setStreamName] = useState('');
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string|undefined>();

  async function onSubmit(e: React.FormEvent){
    e.preventDefault(); setErr(undefined); setLoading(true);
    try{
      const body = { name, rtspUrl, streamName: streamName || undefined } as any;
      const res = await http<{ cameraId: number }>(`/api/cameras`, { method:'POST', body: JSON.stringify(body) });
      nav(`/our-eye?cameraId=${res.cameraId}`);
    }catch(ex:any){ setErr(ex?.message || '등록 실패'); }
    finally{ setLoading(false); }
  }

  return (
    <div className="p-6 max-w-xl mx-auto space-y-4">
      <h1 className="text-xl font-semibold">카메라 등록</h1>
      <form onSubmit={onSubmit} className="space-y-3">
        <div>
          <label className="block text-sm mb-1">이름</label>
          <input className="border rounded px-2 py-1 w-full" value={name} onChange={e=>setName(e.target.value)} required placeholder="c200" />
        </div>
        <div>
          <label className="block text-sm mb-1">RTSP URL</label>
          <input className="border rounded px-2 py-1 w-full" value={rtspUrl} onChange={e=>setRtspUrl(e.target.value)} required placeholder="rtsp://.../c200" />
        </div>
        <div>
          <label className="block text-sm mb-1">스트림 이름(선택)</label>
          <input className="border rounded px-2 py-1 w-full" value={streamName} onChange={e=>setStreamName(e.target.value)} placeholder="기본은 이름과 동일" />
        </div>
        {err && <div className="text-red-500 text-sm">{err}</div>}
        <div className="flex gap-2">
          <button className="px-3 py-1 rounded bg-amber-600 text-white" disabled={loading} type="submit">{loading? '등록 중...' : '등록'}</button>
          <button className="px-3 py-1 rounded bg-gray-200" type="button" onClick={()=>nav(-1)}>취소</button>
        </div>
      </form>
    </div>
  );
}

