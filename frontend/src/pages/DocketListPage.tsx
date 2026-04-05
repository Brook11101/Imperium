import { FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, DocketOverview } from '../api'

function DocketListPage() {
  const navigate = useNavigate()
  const [dockets, setDockets] = useState<DocketOverview[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [form, setForm] = useState({ edictRaw: '', mode: 'STANDARD_SENATE', priority: 'NORMAL' })

  async function load() {
    try {
      setLoading(true)
      setError(null)
      setDockets(await api.listDockets())
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    try {
      setSubmitting(true)
      const created = await api.createDocket(form)
      setForm({ edictRaw: '', mode: 'STANDARD_SENATE', priority: 'NORMAL' })
      await load()
      navigate(`/dockets/${created.id}`)
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page-shell">
      <section className="card">
        <div className="card-header">
          <div>
            <p className="eyebrow">Caesar Desk</p>
            <h1>Imperium Dockets</h1>
          </div>
          <button className="secondary-button" onClick={() => void load()}>
            刷新
          </button>
        </div>

        <form className="stack-form" onSubmit={onSubmit}>
          <textarea
            className="text-area"
            placeholder="输入恺撒法令"
            value={form.edictRaw}
            onChange={(event) => setForm((prev) => ({ ...prev, edictRaw: event.target.value }))}
            rows={4}
            required
          />

          <div className="form-row">
            <label>
              <span>运行模式</span>
              <select value={form.mode} onChange={(event) => setForm((prev) => ({ ...prev, mode: event.target.value }))}>
                <option value="STANDARD_SENATE">STANDARD_SENATE</option>
                <option value="DIRECT_DECREE">DIRECT_DECREE</option>
                <option value="TRIBUNE_LOCK">TRIBUNE_LOCK</option>
              </select>
            </label>

            <label>
              <span>优先级</span>
              <select value={form.priority} onChange={(event) => setForm((prev) => ({ ...prev, priority: event.target.value }))}>
                <option value="LOW">LOW</option>
                <option value="NORMAL">NORMAL</option>
                <option value="HIGH">HIGH</option>
                <option value="CRITICAL">CRITICAL</option>
              </select>
            </label>
          </div>

          <button className="primary-button" disabled={submitting} type="submit">
            {submitting ? '创建中...' : '发布议案'}
          </button>
        </form>

        {error ? <p className="error-text">{error}</p> : null}
      </section>

      <section className="page-section">
        <div className="section-heading">
          <h2>议案列表</h2>
          <span>{loading ? '加载中...' : `${dockets.length} 个议案`}</span>
        </div>

        <div className="grid-list">
          {dockets.map((docket) => (
            <Link className="card docket-card" key={docket.id} to={`/dockets/${docket.id}`}>
              <div className="pill-row">
                <span className="pill">{docket.state}</span>
                <span className="pill muted">{docket.mode}</span>
              </div>
              <h3>{docket.title}</h3>
              <p className="meta-line">Owner: {docket.currentOwner}</p>
              <p className="meta-line">Priority: {docket.priority}</p>
              <p className="meta-line small">{docket.id}</p>
            </Link>
          ))}
        </div>
      </section>
    </div>
  )
}

export default DocketListPage
