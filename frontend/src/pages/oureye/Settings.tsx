import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

type Camera = {
  id: number;
  name: string;
  streamName?: string | null;
  configJson?: string | null;
  armed?: boolean;
  sensitivity?: number;
};

async function http<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
  if (!res.ok) {
    const t = await res.text().catch(() => '');
    throw new Error(`HTTP ${res.status}: ${t}`);
  }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return (await res.json()) as T;
  // @ts-ignore
  return undefined as T;
}

export default function Settings() {
  const { id } = useParams();
  const camId = Number(id);
  const nav = useNavigate();

  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | undefined>();

  const [name, setName] = useState('');
  const [streamName, setStreamName] = useState('');
  const [recordOn, setRecordOn] = useState<boolean>(true);
  const [postSec, setPostSec] = useState<number>(5);
  const [roi, setRoi] = useState<string>(''); // JSON string
  const [rules, setRules] = useState<string>(''); // JSON string (고급)

  const [armed, setArmed] = useState<boolean>(true);
  const [sensitivity, setSensitivity] = useState<number>(0.5); // MOTION 0..1
  const [wakeUpSensitivity, setWakeUpSensitivity] = useState<number>(0.5);
  const [suddenMoveSensitivity, setSuddenMoveSensitivity] = useState<number>(0.5);
  const [loudSensitivity, setLoudSensitivity] = useState<number>(0.5);
  const [objectNearSensitivity, setObjectNearSensitivity] = useState<number>(0.5);

  const [previewTick, setPreviewTick] = useState<number>(0);
  const previewRef = useRef<HTMLDivElement | null>(null);
  const [dragging, setDragging] = useState(false);
  const [dragStart, setDragStart] = useState<{ x: number; y: number } | null>(null);
  const [dragEnd, setDragEnd] = useState<{ x: number; y: number } | null>(null);

  const sensitivityPercent = useMemo(
    () => Math.round(sensitivity * 100),
    [sensitivity],
  );

  const sensitivityLabel = useMemo(() => {
    if (sensitivityPercent <= 25) return '낮음 (움직임 많이 필요)';
    if (sensitivityPercent >= 75) return '높음 (작은 움직임도 감지)';
    return '보통';
  }, [sensitivityPercent]);

  useEffect(() => {
    (async () => {
      try {
        const c = await http<Camera>(`/api/cameras/${camId}`);
        setName(c.name);
        setStreamName(c.streamName || '');
        setArmed(c.armed ?? true);
        if (typeof c.sensitivity === 'number' && !Number.isNaN(c.sensitivity)) {
          setSensitivity(Math.min(1, Math.max(0, c.sensitivity)));
        }

        if (c.configJson) {
          try {
            const j = JSON.parse(c.configJson);
            setRecordOn(Boolean(j.recordOn));
            if (j.postSec != null) setPostSec(Number(j.postSec));
            if (j.roi) setRoi(JSON.stringify(j.roi, null, 2));
            if (j.rules) setRules(JSON.stringify(j.rules, null, 2));
            if (typeof j.armed === 'boolean') setArmed(j.armed);
            if (typeof j.sensitivity === 'number') {
              setSensitivity(Math.min(1, Math.max(0, j.sensitivity)));
            }
            if (typeof j.wakeUpSensitivity === 'number') {
              setWakeUpSensitivity(Math.min(1, Math.max(0, j.wakeUpSensitivity)));
            }
            if (typeof j.suddenMoveSensitivity === 'number') {
              setSuddenMoveSensitivity(Math.min(1, Math.max(0, j.suddenMoveSensitivity)));
            }
            if (typeof j.loudSensitivity === 'number') {
              setLoudSensitivity(Math.min(1, Math.max(0, j.loudSensitivity)));
            }
            if (typeof j.objectNearSensitivity === 'number') {
              setObjectNearSensitivity(Math.min(1, Math.max(0, j.objectNearSensitivity)));
            }
          } catch {
            // ignore JSON parse error; user can overwrite
          }
        }
      } catch (e: any) {
        setErr(e?.message || '불러오기 실패');
      } finally {
        setLoading(false);
      }
    })();
  }, [camId]);

  useEffect(() => {
    setPreviewTick(Date.now());
  }, []);

  async function onSave(e: React.FormEvent) {
    e.preventDefault();
    setErr(undefined);
    try {
      const body: any = {
        recordOn,
        postSec,
        streamName: streamName || undefined,
        armed,
        sensitivity,
        wakeUpSensitivity,
        suddenMoveSensitivity,
        loudSensitivity,
        objectNearSensitivity,
      };
      if (roi.trim()) body.roi = JSON.parse(roi);
      if (rules.trim()) body.rules = JSON.parse(rules);
      await http<void>(`/api/cameras/${camId}/config`, {
        method: 'PUT',
        body: JSON.stringify(body),
      });
      alert('저장되었습니다');
    } catch (ex: any) {
      setErr(ex?.message || '저장 실패(ROI/규칙 JSON 확인)');
    }
  }

  function rectToRoi(start: { x: number; y: number }, end: { x: number; y: number }) {
    const container = previewRef.current;
    if (!container) return;
    const rect = container.getBoundingClientRect();
    const x1 = Math.max(0, Math.min(1, (Math.min(start.x, end.x) - rect.left) / rect.width));
    const y1 = Math.max(0, Math.min(1, (Math.min(start.y, end.y) - rect.top) / rect.height));
    const x2 = Math.max(0, Math.min(1, (Math.max(start.x, end.x) - rect.left) / rect.width));
    const y2 = Math.max(0, Math.min(1, (Math.max(start.y, end.y) - rect.top) / rect.height));
    const poly = [
      [x1, y1],
      [x2, y1],
      [x2, y2],
      [x1, y2],
    ];
    setRoi(JSON.stringify(poly, null, 2));
  }

  function onPreviewMouseDown(e: React.MouseEvent) {
    setDragging(true);
    setDragStart({ x: e.clientX, y: e.clientY });
    setDragEnd({ x: e.clientX, y: e.clientY });
  }

  function onPreviewMouseMove(e: React.MouseEvent) {
    if (!dragging) return;
    setDragEnd({ x: e.clientX, y: e.clientY });
  }

  function onPreviewMouseUp() {
    if (dragging && dragStart && dragEnd) {
      rectToRoi(dragStart, dragEnd);
    }
    setDragging(false);
  }

  const roiRect = useMemo(() => {
    try {
      if (!roi.trim()) return null;
      const poly = JSON.parse(roi) as [number, number][];
      if (!Array.isArray(poly) || poly.length === 0) return null;
      const xs = poly.map(p => p[0]);
      const ys = poly.map(p => p[1]);
      return {
        x1: Math.min(...xs),
        y1: Math.min(...ys),
        x2: Math.max(...xs),
        y2: Math.max(...ys),
      };
    } catch {
      return null;
    }
  }, [roi]);

  if (loading) return <div className="p-6">불러오는 중..</div>;

  return (
    <div className="p-6 max-w-3xl mx-auto space-y-4">
      <h1 className="text-xl font-semibold">카메라 설정</h1>
      <div className="text-sm text-gray-600">{name}</div>
      <form onSubmit={onSave} className="space-y-4">
        <div>
          <label className="block text-sm mb-1">스트림 이름</label>
          <input
            className="border rounded px-2 py-1 w-full"
            value={streamName}
            onChange={e => setStreamName(e.target.value)}
            placeholder="기본은 이름과 동일"
          />
        </div>

        <div>
          <label className="block text-sm mb-1">아기 주변 물체 민감도(OBJECT_NEAR_CHILD)</label>
          <input
            className="w-full"
            type="range"
            min={0}
            max={100}
            step={5}
            value={Math.round(objectNearSensitivity * 100)}
            onChange={e => setObjectNearSensitivity(Number(e.target.value) / 100)}
          />
        </div>

        <div className="flex items-center gap-2">
          <label className="text-sm">AI 알람 활성화</label>
          <select
            className="border rounded px-2 py-1"
            value={String(armed)}
            onChange={e => setArmed(e.target.value === 'true')}
          >
            <option value="true">켜기</option>
            <option value="false">끄기</option>
          </select>
        </div>

        <div>
          <label className="block text-sm mb-1">전체 움직임 민감도(MOTION)</label>
          <input
            className="w-full"
            type="range"
            min={0}
            max={100}
            step={5}
            value={sensitivityPercent}
            onChange={e => setSensitivity(Number(e.target.value) / 100)}
          />
          <div className="text-xs text-gray-600 mt-1">
            {sensitivityPercent}% · {sensitivityLabel}
          </div>
        </div>

        <div>
          <label className="block text-sm mb-1">깨기/기상 민감도(WAKE_UP)</label>
          <input
            className="w-full"
            type="range"
            min={0}
            max={100}
            step={5}
            value={Math.round(wakeUpSensitivity * 100)}
            onChange={e => setWakeUpSensitivity(Number(e.target.value) / 100)}
          />
        </div>

        <div>
          <label className="block text-sm mb-1">갑작스러운 움직임(SUDDEN_MOVE)</label>
          <input
            className="w-full"
            type="range"
            min={0}
            max={100}
            step={5}
            value={Math.round(suddenMoveSensitivity * 100)}
            onChange={e => setSuddenMoveSensitivity(Number(e.target.value) / 100)}
          />
        </div>

        <div>
          <label className="block text-sm mb-1">큰 소리 민감도(LOUD)</label>
          <input
            className="w-full"
            type="range"
            min={0}
            max={100}
            step={5}
            value={Math.round(loudSensitivity * 100)}
            onChange={e => setLoudSensitivity(Number(e.target.value) / 100)}
          />
        </div>

        <div className="flex items-center gap-2">
          <label className="text-sm">녹화 활성화</label>
          <select
            className="border rounded px-2 py-1"
            value={String(recordOn)}
            onChange={e => setRecordOn(e.target.value === 'true')}
          >
            <option value="true">켜기</option>
            <option value="false">끄기</option>
          </select>
        </div>

        <div>
          <label className="block text-sm mb-1">이벤트 후 녹화(초)</label>
          <input
            className="border rounded px-2 py-1 w-32"
            type="number"
            min={0}
            value={postSec}
            onChange={e => setPostSec(Number(e.target.value))}
          />
        </div>

        <div>
          <label className="block text-sm mb-1">ROI 설정</label>
          <div
            ref={previewRef}
            className="relative border rounded overflow-hidden inline-block"
            style={{ width: '100%', maxWidth: 400, cursor: 'crosshair' }}
            onMouseDown={onPreviewMouseDown}
            onMouseMove={onPreviewMouseMove}
            onMouseUp={onPreviewMouseUp}
            onMouseLeave={onPreviewMouseUp}
          >
            <img
              src={`/api/worker/preview?ts=${previewTick}`}
              alt="ROI preview"
              className="w-full block select-none"
              draggable={false}
            />
            {/* 기존 ROI가 있으면 노란 박스로 표시 */}
            {roiRect && previewRef.current && (
              <div
                className="absolute border-2 border-yellow-400"
                style={{
                  left: roiRect.x1 * previewRef.current.clientWidth,
                  top: roiRect.y1 * previewRef.current.clientHeight,
                  width: (roiRect.x2 - roiRect.x1) * previewRef.current.clientWidth,
                  height: (roiRect.y2 - roiRect.y1) * previewRef.current.clientHeight,
                }}
              />
            )}
            {/* 드래그 중인 박스 */}
            {dragging && dragStart && dragEnd && previewRef.current && (
              <div
                className="absolute border-2 border-amber-500 bg-amber-500/10"
                style={{
                  left:
                    Math.min(dragStart.x, dragEnd.x) -
                    previewRef.current.getBoundingClientRect().left,
                  top:
                    Math.min(dragStart.y, dragEnd.y) -
                    previewRef.current.getBoundingClientRect().top,
                  width: Math.abs(dragEnd.x - dragStart.x),
                  height: Math.abs(dragEnd.y - dragStart.y),
                }}
              />
            )}
          </div>
          <div className="text-xs text-gray-500 mt-1">
            미리보기 위를 드래그해서 감지 영역(ROI)을 네모로 지정하세요. (노란 박스는 현재 저장된 영역)
          </div>
        </div>

        <div>
          <label className="block text-sm mb-1">규칙(JSON, 고급 설정)</label>
          <textarea
            className="border rounded px-2 py-1 w-full h-28 font-mono"
            value={rules}
            onChange={e => setRules(e.target.value)}
            placeholder="{}"
          />
        </div>

        {err && <div className="text-red-500 text-sm">{err}</div>}

        <div className="flex gap-2">
          <button
            className="px-3 py-1 rounded bg-amber-600 text-white"
            type="submit"
          >
            저장
          </button>
          <button
            className="px-3 py-1 rounded bg-gray-200"
            type="button"
            onClick={() => nav(-1)}
          >
            뒤로
          </button>
        </div>
      </form>
    </div>
  );
}
