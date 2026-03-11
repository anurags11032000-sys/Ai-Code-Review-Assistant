import { useEffect, useMemo, useState } from 'react'
import api from './api/client'
import UploadReviewCard from './components/UploadReviewCard'
import ExplainCodeCard from './components/ExplainCodeCard'
import ReviewsListCard from './components/ReviewsListCard'

export default function App() {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  const googleLoginUrl = `${apiBaseUrl}/oauth2/authorization/google`
  const defaultFileName = import.meta.env.VITE_DEFAULT_FILE_NAME || ''

  const [authChecked, setAuthChecked] = useState(false)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [authUser, setAuthUser] = useState(null)
  const [lastReview, setLastReview] = useState(null)
  const [explanation, setExplanation] = useState(null)
  const [activeModal, setActiveModal] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const loadAuth = async () => {
      try {
        const { data } = await api.get('/api/v1/auth/me')
        setIsAuthenticated(Boolean(data.authenticated))
        setAuthUser(data.authenticated ? data : null)
      } catch (err) {
        setIsAuthenticated(false)
        setAuthUser(null)
      } finally {
        setAuthChecked(true)
      }
    }
    loadAuth()
  }, [])

  const onReviewCreated = (review, err) => {
    if (err) {
      setError(err)
      return
    }
    setError('')
    setLastReview(review)
    setActiveModal('review')
  }

  const onExplained = (data, err) => {
    if (err) {
      setError(err)
      return
    }
    setError('')
    setExplanation(data)
    setActiveModal('explanation')
  }

  const selectReview = async (reviewId) => {
    try {
      const { data } = await api.get(`/api/v1/reviews/${reviewId}`)
      setLastReview(data)
      setError('')
      setActiveModal('review')
    } catch (err) {
      setError(err.response?.data?.message || err.message)
    }
  }

  const suggestionCount = useMemo(() => lastReview?.suggestions?.length || 0, [lastReview])
  const suggestionsByType = useMemo(() => {
    const suggestions = lastReview?.suggestions || []
    return suggestions.reduce((acc, suggestion) => {
      const type = suggestion.suggestionType || 'OTHER'
      if (!acc[type]) {
        acc[type] = []
      }
      acc[type].push(suggestion)
      return acc
    }, {})
  }, [lastReview])
  const orderedSuggestionTypes = useMemo(() => {
    const preferredOrder = ['BUG', 'SECURITY', 'PERFORMANCE', 'REFACTOR', 'READABILITY', 'EXPLANATION', 'PR_SUMMARY', 'OTHER']
    const types = Object.keys(suggestionsByType)
    return types.sort((a, b) => {
      const aIndex = preferredOrder.indexOf(a)
      const bIndex = preferredOrder.indexOf(b)
      const left = aIndex === -1 ? Number.MAX_SAFE_INTEGER : aIndex
      const right = bIndex === -1 ? Number.MAX_SAFE_INTEGER : bIndex
      return left - right || a.localeCompare(b)
    })
  }, [suggestionsByType])

  const signOut = async () => {
    try {
      await api.post('/api/v1/auth/logout')
    } catch (err) {
      // ignore logout errors and force local sign-out state
    } finally {
      setIsAuthenticated(false)
      setAuthUser(null)
      setLastReview(null)
      setExplanation(null)
      setActiveModal(null)
      setError('')
    }
  }

  if (!authChecked) {
    return (
      <main className="auth-shell">
        <article className="auth-card">
          <h1>AI Code Review Assistant</h1>
          <p className="subtitle">Checking your sign-in session...</p>
        </article>
      </main>
    )
  }

  if (!isAuthenticated) {
    return (
      <main className="auth-shell">
        <article className="auth-card">
          <p className="eyebrow">Welcome</p>
          <h1>AI Code Review Assistant</h1>
          <p className="subtitle">Sign in with Google to start using code analysis and explanation tools.</p>
          <a className="google-btn" href={googleLoginUrl}>Sign in with Google</a>
        </article>
      </main>
    )
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Developer Console</p>
          <h1>AI Code Review Assistant</h1>
        </div>
        <div className="api-box">
          <div className="tiny">Signed in as: {authUser?.email || authUser?.name || 'Google User'}</div>
          <button onClick={signOut}>Sign out</button>
        </div>
      </header>

      {error && <p className="error-banner">{error}</p>}

      <section className="dashboard-grid">
        <UploadReviewCard defaultEmail={authUser?.email || ''} onReviewCreated={onReviewCreated} />
        <ExplainCodeCard onExplained={onExplained} />
        <ReviewsListCard defaultFileName={defaultFileName} onSelectReview={selectReview} onError={setError} />
      </section>

      {activeModal === 'review' && lastReview && (
        <div className="modal-overlay" onClick={() => setActiveModal(null)}>
          <article className="modal-window" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Latest Review Output</h2>
              <button onClick={() => setActiveModal(null)}>Close</button>
            </div>
            <div className="modal-content">
              <p><strong>Review ID:</strong> {lastReview.reviewId}</p>
              <p><strong>Source:</strong> {lastReview.sourceType} | {lastReview.sourceName}</p>
              <p><strong>Status:</strong> {lastReview.status}</p>
              <p><strong>Risk Score:</strong> {lastReview.riskScore ?? 'N/A'}</p>
              <p><strong>Summary:</strong> {lastReview.overallSummary || 'No summary'}</p>
              <p><strong>Suggestions:</strong> {suggestionCount}</p>

              {orderedSuggestionTypes.map((type) => (
                <div key={type}>
                  <h3>{type} ({suggestionsByType[type].length})</h3>
                  <ul className="result-list">
                    {suggestionsByType[type].map((s) => (
                      <li key={s.id || `${type}-${s.title}-${s.lineNumber}`}>
                        <div>
                          <strong>{s.suggestionType}</strong> [{s.severity}] - {s.title}
                          <div className="tiny">{s.filePath || 'N/A'} {s.lineNumber ? `: line ${s.lineNumber}` : ''}</div>
                          <div className="tiny">{s.details}</div>
                          {s.suggestedFix && <pre>{s.suggestedFix}</pre>}
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </article>
        </div>
      )}

      {activeModal === 'explanation' && explanation && (
        <div className="modal-overlay" onClick={() => setActiveModal(null)}>
          <article className="modal-window" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Code Explanation Result</h2>
              <button onClick={() => setActiveModal(null)}>Close</button>
            </div>
            <div className="modal-content">
              <h3>Explanation</h3>
              <p>{explanation.explanation}</p>
              <h3>Complexity Notes</h3>
              <p>{explanation.complexityNotes}</p>
              <h3>Refactoring Hint</h3>
              <p>{explanation.refactoringHint}</p>
            </div>
          </article>
        </div>
      )}
    </main>
  )
}
