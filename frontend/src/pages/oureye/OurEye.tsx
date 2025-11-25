import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';

// ===== Types & tiny fetcher (local to this file) =====
type PageResponse<T> = { items: T[]; page: number; size: number; totalElements: number; totalPages: number };
type Camera = { id: number; name: string; rtspUrl: string; streamName?: string | null };
type PlayUrl = { hlsUrl: string; whepUrl: string; webrtcTestUrl?: string };
type ShortsItem = { id: number; cameraId: number; status: 'READY'|'PROCESSING'|'DONE'|'FAILED'; clipUrl?: string|null; durationSec?: number|null; createdAt: string };
type AiSignals = { motion?: number|null; faceConf?: number|null; personConf?: number|null; lastFrameTs?: number|null };

async function http<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, { credentials: 'include', headers: { 'Content-Type': 'application/json' }, ...init });
  if (!res.ok) { throw new Error(`HTTP ${res.status}`); }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return (await res.json()) as T;
  // @ts-ignore
  return undefined as T;
}

// ===== Minimal HLS player (inlined) =====
declare global { interface Window { Hls?: any } }
function HlsPlayer({ src }: { src: string }){
  const videoRef = useRef<HTMLVideoElement>(null);
  const [ready, setReady] = useState(false);
  useEffect(() => {
    if (!window.Hls && !document.getElementById('hls-cdn')){
      const s = document.createElement('script'); s.id = 'hls-cdn'; s.src = 'https://cdn.jsdelivr.net/npm/hls.js@latest'; s.onload = () => setReady(true); document.body.appendChild(s);
    } else { setReady(true); }
  }, []);
  useEffect(() => {
    const v = videoRef.current; if (!v || !ready) return;
    if (v.canPlayType('application/vnd.apple.mpegurl')) { v.src = src; return; }
    if (window.Hls){ const hls = new window.Hls({ enableWorker:true, lowLatencyMode:true }); hls.loadSource(src); hls.attachMedia(v); return () => { try{hls.destroy();}catch{}}; }
  }, [src, ready]);
  return <video ref={videoRef} playsInline autoPlay muted controls style={{ width:'100%', borderRadius:8, background:'#000' }} />;
}

// ===== Minimal WHEP player (inlined) =====
function WhepPlayer({ endpoint, onError }: { endpoint: string; onError?: () => void }){
  const vref = useRef<HTMLVideoElement>(null);
  const pcRef = useRef<RTCPeerConnection|null>(null);
  const [state, setState] = useState<'connecting'|'playing'|'error'>('connecting');
  useEffect(() => {
    let stopped = false;
    async function start(){
      try{
        const pc = new RTCPeerConnection({ iceServers: [] }); pcRef.current = pc;
        pc.addTransceiver('video', { direction:'recvonly' }); pc.addTransceiver('audio', { direction:'recvonly' });
        pc.ontrack = (e) => { if (vref.current) vref.current.srcObject = e.streams[0]; };
        const offer = await pc.createOffer(); await pc.setLocalDescription(offer);
        await new Promise<void>((res)=>{ if (pc.iceGatheringState==='complete') return res(); pc.onicegatheringstatechange = () => { if (pc.iceGatheringState==='complete') res(); }; });
        const sdp = pc.localDescription?.sdp || '';
        const resp = await fetch(endpoint, { method:'POST', headers:{ 'Content-Type':'application/sdp' }, body:sdp });
        if (!resp.ok) throw new Error('WHEP POST failed');
        const answer = await resp.text(); await pc.setRemoteDescription({ type:'answer', sdp:answer });
        if (!stopped) setState('playing');
      }catch(err){ setState('error'); onError && onError(); }
    }
    start();
    return () => { stopped = true; try{ pcRef.current?.getSenders().forEach(s=>s.track&&s.track.stop()); pcRef.current?.close(); }catch{} pcRef.current=null; };
  }, [endpoint]);
  return (
    <div>
      <video ref={vref} playsInline autoPlay muted controls style={{ width:'100%', borderRadius:8, background:'#000' }} />
      <div style={{ fontSize:12, color:'#888', marginTop:4 }}>state: {state}</div>
    </div>
  );
}

// ===== Page =====
type Mode = 'live' | 'shorts';
const OurEye = () => {
  const nav = useNavigate();
  const loc = useLocation();
  const [cameras, setCameras] = useState<Camera[]>([]);
  const [cameraId, setCameraId] = useState<number | null>(null);
  const [mode, setMode] = useState<Mode>('live');
  const [play, setPlay] = useState<PlayUrl | null>(null);
  const [forceHls, setForceHls] = useState(false);
  const [readyShorts, setReadyShorts] = useState<ShortsItem[]>([]);
  const [doneShorts, setDoneShorts] = useState<ShortsItem[]>([]);
  const [selectedShort, setSelectedShort] = useState<ShortsItem | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [aiSignals, setAiSignals] = useState<AiSignals | null>(null);
  const [previewTick, setPreviewTick] = useState<number>(0);

  // pick cameraId from query (?cameraId=)
  useEffect(() => {
    const sp = new URLSearchParams(loc.search);
    const qid = sp.get('cameraId');
    if (qid) {
      const n = Number(qid);
      if (!Number.isNaN(n)) setCameraId(n);
    }
  }, [loc.search]);

  useEffect(() => { (async () => {
    try { const u = new URL('/api/cameras', location.origin); u.searchParams.set('page','0'); u.searchParams.set('size','50');
      const res = await http<PageResponse<Camera>>(u.toString()); setCameras(res.items); if (!cameraId && res.items.length>0) setCameraId(res.items[0].id);
    } catch(e:any){ setErr(e?.message||'카메라 목록 실패'); }
  })(); }, []);

  useEffect(() => { if (!cameraId) return; (async () => {
    try { const p = await http<PlayUrl>(`/api/cameras/${cameraId}/play`); setPlay(p); setForceHls(false); } catch(e:any){ setErr(e?.message||'재생 URL 실패'); }
  })(); (async () => {
    try { const r = await http<PageResponse<ShortsItem>>(`/api/shorts?cameraId=${cameraId}&status=READY&page=0&size=10`); setReadyShorts(r.items); } catch{}
    try { const d = await http<PageResponse<ShortsItem>>(`/api/shorts?cameraId=${cameraId}&status=DONE&page=0&size=10`); setDoneShorts(d.items); } catch{}
  })(); setSelectedShort(null); setMode('live'); }, [cameraId]);

  // poll AI worker debug signals + preview when in live mode
  useEffect(() => {
    if (!cameraId || mode !== 'live') return;
    let cancelled = false;
    const tick = async () => {
      try {
        const s = await http<AiSignals>('/api/worker/signals');
        if (!cancelled) setAiSignals(s);
      } catch {
        // ignore debug errors
      }
      if (!cancelled) {
        setPreviewTick(Date.now());
      }
    };
    tick();
    const id = setInterval(tick, 1000);
    return () => { cancelled = true; clearInterval(id); };
  }, [cameraId, mode]);

  const player = useMemo(() => {
    if (mode==='live'){
      if (!play) return <div className="text-sm text-gray-500">재생 정보를 불러오는 중...</div>;
      if (!forceHls && play.whepUrl){ return <WhepPlayer endpoint={play.whepUrl} onError={()=>setForceHls(true)} />; }
      return <HlsPlayer src={play.hlsUrl} />;
    }
    if (!selectedShort) return <div className="text-sm text-gray-500">쇼츠를 선택하세요</div>;
    const url = selectedShort.clipUrl || '';
    if (url.includes('.m3u8')) return <HlsPlayer src={url} />;
    return <video src={url} controls playsInline style={{ width:'100%', borderRadius:8, background:'#000' }} />;
  }, [mode, play, selectedShort, forceHls]);

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-2">
        <h1 className="text-2xl font-bold">우리 아이 보기</h1>
        <div className="ml-auto flex items-center gap-2">
          <Link className="px-3 py-1 rounded bg-gray-200" to="/our-eye/cameras">카메라 관리</Link>
          <Link className="px-3 py-1 rounded bg-gray-200" to="/our-eye/cameras/new">카메라 등록</Link>
          {cameraId && (
            <Link className="px-3 py-1 rounded bg-gray-200" to={`/our-eye/settings/${cameraId}`}>이 카메라 설정</Link>
          )}
        </div>
      </div>

      <div className="flex items-center gap-3">
        <select className="border rounded px-2 py-1" value={cameraId ?? ''} onChange={(e)=>{ const id = Number(e.target.value); setCameraId(id); const sp = new URLSearchParams(loc.search); sp.set('cameraId', String(id)); nav({ pathname: loc.pathname, search: sp.toString() }, { replace: true }); }}>
          {cameras.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <button className={`px-3 py-1 rounded ${mode==='live'?'bg-amber-600 text-white':'bg-gray-200'}`} onClick={()=>{ setMode('live'); setSelectedShort(null); }}>실시간 영상</button>
        {play?.webrtcTestUrl && (
          <a className="ml-auto text-blue-600 underline text-sm" href={play.webrtcTestUrl} target="_blank" rel="noreferrer">WebRTC 테스트</a>
        )}
        {err && <span className="text-red-500 text-sm">{err}</span>}
      </div>

      <div className="border rounded-lg p-3">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            {player}
          </div>
          {mode === 'live' && (
            <div className="w-full md:w-64 space-y-2 text-xs text-gray-700">
              <div className="font-semibold text-sm">AI 디버그</div>
              <div className="border rounded bg-black/80">
                {previewTick ? (
                  <img
                    src={`/api/worker/preview?ts=${previewTick}`}
                    alt="AI preview"
                    className="w-full rounded"
                  />
                ) : (
                  <div className="text-gray-400 p-4 text-center">프리뷰 없음</div>
                )}
              </div>
              <div className="space-y-1">
                <div>motion: {aiSignals?.motion != null ? aiSignals.motion.toFixed(3) : '-'}</div>
                <div>faceConf: {aiSignals?.faceConf != null ? aiSignals.faceConf.toFixed(2) : '-'}</div>
                <div>personConf: {aiSignals?.personConf != null ? aiSignals.personConf.toFixed(2) : '-'}</div>
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="border rounded-lg p-3 space-y-4">
        <h2 className="font-semibold">임시 저장 영상</h2>
        <div className="flex gap-3 overflow-x-auto pb-2">
          {readyShorts.length===0 && <div className="text-sm text-gray-500">목록이 없습니다</div>}
          {readyShorts.map(s => (
            <button key={s.id} className={`border rounded p-2 text-left`} style={{ minWidth: '10rem' }} onClick={()=>{ setSelectedShort(s); setMode('shorts'); }}>
              <div className="text-xs text-gray-500">{new Date(s.createdAt).toLocaleString()}</div>
              <div className="truncate">{s.clipUrl || 'clip'}</div>
              <div className="text-xs">{s.durationSec ?? ''}</div>
            </button>
          ))}
        </div>

        <h2 className="font-semibold">로컬 저장 영상</h2>
        <div className="flex gap-3 overflow-x-auto pb-2">
          {doneShorts.length===0 && <div className="text-sm text-gray-500">목록이 없습니다</div>}
          {doneShorts.map(s => (
            <button key={s.id} className={`border rounded p-2 text-left`} style={{ minWidth: '10rem' }} onClick={()=>{ setSelectedShort(s); setMode('shorts'); }}>
              <div className="text-xs text-gray-500">{new Date(s.createdAt).toLocaleString()}</div>
              <div className="truncate">{s.clipUrl || 'clip'}</div>
              <div className="text-xs">{s.durationSec ?? ''}</div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default OurEye;
