export type Theme = 'dark' | 'light'

const STORAGE_KEY = 'loopro.theme'
const LIGHT_QUERY = '(prefers-color-scheme: light)'

function storedTheme(): Theme | null {
  const stored = localStorage.getItem(STORAGE_KEY)
  return stored === 'dark' || stored === 'light' ? stored : null
}

function systemTheme(): Theme {
  return window.matchMedia(LIGHT_QUERY).matches ? 'light' : 'dark'
}

/** 고른 적이 있으면 그 선택을, 없으면 기기 설정을 따른다. */
export function resolveTheme(): Theme {
  return storedTheme() ?? systemTheme()
}

/** 화면에만 반영한다. 사용자가 직접 고른 것이 아니므로 저장하지 않는다. */
export function paintTheme(theme: Theme) {
  document.documentElement.classList.toggle('light', theme === 'light')
}

/** 사용자가 직접 고른 것. 이후로는 기기 설정을 따르지 않는다. */
export function chooseTheme(theme: Theme) {
  localStorage.setItem(STORAGE_KEY, theme)
  paintTheme(theme)
}

/**
 * 아직 직접 고른 적이 없는 동안에는 기기 설정 변화를 그대로 따라간다.
 * (해가 지면 OS가 어두워지는 설정을 쓰는 사람이 있다.)
 */
export function followSystemTheme(onChange: (theme: Theme) => void): () => void {
  const media = window.matchMedia(LIGHT_QUERY)

  const handle = () => {
    if (storedTheme()) return
    const next = systemTheme()
    paintTheme(next)
    onChange(next)
  }

  media.addEventListener('change', handle)
  return () => media.removeEventListener('change', handle)
}
