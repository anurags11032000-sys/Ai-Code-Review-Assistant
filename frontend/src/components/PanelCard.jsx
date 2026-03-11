export default function PanelCard({ title, subtitle, children }) {
  return (
    <section className="panel-card">
      <h2>{title}</h2>
      {subtitle && <p className="subtitle">{subtitle}</p>}
      {children}
    </section>
  )
}
