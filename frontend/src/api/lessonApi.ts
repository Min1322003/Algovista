import type { LessonSession, LessonSummary, SubmitResponse } from "../types/lesson";
const API = (import.meta.env.VITE_API_BASE_URL || "/api/lessons").replace(/\/$/, "");
function userId() {
  const key = "algovista_user_id";
  let id = localStorage.getItem(key);
  if (!id) { id = `learner-${crypto.randomUUID()}`; localStorage.setItem(key, id); }
  return id;
}
async function request<T>(path:string, init:RequestInit = {}):Promise<T> {
  let response:Response;
  try {
    response = await fetch(`${API}${path}`, { ...init, headers: { "Content-Type":"application/json", "X-User-Id":userId(), ...(init.headers || {}) } });
  } catch {
    throw new Error(`Cannot reach ${API}. Start the Spring Boot backend and make sure CORS allows the frontend origin.`);
  }
  const text = await response.text();
  let data:unknown = null;
  if (text) { try { data = JSON.parse(text); } catch { data = text; } }
  if (!response.ok) {
    const message = typeof data === "object" && data !== null && "error" in data ? String((data as {error:unknown}).error) : typeof data === "string" && data ? data : `Request failed (${response.status})`;
    throw new Error(message);
  }
  return data as T;
}
export const lessonApi = {
  list:()=>request<LessonSummary[]>(""),
  start:(id:string)=>request<LessonSession>(`/${encodeURIComponent(id)}/start`,{method:"POST"}),
  session:(id:string)=>request<LessonSession>(`/${encodeURIComponent(id)}/session`),
  submit:(id:string,activityId:number,answer:string[])=>request<SubmitResponse>(`/${encodeURIComponent(id)}/submit`,{method:"POST",body:JSON.stringify({activityId,answer})}),
  restart:(id:string)=>request<LessonSession>(`/${encodeURIComponent(id)}/restart`,{method:"POST"}),
};
