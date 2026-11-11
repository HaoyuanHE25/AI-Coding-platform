/**
 * 代码生成类型枚举
 */
export enum CodeGenTypeEnum {
  HTML = 'html',
  MULTI_FILE = 'multi_file',
  VUE_PROJECT = 'vue_project',
}

/**
 * 代码生成类型配置
 */
export const CODE_GEN_TYPE_CONFIG = {
  [CodeGenTypeEnum.HTML]: {
    label: '原生 HTML 模式',
    value: CodeGenTypeEnum.HTML,
  },
  [CodeGenTypeEnum.MULTI_FILE]: {
    label: '原生多文件模式',
    value: CodeGenTypeEnum.MULTI_FILE,
  },
  [CodeGenTypeEnum.VUE_PROJECT]: {
    label: 'Vue 项目模式',
    value: CodeGenTypeEnum.VUE_PROJECT,
  },
}

/** 代码生成类型选项（用于下拉选择） */
export const CODE_GEN_TYPE_OPTIONS = Object.values(CodeGenTypeEnum).map((value) => ({
  label: CODE_GEN_TYPE_CONFIG[value].label,
  value,
}))

/** 根据代码生成类型值获取标签 */
export const getCodeGenTypeLabel = (value?: string): string => {
  if (!value) return ''
  return CODE_GEN_TYPE_CONFIG[value as CodeGenTypeEnum]?.label ?? value
}
