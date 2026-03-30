// 会员端添加健康档案页面
import { addHealthRecord } from '../../../services/api/member.api'
import { showToast, showLoading, hideLoading } from '../../../utils/wx-util'

Page({
  data: {
    // 表单数据
    form: {
      recordDate: '',
      height: 170,
      weight: 65,
      bodyFatPercentage: 20,
      muscleMass: 45,
      bmi: '',
      chestCircumference: 95,
      waistCircumference: 85,
      hipCircumference: 95,
      bloodPressure: '120/80',
      heartRate: 75,
      notes: '',
    },

    // 提交状态
    submitting: false,
    
    // 会员ID（从页面参数获取）
    memberId: null,
  },

  onLoad: function (options) {
    // 从页面参数获取 memberId
    const memberId = options.memberId
    if (!memberId) {
      showToast('缺少会员ID参数', 'none')
      return
    }

    this.setData({ memberId: memberId })

    // 设置默认日期为今天
    const today = new Date()
    const year = today.getFullYear()
    const month = String(today.getMonth() + 1).padStart(2, '0')
    const day = String(today.getDate()).padStart(2, '0')

    this.setData({
      'form.recordDate': `${year}-${month}-${day}`
    })

    this.calculateBMI()
  },

  /**
   * 输入处理
   */
  onInput: function (e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail

    this.setData({
      [`form.${field}`]: value
    })
  },

  /**
   * 计算BMI
   */
  calculateBMI: function () {
    const { weight, height } = this.data.form

    if (weight && height) {
      const heightInMeter = height / 100
      const bmi = parseFloat(weight) / (heightInMeter * heightInMeter)
      this.setData({
        'form.bmi': bmi.toFixed(1)
      })
    }
  },

  /**
   * 提交表单
   */
  onSubmit: function () {
    var that = this
    var form = this.data.form
    var memberId = this.data.memberId

    if (!memberId) {
      showToast('会员ID不存在', 'none')
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

    addHealthRecord(memberId, params).then(function () {
      hideLoading()
      showToast('保存成功', 'success')

      // 返回上一页
      setTimeout(function () {
        wx.navigateBack()
      }, 1500)

    }).catch(function (error) {
      hideLoading()
      showToast(error.message || '保存失败', 'none')
      that.setData({ submitting: false })
    })
  }
})