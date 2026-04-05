import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, ArchiveRecord } from '../api'

function ArchiveListPage() {
  const [archives, setArchives] = useState<ArchiveRecord[]>([])
  const [error, setError] = useState<string | null>(null)

  async function load() {
    try {
      setError(null)
      setArchives(await api.listArchives())
    } catch (err) {
      setError((err as Error).message)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  return (
    <div className="page-shell">
      <section className="card">
        <div className="card-header">
          <div>
            <p className="eyebrow">Archives</p>
            <h1>归档案卷</h1>
          </div>
          <button className="secondary-button" onClick={() => void load()}>刷新</button>
        </div>
        {error ? <p className="error-text">{error}</p> : null}
        <div className="grid-list">
          {archives.map((archive) => (
            <Link className="card docket-card" key={archive.id} to={`/archives/${archive.docketId}`}>
              <div className="pill-row">
                <span className="pill">ARCHIVED</span>
              </div>
              <h3>{archive.title}</h3>
              <p className="meta-line">{archive.finalSummary || 'No summary'}</p>
              <p className="meta-line small">{archive.archivedAt}</p>
            </Link>
          ))}
        </div>
      </section>
    </div>
  )
}

export default ArchiveListPage
