// 权限编码类型
export type PermissionCode = 
  | 'member:view' | 'member:detail' | 'member:add' | 'member:edit' | 'member:delete' | 'member:batch:delete' | 'member:card:renew' | 'member:health:view' | 'member:health:add'
  | 'coach:view' | 'coach:detail' | 'coach:add' | 'coach:edit' | 'coach:delete' | 'coach:batch:delete' | 'coach:schedule:view' | 'coach:schedule:set'
  | 'course:view' | 'course:detail' | 'course:add' | 'course:edit' | 'course:delete' | 'course:schedule:view' | 'course:schedule:set' | 'course:booking:add' | 'course:booking:cancel'
  | 'checkIn:view' | 'checkIn:detail' | 'checkIn:member:add' | 'checkIn:course:add' | 'checkIn:edit' | 'checkIn:delete' | 'checkIn:verify'
  | 'order:view' | 'order:detail' | 'order:add' | 'order:edit' | 'order:cancel' | 'order:delete' | 'order:pay' | 'order:refund'
  | 'product:view' | 'product:detail' | 'product:add' | 'product:edit' | 'product:delete' | 'product:status' | 'product:stock' | 'product:category:view' | 'product:category:manage'
  | 'settings:user:view' | 'settings:user:add' | 'settings:user:edit' | 'settings:user:delete' | 'settings:user:status' | 'settings:user:resetpwd'
  | 'settings:config:view' | 'settings:config:edit'
  | 'common:upload'

// 菜单项接口
export interface MenuItem {
  path: string
  title: string
  icon?: any
  permissions?: PermissionCode[] // 需要的权限
  children?: MenuItem[]
  hidden?: boolean
}

// 用户权限信息
export interface UserPermission {
  userId: number
  username: string
  role: number
  roleCode: string
  permissions: PermissionCode[]
  menus: MenuItem[]
}