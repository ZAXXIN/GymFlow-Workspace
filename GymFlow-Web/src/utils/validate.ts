/**
 * 验证规则集合
 */
export const validateRules = {
  required: (message = '该字段不能为空') => ({
    required: true,
    message,
    trigger: ['blur', 'change']
  }),
  
  email: (message = '请输入有效的邮箱地址') => ({
    type: 'email',
    message,
    trigger: ['blur', 'change']
  }),
  
  phone: (message = '请输入有效的手机号码') => ({
    pattern: /^1[3-9]\d{9}$/,
    message,
    trigger: ['blur', 'change']
  }),
  
  idCard: (message = '请输入有效的身份证号码') => ({
    pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/,
    message,
    trigger: ['blur', 'change']
  }),
  
  number: (message = '请输入数字') => ({
    type: 'number',
    message,
    trigger: ['blur', 'change']
  }),
  
  integer: (message = '请输入整数') => ({
    pattern: /^-?\d+$/,
    message,
    trigger: ['blur', 'change']
  }),
  
  positiveInteger: (message = '请输入正整数') => ({
    pattern: /^[1-9]\d*$/,
    message,
    trigger: ['blur', 'change']
  }),
  
  decimal: (precision = 2, message = '请输入有效的数字') => ({
    pattern: new RegExp(`^\\d+(\\.\\d{1,${precision}})?$`),
    message,
    trigger: ['blur', 'change']
  }),
  
  minLength: (min: number, message = `长度不能少于${min}个字符`) => ({
    min,
    message,
    trigger: ['blur', 'change']
  }),
  
  maxLength: (max: number, message = `长度不能超过${max}个字符`) => ({
    max,
    message,
    trigger: ['blur', 'change']
  }),
  
  rangeLength: (min: number, max: number, message = `长度应在${min}到${max}个字符之间`) => ({
    min,
    max,
    message,
    trigger: ['blur', 'change']
  }),
  
  minValue: (min: number, message = `值不能小于${min}`) => ({
    type: 'number',
    min,
    message,
    trigger: ['blur', 'change']
  }),
  
  maxValue: (max: number, message = `值不能大于${max}`) => ({
    type: 'number',
    max,
    message,
    trigger: ['blur', 'change']
  }),
  
  rangeValue: (min: number, max: number, message = `值应在${min}到${max}之间`) => ({
    type: 'number',
    min,
    max,
    message,
    trigger: ['blur', 'change']
  }),
  
  url: (message = '请输入有效的URL地址') => ({
    type: 'url',
    message,
    trigger: ['blur', 'change']
  }),
  
  password: (message = '密码必须包含字母和数字，长度6-20位') => ({
    pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,20}$/,
    message,
    trigger: ['blur', 'change']
  }),
  
  confirmPassword: (passwordField = 'password', message = '两次输入的密码不一致') => ({
    validator: (rule: any, value: string, callback: any) => {
      const form = rule.field.split('.')[0]
      const password = rule.field.split('.').length > 1 
        ? (rule.formContext?.model?.[form]?.[passwordField] || '')
        : (rule.formContext?.model?.[passwordField] || '')
      
      if (value !== password) {
        callback(new Error(message))
      } else {
        callback()
      }
    },
    trigger: ['blur', 'change']
  })
}

/**
 * 表单验证工具
 */
export class FormValidator {
  private errors: Record<string, string> = {}
  
  /**
   * 验证字段
   */
  validateField(field: string, value: any, rules: any[]): boolean {
    for (const rule of rules) {
      // 必填验证
      if (rule.required && (value === undefined || value === null || value === '' || (Array.isArray(value) && value.length === 0))) {
        this.errors[field] = rule.message || `${field}不能为空`
        return false
      }
      
      // 类型验证
      if (rule.type === 'email' && value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(value)) {
          this.errors[field] = rule.message || '请输入有效的邮箱地址'
          return false
        }
      }
      
      if (rule.type === 'url' && value) {
        try {
          new URL(value)
        } catch {
          this.errors[field] = rule.message || '请输入有效的URL地址'
          return false
        }
      }
      
      // 正则验证
      if (rule.pattern && value) {
        if (!rule.pattern.test(value)) {
          this.errors[field] = rule.message || '格式不正确'
          return false
        }
      }
      
      // 长度验证
      if (value !== undefined && value !== null) {
        const strValue = String(value)
        
        if (rule.min !== undefined && strValue.length < rule.min) {
          this.errors[field] = rule.message || `长度不能少于${rule.min}个字符`
          return false
        }
        
        if (rule.max !== undefined && strValue.length > rule.max) {
          this.errors[field] = rule.message || `长度不能超过${rule.max}个字符`
          return false
        }
      }
      
      // 数值范围验证
      if (rule.type === 'number' || typeof value === 'number') {
        const numValue = Number(value)
        
        if (rule.min !== undefined && numValue < rule.min) {
          this.errors[field] = rule.message || `值不能小于${rule.min}`
          return false
        }
        
        if (rule.max !== undefined && numValue > rule.max) {
          this.errors[field] = rule.message || `值不能大于${rule.max}`
          return false
        }
      }
      
      // 自定义验证器
      if (rule.validator && value) {
        let valid = true
        let errorMessage = ''
        
        rule.validator(
          { field, value, rule },
          value,
          (message?: string) => {
            if (message) {
              valid = false
              errorMessage = message
            }
          }
        )
        
        if (!valid) {
          this.errors[field] = errorMessage || '验证失败'
          return false
        }
      }
    }
    
    delete this.errors[field]
    return true
  }
  
  /**
   * 验证整个表单
   */
  validateForm(formData: Record<string, any>, rules: Record<string, any[]>): boolean {
    this.errors = {}
    let isValid = true
    
    for (const field in rules) {
      if (!this.validateField(field, formData[field], rules[field])) {
        isValid = false
      }
    }
    
    return isValid
  }
  
  /**
   * 获取所有错误
   */
  getErrors(): Record<string, string> {
    return { ...this.errors }
  }
  
  /**
   * 获取指定字段的错误
   */
  getError(field: string): string | undefined {
    return this.errors[field]
  }
  
  /**
   * 清除所有错误
   */
  clearErrors(): void {
    this.errors = {}
  }
  
  /**
   * 清除指定字段的错误
   */
  clearError(field: string): void {
    delete this.errors[field]
  }
}

/**
 * 验证工具函数
 */
export function validateForm(formRef: any): Promise<boolean> {
  return new Promise((resolve) => {
    if (!formRef) {
      resolve(false)
      return
    }
    
    formRef.validate((valid: boolean) => {
      resolve(valid)
    })
  })
}

export function resetForm(formRef: any): void {
  if (formRef) {
    formRef.resetFields()
  }
}

export function clearValidate(formRef: any, fields?: string | string[]): void {
  if (formRef) {
    formRef.clearValidate(fields)
  }
}