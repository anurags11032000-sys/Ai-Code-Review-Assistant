import { useState } from 'react'
import api from '../api/client'
import PanelCard from './PanelCard'

export default function ExplainCodeCard({ onExplained }) {
  const [language, setLanguage] = useState('')
  const [context, setContext] = useState('')
  const [code, setCode] = useState('')
  const [loading, setLoading] = useState(false)

  const explain = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await api.post('/api/v1/reviews/explain', { code, language, context })
      onExplained(data)
    } catch (err) {
      onExplained(null, err.response?.data?.message || err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <PanelCard title="Explain Code" subtitle="Ask AI to break down logic, complexity, and refactoring hints.">
      <form className="grid-form" onSubmit={explain}>
        <input value={language} onChange={(e) => setLanguage(e.target.value)} placeholder="Programming language" required />
        <input value={context} onChange={(e) => setContext(e.target.value)} placeholder="Code walkthrough (optional)" />
        <textarea value={code} onChange={(e) => setCode(e.target.value)} rows={8} placeholder="Paste code snippet" required />
        <button disabled={loading}>{loading ? 'Explaining...' : 'Get Explanation'}</button>
      </form>
    </PanelCard>
  )
}
