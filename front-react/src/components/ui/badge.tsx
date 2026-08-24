import type { ComponentProps } from 'react'
import { cn } from '@/lib/utils'

export function Badge({ className, ...props }: ComponentProps<'span'>) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-md border border-border px-2 py-0.5',
        'bg-surface-strong text-[11px] font-medium text-muted-foreground',
        className,
      )}
      {...props}
    />
  )
}
