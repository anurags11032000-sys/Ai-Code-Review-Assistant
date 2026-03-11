import { useState } from 'react'
import api from '../api/client'
import PanelCard from './PanelCard'

export default function UploadReviewCard({ defaultEmail, onReviewCreated }) {
  const [fileName, setFileName] = useState('')
  const [language, setLanguage] = useState('')
  const [sourceCode, setSourceCode] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await api.post('/api/v1/reviews/upload', { fileName, language, sourceCode, userEmail: defaultEmail })
      onReviewCreated(data)
    } catch (err) {
      onReviewCreated(null, err.response?.data?.message || err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <PanelCard title="Upload & Analyze Code" subtitle="AI detects bugs, security issues, and refactoring opportunities.">
      <form className="grid-form" onSubmit={submit}>
        <input value={fileName} onChange={(e) => setFileName(e.target.value)} placeholder="File name" required />
        <input value={language} onChange={(e) => setLanguage(e.target.value)} placeholder="Programming language" required />
        <textarea value={sourceCode} onChange={(e) => setSourceCode(e.target.value)} rows={9} placeholder="Paste source code" required />
        <button disabled={loading}>{loading ? 'Analyzing...' : 'Analyze Code'}</button>
      </form>
    </PanelCard>
  )
}
