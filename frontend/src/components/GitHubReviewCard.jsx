import { useState } from 'react'
import api from '../api/client'
import PanelCard from './PanelCard'

export default function GitHubReviewCard({ defaultEmail, defaultUserId, onReviewCreated }) {
  const [repoUrl, setRepoUrl] = useState('https://github.com/octocat/Hello-World')
  const [defaultBranch, setDefaultBranch] = useState('main')
  const [githubUsername, setGithubUsername] = useState('')
  const [githubToken, setGithubToken] = useState('')
  const [userEmail, setUserEmail] = useState(defaultEmail)
  const [repositoryId, setRepositoryId] = useState('')
  const [pullRequestNumber, setPullRequestNumber] = useState('1')
  const [userId, setUserId] = useState(defaultUserId)
  const [loading, setLoading] = useState(false)

  const connectRepo = async () => {
    setLoading(true)
    try {
      const { data } = await api.post('/api/v1/github/connect-repository', {
        repoUrl,
        defaultBranch,
        githubUsername,
        githubToken,
        userEmail: userEmail?.trim() ? userEmail.trim() : null,
      })
      setRepositoryId(String(data.repositoryId))
      if (data.userId) {
        setUserId(String(data.userId))
      }
      if (data.userEmail) {
        setUserEmail(data.userEmail)
      }
    } catch (err) {
      onReviewCreated(null, err.response?.data?.message || err.message)
    } finally {
      setLoading(false)
    }
  }

  const analyzePr = async () => {
    setLoading(true)
    try {
      const { data } = await api.post('/api/v1/github/analyze-pr', {
        repositoryId: Number(repositoryId),
        pullRequestNumber: Number(pullRequestNumber),
        userId: Number(userId),
      })
      onReviewCreated(data)
    } catch (err) {
      onReviewCreated(null, err.response?.data?.message || err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <PanelCard title="GitHub PR Review" subtitle="Connect repository and trigger AI review for pull requests.">
      <div className="grid-form">
        <input value={repoUrl} onChange={(e) => setRepoUrl(e.target.value)} placeholder="Repository URL" />
        <input value={defaultBranch} onChange={(e) => setDefaultBranch(e.target.value)} placeholder="Default branch" />
        <input value={githubUsername} onChange={(e) => setGithubUsername(e.target.value)} placeholder="GitHub username" />
        <input type="password" value={githubToken} onChange={(e) => setGithubToken(e.target.value)} placeholder="GitHub token" />
        <input type="email" value={userEmail} onChange={(e) => setUserEmail(e.target.value)} placeholder="User email (optional)" />
        <button onClick={connectRepo} disabled={loading}>{loading ? 'Connecting...' : 'Connect Repository'}</button>

        <hr />

        <input value={repositoryId} onChange={(e) => setRepositoryId(e.target.value)} placeholder="Repository ID" />
        <input value={pullRequestNumber} onChange={(e) => setPullRequestNumber(e.target.value)} placeholder="PR number" />
        <input value={userId} onChange={(e) => setUserId(e.target.value)} placeholder="User ID" />
        <button onClick={analyzePr} disabled={loading}>{loading ? 'Analyzing PR...' : 'Analyze Pull Request'}</button>
      </div>
    </PanelCard>
  )
}
