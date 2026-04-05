export type ApiResponse<T> = {
  code: string
  message: string
  data: T
  traceId?: string | null
}

export type DocketOverview = {
  id: string
  title: string
  state: string
  mode: string
  priority: string
  riskLevel: string
  currentOwner: string
  createdAt: string
  updatedAt: string
}

export type DocketDetail = DocketOverview & {
  edictRaw: string
  summary?: string | null
  lastProgressAt?: string | null
  retryCount: number
  escalationLevel: number
  availableTransitions: string[]
}

export type TimelineEvent = {
  id: number
  docketId: string
  eventType: string
  actorType: string
  actorId: string
  payload: Record<string, unknown>
  createdAt: string
}

export type SenateOpinion = {
  id: number
  roleCode: string
  stance: string
  summary: string
  details?: string | null
  generatedAt: string
}

export type SenateSession = {
  id: string
  docketId: string
  status: string
  recommendedMotion?: string | null
  consensus: string[]
  disputes: string[]
  opinions: SenateOpinion[]
  openedAt: string
  closedAt?: string | null
}

export type Delegation = {
  id: string
  docketId: string
  roleCode: string
  objective: string
  status: string
  dependsOn: string[]
  executionTaskId?: string | null
  assignedAt: string
  completedAt?: string | null
}

export type ExecutionTask = {
  id: string
  docketId: string
  delegationId: string
  roleCode: string
  progressPercent: number
  status: string
  blockReason?: string | null
  outputSummary?: string | null
  updatedAt: string
}

const API_BASE = 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  const payload = (await response.json()) as ApiResponse<T>
  if (!response.ok || payload.code !== 'OK') {
    throw new Error(payload.message || 'Request failed')
  }
  return payload.data
}

export const api = {
  listDockets: () => request<DocketOverview[]>('/api/dockets'),
  createDocket: (body: { edictRaw: string; mode: string; priority?: string }) =>
    request<DocketDetail>('/api/dockets', { method: 'POST', body: JSON.stringify(body) }),
  getDocket: (id: string) => request<DocketDetail>(`/api/dockets/${id}`),
  getTimeline: (id: string) => request<TimelineEvent[]>(`/api/dockets/${id}/timeline`),
  openSenate: (id: string) => request<SenateSession>(`/api/dockets/${id}/senate/open`, { method: 'POST' }),
  getSenate: (id: string) => request<SenateSession>(`/api/dockets/${id}/senate`),
  submitOpinion: (id: string, body: { roleCode: string; stance: string; summary: string; details?: string }) =>
    request<SenateSession>(`/api/dockets/${id}/senate/opinions`, { method: 'POST', body: JSON.stringify(body) }),
  tribuneApprove: (id: string, reason?: string) =>
    request<DocketDetail>(`/api/dockets/${id}/tribune/approve`, { method: 'POST', body: JSON.stringify({ reason }) }),
  tribuneReject: (id: string, reason?: string) =>
    request<DocketDetail>(`/api/dockets/${id}/tribune/reject`, { method: 'POST', body: JSON.stringify({ reason }) }),
  tribuneReturn: (id: string, reason?: string) =>
    request<DocketDetail>(`/api/dockets/${id}/tribune/return`, { method: 'POST', body: JSON.stringify({ reason }) }),
  caesarApprove: (id: string, comment?: string) =>
    request<DocketDetail>(`/api/dockets/${id}/caesar/approve`, { method: 'POST', body: JSON.stringify({ comment }) }),
  caesarReject: (id: string, comment?: string) =>
    request<DocketDetail>(`/api/dockets/${id}/caesar/reject`, { method: 'POST', body: JSON.stringify({ comment }) }),
  caesarOverride: (id: string, comment?: string) =>
    request<DocketDetail>(`/api/dockets/${id}/caesar/override`, { method: 'POST', body: JSON.stringify({ comment }) }),
  caesarRestrict: (id: string, comment?: string, constraints?: string[]) =>
    request<DocketDetail>(`/api/dockets/${id}/caesar/restrict`, {
      method: 'POST',
      body: JSON.stringify({ comment, constraints }),
    }),
  listDelegations: (id: string) => request<Delegation[]>(`/api/dockets/${id}/delegations`),
  createDelegations: (
    id: string,
    body: { items: Array<{ roleCode: string; objective: string; dependsOn?: string[] }> },
  ) => request<Delegation[]>(`/api/dockets/${id}/delegate`, { method: 'POST', body: JSON.stringify(body) }),
  listExecutionTasks: (id: string) => request<ExecutionTask[]>(`/api/dockets/${id}/execution-tasks`),
  progressExecutionTask: (id: string, body: { progressPercent: number; outputSummary?: string }) =>
    request<ExecutionTask>(`/api/execution-tasks/${id}/progress`, { method: 'POST', body: JSON.stringify(body) }),
  blockExecutionTask: (id: string, body: { blockReason: string; outputSummary?: string }) =>
    request<ExecutionTask>(`/api/execution-tasks/${id}/block`, { method: 'POST', body: JSON.stringify(body) }),
  completeExecutionTask: (id: string, body: { outputSummary?: string }) =>
    request<ExecutionTask>(`/api/execution-tasks/${id}/complete`, { method: 'POST', body: JSON.stringify(body) }),
}
