export function FormAlert({ message }: { message?: string }) {
  if (!message) return null

  return (
    <p
      role="alert"
      className="rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm whitespace-pre-line text-destructive"
    >
      {message}
    </p>
  )
}
