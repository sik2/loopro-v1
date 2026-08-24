import ReactMarkdown from 'react-markdown'
import rehypeSanitize from 'rehype-sanitize'
import remarkGfm from 'remark-gfm'

/**
 * 마크다운을 HTML로 그린다. 내용은 사용자가 쓴 것이므로 새니타이징이 필수다.
 * remark-gfm은 표와 여러 줄 코드블록을 위해 붙인다.
 */
export function Markdown({ children }: { children: string }) {
  return (
    <div className="prose prose-neutral max-w-none prose-pre:bg-muted prose-pre:text-foreground">
      <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeSanitize]}>
        {children}
      </ReactMarkdown>
    </div>
  )
}
