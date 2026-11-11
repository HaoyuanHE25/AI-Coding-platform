/**
 * 可视化编辑器工具
 * 负责与 iframe 内预览页面的通信、元素选择高亮、选中信息收集等逻辑。
 * 主页面通过 postMessage 向 iframe 注入编辑脚本并接收用户选中的元素信息。
 */

/** 选中元素的描述信息 */
export interface ElementInfo {
  /** 标签名（小写），如 div / span / h1 */
  tagName: string
  /** 元素 id（可能为空） */
  id: string
  /** 元素 class 列表（可能为空） */
  className: string
  /** 元素的文本内容摘要（截取前 200 字符） */
  textContent: string
  /** CSS 选择器路径，用于精确定位元素 */
  selector: string
  /** 当前页面路径（iframe 内的 URL pathname） */
  pagePath: string
}

/** VisualEditor 构造参数 */
export interface VisualEditorOptions {
  /** 当用户在 iframe 中选中某个元素时触发 */
  onElementSelected?: (info: ElementInfo) => void
}

/**
 * 注入到 iframe 内部的编辑模式脚本。
 * 该脚本在 iframe 的 window 上下文中执行，负责：
 * - 监听 mouseover / mouseout 实现悬浮边框
 * - 监听 click 实现选中高亮 + postMessage 回传元素信息
 * - 监听来自主页面的控制消息（启用/禁用编辑模式、清除选中）
 */
const EDITOR_INJECT_SCRIPT = `
(function () {
  // 防止重复注入
  if (window.__visualEditorInjected) return;
  window.__visualEditorInjected = true;

  let editMode = false;
  let selectedEl = null;
  let hoveredEl = null;

  // ---------- 样式注入 ----------
  const style = document.createElement('style');
  style.id = '__visual_editor_style';
  style.textContent = [
    '.__ve-hover { outline: 2px dashed #1677ff !important; outline-offset: 2px; cursor: pointer; }',
    '.__ve-selected { outline: 3px solid #cf1322 !important; outline-offset: 2px; cursor: pointer; }',
  ].join('\\n');
  document.head.appendChild(style);

  // ---------- 工具函数 ----------
  function buildSelector(el) {
    const parts = [];
    let cur = el;
    while (cur && cur !== document.body && cur !== document.documentElement) {
      let seg = cur.tagName.toLowerCase();
      if (cur.id) {
        seg += '#' + cur.id;
        parts.unshift(seg);
        break; // id 已足够唯一
      }
      if (cur.className && typeof cur.className === 'string') {
        const cls = cur.className.trim().split(/\\s+/).filter(c => c && c !== '__ve-hover' && c !== '__ve-selected');
        if (cls.length) seg += '.' + cls.join('.');
      }
      // 加上 nth-child 避免同名兄弟歧义
      const parent = cur.parentElement;
      if (parent) {
        const siblings = Array.from(parent.children).filter(c => c.tagName === cur.tagName);
        if (siblings.length > 1) {
          seg += ':nth-child(' + (Array.from(parent.children).indexOf(cur) + 1) + ')';
        }
      }
      parts.unshift(seg);
      cur = cur.parentElement;
    }
    return parts.join(' > ');
  }

  function getTextContent(el) {
    // 优先取直接文本节点，避免拿到子元素的长文本
    let text = '';
    for (const node of el.childNodes) {
      if (node.nodeType === Node.TEXT_NODE) {
        text += node.textContent.trim();
      }
    }
    if (!text) text = (el.innerText || el.textContent || '').trim();
    return text.substring(0, 200);
  }

  // ---------- 事件处理 ----------
  function onMouseOver(e) {
    if (!editMode) return;
    const target = e.target;
    if (target === selectedEl || target === hoveredEl) return;
    if (hoveredEl && hoveredEl !== selectedEl) {
      hoveredEl.classList.remove('__ve-hover');
    }
    hoveredEl = target;
    hoveredEl.classList.add('__ve-hover');
  }

  function onMouseOut(e) {
    if (!editMode) return;
    const target = e.target;
    if (target === hoveredEl && target !== selectedEl) {
      target.classList.remove('__ve-hover');
      hoveredEl = null;
    }
  }

  function onClick(e) {
    if (!editMode) return;
    e.preventDefault();
    e.stopPropagation();

    const target = e.target;

    // 清除旧选中
    if (selectedEl && selectedEl !== target) {
      selectedEl.classList.remove('__ve-selected');
      selectedEl.classList.remove('__ve-hover');
    }

    selectedEl = target;
    selectedEl.classList.remove('__ve-hover');
    selectedEl.classList.add('__ve-selected');

    // 回传元素信息给主页面
    const info = {
      tagName: target.tagName.toLowerCase(),
      id: target.id || '',
      className: (typeof target.className === 'string' ? target.className : '')
        .replace(/__ve-hover|__ve-selected/g, '').trim(),
      textContent: getTextContent(target),
      selector: buildSelector(target),
      pagePath: window.location.pathname,
    };
    window.parent.postMessage({ type: 'visual-editor:element-selected', data: info }, '*');
  }

  // ---------- 控制接口 ----------
  function enableEditMode() {
    editMode = true;
    document.addEventListener('mouseover', onMouseOver, true);
    document.addEventListener('mouseout', onMouseOut, true);
    document.addEventListener('click', onClick, true);
    document.body.style.cursor = 'crosshair';
  }

  function disableEditMode() {
    editMode = false;
    document.removeEventListener('mouseover', onMouseOver, true);
    document.removeEventListener('mouseout', onMouseOut, true);
    document.removeEventListener('click', onClick, true);
    document.body.style.cursor = '';
    clearSelection();
    // 清除所有残留高亮
    document.querySelectorAll('.__ve-hover').forEach(el => el.classList.remove('__ve-hover'));
  }

  function clearSelection() {
    if (selectedEl) {
      selectedEl.classList.remove('__ve-selected');
      selectedEl.classList.remove('__ve-hover');
      selectedEl = null;
    }
    if (hoveredEl) {
      hoveredEl.classList.remove('__ve-hover');
      hoveredEl = null;
    }
  }

  // 监听来自主页面的控制消息
  window.addEventListener('message', function (e) {
    // 只接受父窗口（主页面）发来的消息
    if (e.source !== window.parent) return;
    const msg = e.data;
    if (!msg || typeof msg.type !== 'string') return;
    switch (msg.type) {
      case 'visual-editor:enable':
        enableEditMode();
        break;
      case 'visual-editor:disable':
        disableEditMode();
        break;
      case 'visual-editor:clear-selection':
        clearSelection();
        break;
    }
  });
})();
`;

/**
 * 可视化编辑器
 * 在主页面中使用，管理与 iframe 预览页面的编辑交互。
 */
export class VisualEditor {
  private iframe: HTMLIFrameElement | null = null
  private editMode = false
  private options: VisualEditorOptions

  constructor(options: VisualEditorOptions = {}) {
    this.options = options
  }

  /**
   * 绑定 iframe 元素，应在 iframe load 事件中调用
   */
  init(iframe: HTMLIFrameElement): void {
    this.iframe = iframe
  }

  /**
   * iframe 加载完成后调用，向 iframe 注入编辑脚本
   * 注意：要求 iframe 与主页面同源（否则 contentDocument 访问会被浏览器拦截）。
   */
  injectScript(): void {
    if (!this.iframe?.contentWindow) return
    try {
      const doc = this.iframe.contentDocument
      if (!doc) return
      // 如果已经注入过则跳过
      if (doc.getElementById('__visual_editor_style')) return
      const script = doc.createElement('script')
      script.textContent = EDITOR_INJECT_SCRIPT
      ;(doc.head || doc.documentElement).appendChild(script)
      // 如果当前处于编辑模式，注入后立即启用
      if (this.editMode) {
        this.postToIframe('visual-editor:enable')
      }
    } catch (err) {
      // 跨域 iframe 会抛出 SecurityError，导致无法注入编辑脚本
      console.error(
        '[VisualEditor] 注入脚本失败：预览页面必须与主页面同源（同域名），' +
          '请确认预览地址经由 Vite 代理 / 反向代理转发（如 /api/static）。',
        err,
      )
    }
  }

  /**
   * 切换编辑模式，返回新的模式状态
   */
  toggleEditMode(): boolean {
    this.editMode = !this.editMode
    if (this.editMode) {
      this.postToIframe('visual-editor:enable')
    } else {
      this.postToIframe('visual-editor:disable')
    }
    return this.editMode
  }

  /**
   * 获取当前是否处于编辑模式
   */
  isEditMode(): boolean {
    return this.editMode
  }

  /**
   * 退出编辑模式
   */
  exitEditMode(): void {
    if (!this.editMode) return
    this.editMode = false
    this.postToIframe('visual-editor:disable')
  }

  /**
   * 清除 iframe 中的选中状态
   */
  clearSelection(): void {
    this.postToIframe('visual-editor:clear-selection')
  }

  /**
   * 处理来自 iframe 的 postMessage，应在 window message 事件中调用
   */
  handleMessage(event: MessageEvent): void {
    const msg = event.data
    if (!msg || typeof msg.type !== 'string') return

    // 只接受来自当前绑定 iframe 的消息
    if (event.source !== this.iframe?.contentWindow) return

    if (msg.type === 'visual-editor:element-selected') {
      this.options.onElementSelected?.(msg.data as ElementInfo)
    }
  }

  /**
   * 销毁编辑器，清理引用
   */
  destroy(): void {
    this.exitEditMode()
    this.iframe = null
  }

  /** 向 iframe 发送控制消息 */
  private postToIframe(type: string, data?: unknown): void {
    if (!this.iframe?.contentWindow) return
    try {
      this.iframe.contentWindow.postMessage({ type, data }, '*')
    } catch (err) {
      console.error('[VisualEditor] postMessage 失败:', err)
    }
  }
}
