import { FormEvent, useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  api,
  Delegation,
  DocketDetail,
  ExecutionTask,
  SenateSession,
  TimelineEvent,
} from '../api'

const senatorRoles = ['SENATOR_STRATEGOS', 'SENATOR_JURIS', 'SENATOR_FISCUS']
const delegationRoles = ['LEGATUS', 'PRAETOR', 'AEDILE', 'QUAESTOR', 'SCRIBA', 'GOVERNOR']

function DocketDetailPage() {
  const { docketId = '' } = useParams()
  const [docket, setDocket] = useState<DocketDetail | null>(null)
  const [timeline, setTimeline] = useState<TimelineEvent[]>([])
  const [senate, setSenate] = useState<SenateSession | null>(null)
  const [delegations, setDelegations] = useState<Delegation[]>([])
  const [tasks, setTasks] = useState<ExecutionTask[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const [tribuneReason, setTribuneReason] = useState('')
  const [caesarComment, setCaesarComment] = useState('')
  const [caesarConstraints, setCaesarConstraints] = useState('')
  const [delegationForm, setDelegationForm] = useState({ roleCode: 'LEGATUS', objective: '', dependsOn: '' })
  const [senateForm, setSenateForm] = useState({ roleCode: senatorRoles[0], stance: 'SUPPORT', summary: '', details: '' })

  async function load() {
    try {
      setLoading(true)
      setError(null)
      const [detail, events, delegationList, executionTasks] = await Promise.all([
        api.getDocket(docketId),
        api.getTimeline(docketId),
        api.listDelegations(docketId),
        api.listExecutionTasks(docketId),
      ])

      setDocket(detail)
      setTimeline(events)
      setDelegations(delegationList)
      setTasks(executionTasks)

      try {
        const session = await api.getSenate(docketId)
        setSenate(session)
      } catch {
        setSenate(null)
      }
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [docketId])

  const taskByDelegation = useMemo(() => {
    return new Map(tasks.map((task) => [task.delegationId, task]))
  }, [tasks])

  async function submitSenateOpinion(event: FormEvent) {
    event.preventDefault()
    await api.submitOpinion(docketId, senateForm)
    setSenateForm({ roleCode: senatorRoles[0], stance: 'SUPPORT', summary: '', details: '' })
    await load()
  }

  async function submitDelegation(event: FormEvent) {
    event.preventDefault()
    await api.createDelegations(docketId, {
      items: [
        {
          roleCode: delegationForm.roleCode,
          objective: delegationForm.objective,
          dependsOn: delegationForm.dependsOn
            ? delegationForm.dependsOn.split(',').map((item) => item.trim()).filter(Boolean)
            : [],
        },
      ],
    })
    setDelegationForm({ roleCode: 'LEGATUS', objective: '', dependsOn: '' })
    await load()
  }

  async function handleTribune(action: 'approve' | 'reject' | 'return') {
    if (action === 'approve') await api.tribuneApprove(docketId, tribuneReason)
    if (action === 'reject') await api.tribuneReject(docketId, tribuneReason)
    if (action === 'return') await api.tribuneReturn(docketId, tribuneReason)
    setTribuneReason('')
    await load()
  }

  async function handleCaesar(action: 'approve' | 'reject' | 'override' | 'restrict') {
    const constraints = caesarConstraints
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)

    if (action === 'approve') await api.caesarApprove(docketId, caesarComment)
    if (action === 'reject') await api.caesarReject(docketId, caesarComment)
    if (action === 'override') await api.caesarOverride(docketId, caesarComment)
    if (action === 'restrict') await api.caesarRestrict(docketId, caesarComment, constraints)
    setCaesarComment('')
    setCaesarConstraints('')
    await load()
  }

  async function openSenate() {
    await api.openSenate(docketId)
    await load()
  }

  async function updateTaskProgress(taskId: string, progressPercent: number) {
    await api.progressExecutionTask(taskId, { progressPercent })
    await load()
  }

  async function completeTask(taskId: string) {
    await api.completeExecutionTask(taskId, { outputSummary: 'Marked complete from dashboard' })
    await load()
  }

  async function blockTask(taskId: string) {
    await api.blockExecutionTask(taskId, { blockReason: 'Blocked from dashboard' })
    await load()
  }

  if (loading) {
    return <div className="page-shell"><section className="card">加载中...</section></div>
  }

  if (!docket) {
    return <div className="page-shell"><section className="card">未找到议案</section></div>
  }

  return (
    <div className="page-shell detail-page">
      <section className="card">
        <div className="card-header">
          <div>
            <p className="eyebrow">Docket Detail</p>
            <h1>{docket.title}</h1>
          </div>
          <div className="header-actions">
            <Link className="secondary-button" to="/dockets">返回列表</Link>
            <button className="secondary-button" onClick={() => void load()}>刷新</button>
          </div>
        </div>
        <div className="pill-row">
          <span className="pill">{docket.state}</span>
          <span className="pill muted">{docket.mode}</span>
          <span className="pill muted">Owner {docket.currentOwner}</span>
        </div>
        <p className="description">{docket.edictRaw}</p>
        {error ? <p className="error-text">{error}</p> : null}
      </section>

      <section className="detail-grid">
        <article className="card">
          <h2>Senate</h2>
          <div className="button-row">
            <button className="secondary-button" onClick={() => void openSenate()}>开启会话</button>
          </div>
          {senate ? (
            <>
              <p className="meta-line">Session: {senate.id}</p>
              <p className="meta-line">Status: {senate.status}</p>
              <p className="meta-line">Motion: {senate.recommendedMotion || 'N/A'}</p>
              <div className="mini-list">
                <strong>共识</strong>
                {senate.consensus.map((item) => <span key={item}>{item}</span>)}
              </div>
              <div className="mini-list">
                <strong>争议</strong>
                {senate.disputes.map((item) => <span key={item}>{item}</span>)}
              </div>
              <div className="mini-list">
                <strong>意见</strong>
                {senate.opinions.map((item) => <span key={item.id}>{item.roleCode}: {item.summary}</span>)}
              </div>
            </>
          ) : (
            <p className="meta-line">暂无元老院会话</p>
          )}

          <form className="stack-form" onSubmit={submitSenateOpinion}>
            <label>
              <span>角色</span>
              <select value={senateForm.roleCode} onChange={(event) => setSenateForm((prev) => ({ ...prev, roleCode: event.target.value }))}>
                {senatorRoles.map((role) => <option key={role} value={role}>{role}</option>)}
              </select>
            </label>
            <label>
              <span>立场</span>
              <select value={senateForm.stance} onChange={(event) => setSenateForm((prev) => ({ ...prev, stance: event.target.value }))}>
                <option value="SUPPORT">SUPPORT</option>
                <option value="OBJECT">OBJECT</option>
                <option value="CONDITION">CONDITION</option>
                <option value="NEUTRAL">NEUTRAL</option>
              </select>
            </label>
            <input value={senateForm.summary} onChange={(event) => setSenateForm((prev) => ({ ...prev, summary: event.target.value }))} placeholder="摘要" required />
            <textarea className="text-area" rows={3} value={senateForm.details} onChange={(event) => setSenateForm((prev) => ({ ...prev, details: event.target.value }))} placeholder="详细意见" />
            <button className="primary-button" type="submit">提交元老意见</button>
          </form>
        </article>

        <article className="card">
          <h2>Tribune</h2>
          <textarea className="text-area" rows={4} value={tribuneReason} onChange={(event) => setTribuneReason(event.target.value)} placeholder="保民官意见 / 否决原因" />
          <div className="button-row">
            <button className="primary-button" onClick={() => void handleTribune('approve')}>通过</button>
            <button className="danger-button" onClick={() => void handleTribune('reject')}>否决</button>
            <button className="secondary-button" onClick={() => void handleTribune('return')}>退回重议</button>
          </div>
        </article>

        <article className="card">
          <h2>Caesar</h2>
          <textarea className="text-area" rows={4} value={caesarComment} onChange={(event) => setCaesarComment(event.target.value)} placeholder="裁决批注" />
          <input value={caesarConstraints} onChange={(event) => setCaesarConstraints(event.target.value)} placeholder="限制条件，逗号分隔" />
          <div className="button-row">
            <button className="primary-button" onClick={() => void handleCaesar('approve')}>批准</button>
            <button className="secondary-button" onClick={() => void handleCaesar('reject')}>退回元老院</button>
            <button className="danger-button" onClick={() => void handleCaesar('override')}>强制批准</button>
            <button className="secondary-button" onClick={() => void handleCaesar('restrict')}>附限制批准</button>
          </div>
        </article>

        <article className="card">
          <h2>Delegations</h2>
          <form className="stack-form" onSubmit={submitDelegation}>
            <label>
              <span>角色</span>
              <select value={delegationForm.roleCode} onChange={(event) => setDelegationForm((prev) => ({ ...prev, roleCode: event.target.value }))}>
                {delegationRoles.map((role) => <option key={role} value={role}>{role}</option>)}
              </select>
            </label>
            <input value={delegationForm.objective} onChange={(event) => setDelegationForm((prev) => ({ ...prev, objective: event.target.value }))} placeholder="执行目标" required />
            <input value={delegationForm.dependsOn} onChange={(event) => setDelegationForm((prev) => ({ ...prev, dependsOn: event.target.value }))} placeholder="依赖 delegation id，逗号分隔" />
            <button className="primary-button" type="submit">新增派发</button>
          </form>

          <div className="mini-list">
            {delegations.map((item) => (
              <span key={item.id}>{item.roleCode} · {item.status} · {item.objective}</span>
            ))}
          </div>
        </article>

        <article className="card full-span">
          <h2>Execution Tasks</h2>
          <div className="task-grid">
            {tasks.map((task) => (
              <div className="task-card" key={task.id}>
                <div className="pill-row">
                  <span className="pill">{task.status}</span>
                  <span className="pill muted">{task.roleCode}</span>
                </div>
                <p className="meta-line">{task.id}</p>
                <p className="meta-line">Progress: {task.progressPercent}%</p>
                <p className="meta-line">{task.blockReason || task.outputSummary || 'No notes'}</p>
                <div className="button-row compact">
                  <button className="secondary-button" onClick={() => void updateTaskProgress(task.id, Math.min(task.progressPercent + 25, 99))}>+25%</button>
                  <button className="danger-button" onClick={() => void blockTask(task.id)}>阻塞</button>
                  <button className="primary-button" onClick={() => void completeTask(task.id)}>完成</button>
                </div>
              </div>
            ))}
          </div>
        </article>

        <article className="card full-span">
          <h2>Timeline</h2>
          <div className="timeline-list">
            {timeline.map((event) => (
              <div className="timeline-item" key={event.id}>
                <div>
                  <strong>{event.eventType}</strong>
                  <p className="meta-line">{event.actorType} / {event.actorId}</p>
                </div>
                <p className="meta-line small">{event.createdAt}</p>
              </div>
            ))}
          </div>
        </article>
      </section>
    </div>
  )
}

export default DocketDetailPage
