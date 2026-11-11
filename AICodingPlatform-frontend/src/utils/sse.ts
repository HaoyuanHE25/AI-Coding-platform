/**
 * SSE 流式请求工具
 * 用于处理 /app/chat/gen/code 等 text/event-stream 接口
 * 由于该接口为 GET + withCredentials，EventSource 无法携带手动请求头，
 * 这里使用 fetch + ReadableStream 实现 SSE 解析。
 */

import { API_BASE_URL } from '@/config/env'

const BASE_URL = API_BASE_URL

export interface SSEOptions {
  method?: string
  headers?: Record<string, string>
  body?: BodyInit
  onMessage?: (data: string, raw: MessageEventLike) => void
  onError?: (error: Error) => void
  onDone?: () => void
  signal?: AbortSignal
}

interface MessageEventLike {
  event: string
  data: string
  lastEventId: string
}

/**
 * 发送 SSE 请求
 * @param url 请求路径（相对于 BASE_URL）
 * @param options 回调配置
 */
export async function sendSSE(url: string, options: SSEOptions = {}): Promise<void> {
  const { method = 'GET', headers, body, onMessage, onError, onDone, signal } = options

  const fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`

  try {
    const response = await fetch(fullUrl, {
      method,
      headers,
      body,
      credentials: 'include',
      signal,
    })

    if (!response.ok || !response.body) {
      let errMsg = `请求失败: ${response.status}`
      try {
        const errBody = await response.text()
        if (errBody) errMsg = errBody
      } catch {
        // ignore
      }
      throw new Error(errMsg)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const parts = buffer.split(/\r?\n\r?\n/)
      buffer = parts.pop() ?? ''

      for (const part of parts) {
        const event = parseEvent(part)
        if (!event) continue
        // 后端结束事件：event: done
        if (event.event === 'done') continue
        if (onMessage) {
          onMessage(event.data, event)
        }
      }
    }

    if (onDone) onDone()
  } catch (err) {
    const error = err instanceof Error ? err : new Error(String(err))
    if (error.name === 'AbortError') {
      if (onDone) onDone()
      return
    }
    if (onError) onError(error)
    else throw error
  }
}

function parseEvent(raw: string): MessageEventLike | null {
  if (!raw.trim()) return null

  const lines = raw.split(/\r?\n/)
  const event: MessageEventLike = {
    event: 'message',
    data: '',
    lastEventId: '',
  }
  const dataLines: string[] = []

  for (const line of lines) {
    if (!line || line.startsWith(':')) continue
    if (line.startsWith('event:')) {
      event.event = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).replace(/^\s/, ''))
    } else if (line.startsWith('id:')) {
      event.lastEventId = line.slice(3).trim()
    }
  }
  event.data = dataLines.join('\n')
  return event
}
