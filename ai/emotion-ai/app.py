import os, re, math, json, time, logging, hashlib
from pathlib import Path
import numpy as np
import torch, torch.nn.functional as F
import requests
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv
from transformers import AutoTokenizer, AutoModelForSequenceClassification

# ==== 기본 설정/환경 ====
load_dotenv()  # .env 로딩

logging.basicConfig(
    level=logging.INFO,  # 필요시 DEBUG
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger("emotion")

logger.info("__file__ = %s", __file__)

# ===== 로컬 요약 (LLM 실패/빈 요약일 때 대체) =====
def summarize_local(dist: dict) -> str:
    ko = {"angry":"분노","sad":"슬픔","anxious":"불안","hurt":"상처","embarrass":"머쓱함","happy":"행복"}
    top = sorted(dist.items(), key=lambda x: x[1], reverse=True)
    k1, v1 = top[0]; k2, v2 = top[1]
    gap = v1 - v2  # 상대 격차

    if gap >= 0.05:
        return f"전반적으로 {ko[k1]} 감정이 두드러집니다."
    if (v1 + v2) >= 0.45:
        return f"{ko[k1]}과(와) {ko[k2]} 감정이 함께 나타납니다."
    return "감정 분포가 고르게 나타나 전반적으로 복합적인 상태입니다."

# ===== 파일 기준 경로 안전화 =====
BASE_DIR = Path(__file__).parent.resolve()
_model_dir_env = os.getenv("MODEL_DIR", "out_roberta_60cls/checkpoint-6843")
_model_dir = Path(_model_dir_env)
if not _model_dir.is_absolute():
    _model_dir = (BASE_DIR / _model_dir).resolve()
if not _model_dir.exists():
    raise FileNotFoundError(f"MODEL_DIR not found: {_model_dir}")

MODEL_DIR = str(_model_dir)  # HF는 str 경로를 받음
MODEL_VERSION = os.getenv("MODEL_VERSION", "roberta-kr-6cls-ctx:ckpt-6843")
LLM_API_BASE = os.getenv("LLM_API_BASE", "https://gms.ssafy.io/gmsapi/api.openai.com/v1")
LLM_API_KEY  = os.getenv("LLM_API_KEY", "")
LLM_MODEL    = os.getenv("LLM_MODEL", "gpt-4o")
PIPELINE_ALPHA = float(os.getenv("PIPELINE_ALPHA", "0.7"))
MAX_LEN = int(os.getenv("MAX_LEN", "256"))
os.environ["TOKENIZERS_PARALLELISM"] = "false"

LABELS6 = ["angry","sad","anxious","hurt","embarrass","happy"]

logger.info("LLM base=%s model=%s key_loaded=%s",
            LLM_API_BASE, LLM_MODEL, bool(LLM_API_KEY))

# ==== 문장 분리(kss 우선) ====
try:
    import kss
    def split_sents(t: str):
        return [s.strip() for s in kss.split_sentences(t) if s.strip()]
except ImportError:
    _pat = re.compile(r"\n+|(?<=[.!?])\s+|(?<=[다요죠])\s+(?=[A-Z가-힣])")
    def split_sents(t: str):
        return [s.strip() for s in _pat.split(t) if s.strip()]

# ==== 로컬 모델 로드 ====
tok = AutoTokenizer.from_pretrained(MODEL_DIR, local_files_only=True)
mdl = AutoModelForSequenceClassification.from_pretrained(MODEL_DIR, local_files_only=True).eval()

# 60 → 6 합산 (60클래스 모델용)
E_CODES = [f"E{i}" for i in range(10,70)]
LABEL2ID = {e:i for i,e in enumerate(E_CODES)}
def _idx_range(s, e): return [LABEL2ID[f"E{i}"] for i in range(s, e+1)]
GROUPS_6 = {
    "angry":     _idx_range(10,19),
    "sad":       _idx_range(20,29),
    "anxious":   _idx_range(30,39),
    "hurt":      _idx_range(40,49),
    "embarrass": _idx_range(50,59),
    "happy":     _idx_range(60,69),
}

def p60_to_p6(p60: np.ndarray) -> dict:
    g = {k: float(p60[idxs].sum()) for k, idxs in GROUPS_6.items()}
    s = sum(g.values())
    return {k: (v/s if s>0 else 0.0) for k,v in g.items()}

def normalize(d: dict) -> dict:
    s = sum(d.values())
    return {k: (v/s if s>0 else 0.0) for k,v in d.items()}

@torch.no_grad()
def local_distribution(text: str) -> dict:
    sents = split_sents(text) or [text]
    probs = []
    for i in range(0, len(sents), 32):
        batch = sents[i:i+32]
        enc = tok(batch, return_tensors="pt", padding=True, truncation=True, max_length=MAX_LEN)
        logits = mdl(**enc).logits
        p = F.softmax(logits, dim=-1).cpu().numpy()  # [B, C]
        probs.append(p)
    p_mat = np.concatenate(probs, axis=0)  # [N, C]

    if p_mat.shape[1] == 60:
        p6 = np.stack([list(p60_to_p6(row).values()) for row in p_mat], axis=0)  # [N,6]
    elif p_mat.shape[1] == 6:
        p6 = p_mat
    else:
        raise RuntimeError(f"Unexpected num_labels: {p_mat.shape[1]}")

    lengths = np.array([len(tok(s)["input_ids"]) for s in sents], dtype=np.float32)
    weights = lengths / (lengths.sum() if lengths.sum() > 0 else 1.0)
    agg = (p6 * weights[:, None]).sum(axis=0)
    return {LABELS6[i]: float(agg[i]) for i in range(6)}

# ==== LLM 호출 (엄격 JSON) ====
LLM_SYS_PROMPT = """
You are a Korean diary sentiment rater.

Return STRICT JSON only in this exact shape:
{
 "distribution":{"angry":0.0,"sad":0.0,"anxious":0.0,"hurt":0.0,"embarrass":0.0,"happy":0.0},
 "summary":"..."
}

Rules:
- Output MUST be valid JSON (no markdown, no extra text).
- distribution keys must be exactly angry,sad,anxious,hurt,embarrass,happy.
- Values must be non-negative and SUM TO 1.
- "summary" is a 1–2 sentence KOREAN emotional summary of the diary (dominant feelings and brief reason).
- Do NOT give advice, diagnosis, or instructions. Describe feelings only.
"""

def analyze_llm(text: str) -> dict:
    payload = {
        "model": LLM_MODEL,
        "response_format": {"type": "json_object"},
        "messages": [
            {"role":"system","content": LLM_SYS_PROMPT},
            {"role":"user","content": f"Text:\n<<<{text}>>>"}]
    }
    headers = {"Authorization": f"Bearer {LLM_API_KEY}", "Content-Type": "application/json"}

    t0 = time.time()
    r = requests.post(f"{LLM_API_BASE}/chat/completions",
                      headers=headers, data=json.dumps(payload), timeout=30)
    dt = time.time() - t0
    logger.info("LLM POST /chat/completions -> %s (%.2fs)", r.status_code, dt)
    r.raise_for_status()

    # 안전 파싱 (빈 내용/비JSON 대응)
    try:
        content = r.json()["choices"][0]["message"]["content"]
        if not content or not isinstance(content, str):
            raise ValueError("empty LLM content")
        obj = json.loads(content)
    except Exception as e:
        logger.exception("LLM parse failed: %s", e)
        raise

    dist = {k: float(obj.get("distribution", {}).get(k, 0.0)) for k in LABELS6}
    dist = normalize(dist)
    summary = (obj.get("summary", "") or "").strip()
    logger.info("LLM summary length: %d chars", len(summary))
    return {"distribution": dist, "summary": summary}

# ==== 합성 ====
def merge(llm: dict, local: dict, alpha: float = PIPELINE_ALPHA) -> dict:
    m = {k: alpha*llm[k] + (1-alpha)*local[k] for k in LABELS6}
    return normalize(m)

# ==== API 모델 (OpenAPI 스키마 안정) ====
class Distribution(BaseModel):
    angry: float
    sad: float
    anxious: float
    hurt: float
    embarrass: float
    happy: float

class AnalyzeIn(BaseModel):
    text: str

class AnalyzeOut(BaseModel):
    distribution: Distribution
    summary: str
    model_version: str
    llm_model: str
    source: str
    confidence: float

app = FastAPI(
    title="Diary Sentiment (LLM-first)",
    version=f"{MODEL_VERSION}+{LLM_MODEL}",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
)

@app.get("/healthz")
def health():
    return {"ok": True, "model_version": MODEL_VERSION, "llm_model": LLM_MODEL}

@app.post("/analyze", response_model=AnalyzeOut)
def analyze(req: AnalyzeIn):
    # 1) LLM 시도
    llm_ok = True
    try:
        llm = analyze_llm(req.text)
        if not llm.get("summary"):  # 요약이 빈 문자열이면 실패 취급
            llm_ok = False
    except Exception as e:
        logger.warning("LLM unavailable -> using uniform prior: %s", e)
        llm = {"distribution": {k: 1/6 for k in LABELS6}, "summary": ""}
        llm_ok = False

    # 2) 로컬 분포
    try:
        local = local_distribution(req.text)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"local_failed: {e}")

    # 3) 병합: LLM 실패 시에는 α=0.0(로컬만 사용)
    alpha = PIPELINE_ALPHA if llm_ok else 0.0
    merged = merge(llm["distribution"], local, alpha)
    conf = max(merged.values())

    # 4) 요약: LLM 요약 없으면 로컬 요약으로 대체
    summary = llm.get("summary") or summarize_local(merged)
    source  = "merge_llm_first" if llm_ok and llm.get("summary") else "local_fallback"
    logger.info("analyze done: source=%s alpha=%.2f confidence=%.3f", source, alpha, conf)

    return AnalyzeOut(
        distribution=Distribution(**merged),
        summary=summary,
        model_version=MODEL_VERSION,
        llm_model=LLM_MODEL,
        source=source,
        confidence=conf,
    )

