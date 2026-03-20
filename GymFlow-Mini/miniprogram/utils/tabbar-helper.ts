import { userStore } from '../stores/user.store'

/**
 * TabBar 辅助类
 * 用于页面中获取当前选中的 Tab 索引
 */
export class TabBarHelper {
  /**
   * 根据当前页面路径获取选中的 Tab 索引
   */
  static getSelectedIndex(pagePath: string): number {
    const role = userStore.role
    
    if (role === 'COACH') {
      const coachPaths = [
        '/pages/coach/home/index',
        '/pages/coach/student-list/index',
        '/pages/coach/finance/index',
        '/pages/coach/my/index'
      ]
      const index = coachPaths.findIndex(path => pagePath.includes(path))
      return index >= 0 ? index : 0
    } else {
      const memberPaths = [
        '/pages/member/home/index',
        '/pages/member/booking/index',
        '/pages/member/my/index'
      ]
      const index = memberPaths.findIndex(path => pagePath.includes(path))
      return index >= 0 ? index : 0
    }
  }
}