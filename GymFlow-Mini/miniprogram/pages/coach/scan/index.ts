// 教练端扫码页面
import { scanCheckin, verifyCodeCheckin } from '../../../services/api/checkin.api'
import { showToast, showLoading, hideLoading } from '../../../utils/wx-util'

Page({
  data: {
    // 是否显示相机（用于UI）
    showCamera: true,
    
    // 手电筒状态
    torchOn: false,
    
    // 是否显示手动输入弹窗
    showInputModal: false,
    
    // 输入的数字码
    inputCode: '',
    
    // 是否显示核销结果
    showResult: false,
    
    // 核销结果
    resultSuccess: false,
    resultMessage: ''
  },

  onLoad() {
    // 页面加载时自动开始扫码
    this.startScan()
  },

  /**
   * 开始扫码
   */
  async startScan() {
    try {
      const { result } = await wx.scanCode({
        onlyFromCamera: false,
        scanType: ['qrCode']
      })
      
      // 处理扫码结果
      await this.handleScanResult(result)
      
    } catch (error: any) {
      if (error.errMsg && error.errMsg.indexOf('cancel') > -1) {
        // 用户取消扫码，留在当前页面
        return
      }
      this.showResult(false, error.message || '扫码失败')
    }
  },

  /**
   * 处理扫码结果
   */
  async handleScanResult(result: string) {
    showLoading('核销中...')
    
    try {
      // 验证是否是有效的签到码
      if (!result || !result.startsWith('gymflow://checkin')) {
        hideLoading()
        this.showResult(false, '无效的签到码')
        return
      }
      
      // 调用核销接口
      await scanCheckin({ code: result })
      
      hideLoading()
      this.showResult(true, '核销成功')
      
    } catch (error: any) {
      hideLoading()
      this.showResult(false, error.message || '核销失败')
    }
  },

  /**
   * 显示核销结果
   */
  showResult(success: boolean, message: string) {
    this.setData({
      showResult: true,
      resultSuccess: success,
      resultMessage: message
    })
  },

  /**
   * 结果确认
   */
  onResultConfirm() {
    this.setData({ showResult: false })
    
    // 核销成功后返回上一页
    if (this.data.resultSuccess) {
      setTimeout(() => {
        wx.navigateBack()
      }, 300)
    }
  },

  /**
   * 切换手电筒
   */
  toggleTorch() {
    const { torchOn } = this.data
    
    // 实际手电筒控制需要结合 camera 组件
    this.setData({
      torchOn: !torchOn
    })
    
    showToast(!torchOn ? '手电筒已开启' : '手电筒已关闭', 'none')
  },

  /**
   * 打开手动输入
   */
  onInputCode() {
    this.setData({
      showInputModal: true,
      inputCode: ''
    })
  },

  /**
   * 关闭手动输入
   */
  onCloseModal() {
    this.setData({
      showInputModal: false,
      inputCode: ''
    })
  },

  /**
   * 输入变化
   */
  onCodeInput(e: any) {
    this.setData({
      inputCode: e.detail.value
    })
  },

  /**
   * 确认手动输入
   */
  async onConfirmCode() {
    const { inputCode } = this.data
    
    if (inputCode.length !== 6) {
      showToast('请输入6位数字码', 'none')
      return
    }
    
    // 关闭弹窗
    this.onCloseModal()
    
    showLoading('核销中...')
    
    try {
      // 解析数字码（需要从后端获取bookingId）
      // 这里简化处理，实际应该先通过数字码查询预约ID
      // 或者直接调用带数字码的核销接口
      await verifyCodeCheckin({
        bookingId: 0, // 需要根据数字码查询
        checkinCode: inputCode
      })
      
      hideLoading()
      this.showResult(true, '核销成功')
      
    } catch (error: any) {
      hideLoading()
      this.showResult(false, error.message || '核销失败')
    }
  },

  /**
   * 页面卸载
   */
  onUnload() {
    // 清理状态
    this.setData({
      showCamera: false,
      showInputModal: false,
      showResult: false
    })
  }
})