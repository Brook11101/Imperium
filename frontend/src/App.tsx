import { Navigate, NavLink, Route, Routes } from 'react-router-dom'
import DocketListPage from './pages/DocketListPage'
import DocketDetailPage from './pages/DocketDetailPage'
import AuditPage from './pages/AuditPage'
import ArchiveListPage from './pages/ArchiveListPage'
import ArchiveDetailPage from './pages/ArchiveDetailPage'

function App() {
  return (
    <main className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">Imperium</p>
          <h1>Roman governance console</h1>
        </div>
        <nav className="nav-tabs">
          <NavLink to="/dockets">Dockets</NavLink>
          <NavLink to="/audit">Audit</NavLink>
          <NavLink to="/archives">Archives</NavLink>
        </nav>
      </header>

      <Routes>
        <Route path="/" element={<Navigate replace to="/dockets" />} />
        <Route path="/dockets" element={<DocketListPage />} />
        <Route path="/dockets/:docketId" element={<DocketDetailPage />} />
        <Route path="/audit" element={<AuditPage />} />
        <Route path="/archives" element={<ArchiveListPage />} />
        <Route path="/archives/:docketId" element={<ArchiveDetailPage />} />
      </Routes>
    </main>
  )
}

export default App
