function App() {
  return (
    <main className="app-shell">
      <section className="hero">
        <p className="eyebrow">Imperium</p>
        <h1>Roman governance for agent systems</h1>
        <p className="description">
          The project skeleton is ready. Next steps are domain services, persistence,
          queue integration, and the Caesar-facing console.
        </p>
      </section>

      <section className="panel-grid">
        <article className="panel">
          <h2>API</h2>
          <p>Spring Boot service for dockets, senate flow, and Caesar decisions.</p>
        </article>
        <article className="panel">
          <h2>Worker</h2>
          <p>Async job runner for OpenClaw CLI, recovery, and scheduling.</p>
        </article>
        <article className="panel">
          <h2>Web</h2>
          <p>React client for Caesar Desk, Senate Chamber, and archive views.</p>
        </article>
      </section>
    </main>
  )
}

export default App
