// 会员端添加健康档案页面
import { userStore } from '../../../stores/user.store'
import { addHealthRecord } from '../../../services/api/member.api'
import { showToast, showLoading, hideLoading } from '../../../utils/wx-util'

Page({
  data: {
    // 表单数据
    form: {
      recordDate: '',
      weight: '',
      bodyFatPercentage: '',
      muscleMass: '',
      bmi: '',
      chestCircumference: '',
      waistCircumference: '',
      hipCircumference: '',
      bloodPressure: '',
      heartRate: '',
      notes: ''
    },
    
    // 日期选择器
    datePickerVisible: false,
    
    // 提交状态
    submitting: false,

    dateOptions: []
  },

  onLoad: function() {
    // 设置默认日期为今天
    const today = new Date()
    const year = today.getFullYear()
    const month = String(today.getMonth() + 1).padStart(2, '0')
    const day = String(today.getDate()).padStart(2, '0')
    
    this.setData({
      'form.recordDate': `${year}-${month}-${day}`
    })
    
    // 生成日期选项（最近30天）
    const dateOptions = []
    for (let i = 0; i < 30; i++) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      dateOptions.push(`${y}-${m}-${d}`)
    }
    this.setData({ dateOptions })
  },

  /**
   * 日期确认
   */
  onDateConfirm: function() {
    // 使用当前选中的日期，这里简化处理，实际应该从picker-view获取
    const { dateOptions } = this.data
    if (dateOptions && dateOptions.length > 0) {
      this.setData({
        'form.recordDate': dateOptions[0],
        datePickerVisible: false
      })
    }
  },

  /**
   * 输入处理
   */
  onInput: function(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    
    this.setData({
      [`form.${field}`]: value
    })
  },

  /**
   * 选择日期
   */
  onDatePickerTap: function() {
    this.setData({ datePickerVisible: true })
  },

  /**
   * 日期变化
   */
  onDateChange: function(e) {
    const { value } = e.detail
    this.setData({
      'form.recordDate': value[0],
      datePickerVisible: false
    })
  },

  /**
   * 取消日期选择
   */
  onDateCancel: function() {
    this.setData({ datePickerVisible: false })
  },

  /**
   * 计算BMI
   */
  calculateBMI: function() {
    const { weight, height } = this.data.form
    const userHeight = userStore.height
    
    if (weight && userHeight) {
      const heightInMeter = userHeight / 100
      const bmi = parseFloat(weight) / (heightInMeter * heightInMeter)
      this.setData({
        'form.bmi': bmi.toFixed(1)
      })
    }
  },

  /**
   * 提交表单
   */
  onSubmit: function() {
    var that = this
    var form = this.data.form
    
    // 表单验证
    if (!form.recordDate) {
      showToast('请选择记录日期', 'none')
      return
    }
    
    if (!form.weight) {
      showToast('请输入体重', 'none')
      return
    }
    
    if (isNaN(parseFloat(form.weight)) || parseFloat(form.weight) <= 0) {
      showToast('请输入有效的体重', 'none')
      return
    }
    
    // 可选字段验证
    if (form.bodyFatPercentage && (isNaN(parseFloat(form.bodyFatPercentage)) || 
        parseFloat(form.bodyFatPercentage) < 0 || parseFloat(form.bodyFatPercentage) > 100)) {
      showToast('请输入有效的体脂率(0-100)', 'none')
      return
    }
    
    this.setData({ submitting: true })
    showLoading('保存中...')
    
    var memberId = userStore.memberId
    if (!memberId) {
      wx.navigateTo({ url: '/pages/common/login/index' })
      return
    }
    
    // 转换数据类型
    var params = {
      recordDate: form.recordDate,
      weight: parseFloat(form.weight)
    }
    
    if (form.bodyFatPercentage) params.bodyFatPercentage = parseFloat(form.bodyFatPercentage)
    if (form.muscleMass) params.muscleMass = parseFloat(form.muscleMass)
    if (form.bmi) params.bmi = parseFloat(form.bmi)
    if (form.chestCircumference) params.chestCircumference = parseFloat(form.chestCircumference)
    if (form.waistCircumference) params.waistCircumference = parseFloat(form.waistCircumference)
    if (form.hipCircumference) params.hipCircumference = parseFloat(form.hipCircumference)
    if (form.bloodPressure) params.bloodPressure = form.bloodPressure
    if (form.heartRate) params.heartRate = parseInt(form.heartRate)
    if (form.notes) params.notes = form.notes
    
    addHealthRecord(memberId, params).then(function() {
      hideLoading()
      showToast('保存成功', 'success')
      
      // 返回上一页
      setTimeout(function() {
        wx.navigateBack()
      }, 1500)
      
    }).catch(function(error) {
      hideLoading()
      showToast(error.message || '保存失败', 'none')
      that.setData({ submitting: false })
    })
  }
})