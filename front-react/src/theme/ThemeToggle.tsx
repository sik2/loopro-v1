import { Moon, Sun } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { chooseTheme, followSystemTheme, resolveTheme, type Theme } from '@/theme/theme'

export function ThemeToggle() {
  const [theme, setTheme] = useState<Theme>(resolveTheme)

  useEffect(() => followSystemTheme(setTheme), [])

  const label = theme === 'dark' ? '밝은 화면으로' : '어두운 화면으로'

  return (
    <Button
      variant="ghost"
      size="icon"
      aria-label={label}
      title={label}
      onClick={() => {
        const next: Theme = theme === 'dark' ? 'light' : 'dark'
        chooseTheme(next)
        setTheme(next)
      }}
    >
      {theme === 'dark' ? <Sun aria-hidden /> : <Moon aria-hidden />}
    </Button>
  )
}
