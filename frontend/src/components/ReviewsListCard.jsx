import { useState } from 'react'
import api from '../api/client'
import PanelCard from './PanelCard'

export default function ReviewsListCard({ defaultFileName, onSelectReview, onError }) {
  const [fileName, setFileName] = useState(defaultFileName || '')
  const [loading, setLoading] = useState(false)

  const load = async () => {
    const trimmed = fileName.trim()
    if (!trimmed) {
      onError?.('Please enter a file name')
      return
    }

    setLoading(true)
    try {
      const { data } = await api.get('/api/v1/reviews/search-by-file', { params: { fileName: trimmed } })
      await onSelectReview(data.reviewId)
      onError?.('')
    } catch (err) {
      onError?.(err.response?.data?.message || err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <PanelCard title="Previous Reviews" subtitle="Retrieve historical AI review runs.">
      <div className="grid-form">
        <div className="inline-row">
          <input value={fileName} onChange={(e) => setFileName(e.target.value)} placeholder="File name (e.g. first.cpp)" />
          <button onClick={load} disabled={loading}>{loading ? 'Loading...' : 'Search'}</button>
        </div>
      </div>
    </PanelCard>
  )
}
