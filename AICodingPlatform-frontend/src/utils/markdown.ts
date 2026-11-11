import { marked } from 'marked'
import hljs from 'highlight.js'
import DOMPurify from 'dompurify'

marked.setOptions({
  breaks: true,
  gfm: true,
})

const renderer = new marked.Renderer()

renderer.code = function ({ text, lang }: { text: string; lang?: string }) {
  const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
  const highlighted = hljs.highlight(text, { language, ignoreIllegals: true }).value
  return `<pre class="hljs"><code class="language-${language} hljs">${highlighted}</code></pre>\n`
}

marked.use({ renderer })

export function renderMarkdown(text: string): string {
  if (!text) return ''
  const html = marked.parse(text) as string
  // AI 输出属于不可信内容，必须净化后再交给 v-html，防止 XSS
  return DOMPurify.sanitize(html, { ADD_ATTR: ['target'] })
}
