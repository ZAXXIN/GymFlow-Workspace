Component({
  options: {
    styleIsolation: 'apply-shared',
    addGlobalClass: true
  },
  properties: {},
  data: {},
  lifetimes: {
    attached() {
      console.log('global-bg 组件挂载成功');
    }
  },
  methods: {}
})