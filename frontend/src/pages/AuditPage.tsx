import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, AuditQueueItem } from '../api'

function AuditPage() {
  const [items, setItems] = useState<AuditQueueItem[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [submittingId, setSubmittingId] = useState<string | null>(null)

  async function load() {
    try {
      setLoading(true)
      setError(null)
      setItems(await api.listAuditQueue())
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  async function handleAction(docketId: string, action: 'pass' | 'return' | 'escalate') {
    try {
      setSubmittingId(docketId)
      if (action === 'pass') {
        await api.auditPass(docketId, { finalSummary: 'Archived from audit page' })
      }
      if (action === 'return') {
        await api.auditReturn(docketId, { qualityNotes: ['Manual review requested more execution work'] })
      }
      if (action === 'escalate') {
        await api.auditEscalate(docketId, { riskNotes: ['Manual review escalated to Caesar'] })
      }
      await load()
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setSubmittingId(null)
    }
  }

  return (
    <div className="page-shell">
      <section className="card">
        <div className="card-header">
          <div>
            <p className="eyebrow">Audit</p>
            <h1>审计队列</h1>
          </div>
          <button className="secondary-button" onClick={() => void load()}>
            刷新
          </button>
        </div>
        {error ? <p className="error-text">{error}</p> : null}
        <div className="grid-list audit-grid">
          {items.map((item) => (
            <Link className="card docket-card" key={item.docketId} to={`/dockets/${item.docketId}`}>
              <div className="pill-row">
                <span className="pill">UNDER_AUDIT</span>
                <span className="pill muted">{item.mode}</span>
              </div>
              <h3>{item.title}</h3>
              <p className="meta-line">Priority: {item.priority}</p>
              <p className="meta-line">Risk: {item.riskLevel}</p>
              <p className="meta-line">Owner: {item.currentOwner}</p>
            </Link>
          ))}
        </div>
        <div className="grid-list audit-grid">
          {items.map((item) => (
            <div className="card" key={`${item.docketId}-actions`}>
              <h3>{item.title}</h3>
              <div className="button-row">
                <button className="primary-button" disabled={submittingId === item.docketId} onClick={() => void handleAction(item.docketId, 'pass')}>通过并归档</button>
                <button className="secondary-button" disabled={submittingId === item.docketId} onClick={() => void handleAction(item.docketId, 'return')}>退回执行</button>
                <button className="danger-button" disabled={submittingId === item.docketId} onClick={() => void handleAction(item.docketId, 'escalate')}>升级恺撒</button>
              </div>
            </div>
          ))}
        </div>
        {loading && <p className="meta-line">加载中...</p>}
      </section>
    </div>
  )
}

export default AuditPage
