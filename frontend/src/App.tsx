import { Navigate, NavLink, Route, Routes } from 'react-router-dom'
import DocketListPage from './pages/DocketListPage'
import DocketDetailPage from './pages/DocketDetailPage'

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
        </nav>
      </header>

      <Routes>
        <Route path="/" element={<Navigate replace to="/dockets" />} />
        <Route path="/dockets" element={<DocketListPage />} />
        <Route path="/dockets/:docketId" element={<DocketDetailPage />} />
      </Routes>
    </main>
  )
}

export default App
