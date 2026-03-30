// utils/qrcode.ts
import QRCode from 'qrcode'

// 错误纠正级别
export const QRErrorCorrectLevel = {
  L: 1,
  M: 0,
  Q: 3,
  H: 2
}

/**
 * 使用 Canvas 2D 上下文绘制二维码
 */
async function drawQRCodeWithContext(
  ctx: CanvasRenderingContext2D,
  text: string,
  width: number,
  height: number,
  colorDark: string,
  colorLight: string,
  correctLevel: number
): Promise<void> {
  try {
    // 微信小程序 Canvas 2D 中，canvas 元素需要通过其他方式获取
    // 我们不能直接使用 ctx.canvas，需要从外部传入 canvas 节点
    
    // 清空画布
    ctx.fillStyle = colorLight
    ctx.fillRect(0, 0, width, height)
    
    // 由于无法直接获取 canvas 节点，使用 CanvasRenderingContext2D 直接绘制二维码
    // 生成二维码数据
    const qrData = await QRCode.toDataURL(text, {
      width: width,
      margin: 0,
      color: {
        dark: colorDark,
        light: colorLight
      },
      errorCorrectionLevel: correctLevel === 0 ? 'M' : 'L'
    })
    
    // 创建 Image 对象并绘制
    return new Promise((resolve, reject) => {
      const img = ctx.canvas.createImage()
      img.onload = () => {
        ctx.drawImage(img, 0, 0, width, height)
        resolve()
      }
      img.onerror = reject
      img.src = qrData
    })
  } catch (error) {
    console.error('绘制二维码失败:', error)
    throw error
  }
}

export default drawQRCodeWithContext