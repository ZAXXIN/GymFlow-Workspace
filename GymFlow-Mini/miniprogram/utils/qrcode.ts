/**
 * 二维码生成工具
 * 使用 weapp-qrcode 的简化版本
 */

// 二维码纠错级别
const QRErrorCorrectLevel = {
  L: 1,
  M: 0,
  Q: 3,
  H: 2
}

// 二维码绘制函数
function drawQrcode(options) {
  const {
    ctx,
    canvasId,
    text,
    width,
    height,
    colorDark = '#000000',
    colorLight = '#ffffff',
    correctLevel = QRErrorCorrectLevel.M
  } = options

  if (!text) {
    console.error('二维码内容为空')
    return
  }

  // 如果没有传入 ctx，使用 canvasId 获取
  if (!ctx && canvasId) {
    const context = wx.createCanvasContext(canvasId)
    drawQRCodeWithContext(context, text, width, height, colorDark, colorLight, correctLevel)
  } else if (ctx) {
    drawQRCodeWithContext(ctx, text, width, height, colorDark, colorLight, correctLevel)
  }
}

/**
 * 使用 canvas context 绘制二维码
 */
function drawQRCodeWithContext(ctx, text, width, height, colorDark, colorLight, correctLevel) {
  // 生成二维码矩阵
  const qrcode = generateQRCode(text, correctLevel)
  const modules = qrcode.modules
  const moduleCount = modules.length
  
  // 计算每个模块的大小
  const cellSize = width / moduleCount
  
  // 绘制背景
  ctx.setFillStyle(colorLight)
  ctx.fillRect(0, 0, width, height)
  
  // 绘制二维码
  ctx.setFillStyle(colorDark)
  for (let row = 0; row < moduleCount; row++) {
    for (let col = 0; col < moduleCount; col++) {
      if (modules[row][col]) {
        const x = col * cellSize
        const y = row * cellSize
        ctx.fillRect(x, y, cellSize, cellSize)
      }
    }
  }
  
  // 执行绘制
  ctx.draw()
}

/**
 * 生成二维码矩阵
 * 简化版 QR 码生成算法
 */
function generateQRCode(text, correctLevel) {
  // 简化版：使用字符转二进制的方式生成
  // 实际项目建议使用完整的 QR 码库
  const modules = []
  const size = 21 // 最小版本大小
  
  // 初始化矩阵
  for (let i = 0; i < size; i++) {
    modules[i] = []
    for (let j = 0; j < size; j++) {
      modules[i][j] = false
    }
  }
  
  // 添加定位图案
  addPositionPattern(modules, 0, 0)
  addPositionPattern(modules, size - 7, 0)
  addPositionPattern(modules, 0, size - 7)
  
  // 将文本转换为二进制数据
  const dataBits = stringToBits(text)
  
  // 填充数据
  fillData(modules, dataBits)
  
  // 添加掩码
  applyMask(modules)
  
  return { modules, moduleCount: size }
}

/**
 * 添加定位图案
 */
function addPositionPattern(modules, startX, startY) {
  // 外部边框
  for (let i = 0; i < 7; i++) {
    for (let j = 0; j < 7; j++) {
      modules[startY + i][startX + j] = true
    }
  }
  // 内部白色
  for (let i = 1; i < 6; i++) {
    for (let j = 1; j < 6; j++) {
      modules[startY + i][startX + j] = false
    }
  }
  // 中心黑色
  for (let i = 2; i < 5; i++) {
    for (let j = 2; j < 5; j++) {
      modules[startY + i][startX + j] = true
    }
  }
}

/**
 * 字符串转二进制
 */
function stringToBits(str) {
  const bits = []
  for (let i = 0; i < str.length; i++) {
    const code = str.charCodeAt(i)
    // 转换为16位二进制
    for (let j = 15; j >= 0; j--) {
      bits.push((code >> j) & 1)
    }
  }
  return bits
}

/**
 * 填充数据到矩阵
 */
function fillData(modules, dataBits) {
  const size = modules.length
  let direction = -1 // 向上
  let x = size - 1
  let y = size - 1
  
  for (let i = 0; i < dataBits.length && x > 0; i++) {
    // 跳过定位图案区域
    if (isSkippable(x, y, size)) {
      // 继续移动
    } else {
      modules[y][x] = dataBits[i] === 1
    }
    
    // 移动位置
    if (direction === -1) {
      y--
      if (y < 0) {
        y = 0
        x -= 2
        direction = 1
      }
    } else {
      y++
      if (y >= size) {
        y = size - 1
        x -= 2
        direction = -1
      }
    }
  }
}

/**
 * 检查位置是否应该跳过（定位图案区域）
 */
function isSkippable(x, y, size) {
  // 左上角定位
  if (x < 7 && y < 7) return true
  // 右上角定位
  if (x >= size - 7 && y < 7) return true
  // 左下角定位
  if (x < 7 && y >= size - 7) return true
  return false
}

/**
 * 应用掩码
 */
function applyMask(modules) {
  const size = modules.length
  for (let i = 0; i < size; i++) {
    for (let j = 0; j < size; j++) {
      // 简单掩码：根据行列奇偶性反转
      if ((i + j) % 2 === 0) {
        modules[i][j] = !modules[i][j]
      }
    }
  }
}

module.exports = drawQrcode