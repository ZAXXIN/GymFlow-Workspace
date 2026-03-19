Component({
  options: {
    multipleSlots: true,
    // 关键：允许全局样式影响组件，且组件类名可被全局样式覆盖
    styleIsolation: 'apply-shared',
    addGlobalClass: true
  },
  properties: {},
  data: {},
  lifetimes: {
    attached() {
      // 调试用，可删除
      console.log('page-layout 组件挂载成功');
    }
  },
  methods: {}
})