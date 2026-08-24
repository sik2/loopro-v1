import type { ComponentProps } from 'react'
import ReactMarkdown from 'react-markdown'
import rehypeSanitize from 'rehype-sanitize'
import remarkGfm from 'remark-gfm'

/**
 * 마크다운을 HTML로 그린다. 내용은 사용자가 쓴 것이므로 새니타이징이 필수다.
 * remark-gfm은 표와 여러 줄 코드블록을 위해 붙인다.
 */
export function Markdown({ children }: { children: string }) {
  return (
    <div className="prose prose-loopro max-w-none">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeSanitize]}
        components={{ table: ScrollableTable }}
      >
        {children}
      </ReactMarkdown>
    </div>
  )
}

/**
 * 넓은 표가 본문 폭을 밀어내지 않도록 자기 상자 안에서 넘어가게 한다.
 * 표 자체에 overflow를 걸면 좁은 표가 폭을 못 채우므로 감싸는 쪽에 건다.
 */
function ScrollableTable(props: ComponentProps<'table'>) {
  return (
    <div className="not-prose my-6 overflow-x-auto rounded-md border border-border">
      <table className="prose-table w-full border-collapse text-[0.9em]" {...props} />
    </div>
  )
}
