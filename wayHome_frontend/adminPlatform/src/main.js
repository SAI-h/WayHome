import Vue from 'vue'
import App from './App.vue'

// 路由
import VueRouter from 'vue-router'
Vue.use(VueRouter);
import router from './router'

// 屏幕自适应
import VScaleScreen from 'v-scale-screen'
Vue.use(VScaleScreen);

Vue.config.productionTip = false

import {
  Button, Input, Form, FormItem,
  Row, Col, Menu, Submenu, MenuItemGroup, MenuItem,
  Container, Aside, Main, Header,
  Table, TableColumn, Popover, Tag,
  ButtonGroup, Select, Option, Empty,
  TimePicker, Checkbox, CheckboxGroup,
  Card, Descriptions, DescriptionsItem, Transfer, 
  Avatar, Collapse, CollapseItem, Dialog,
  CheckboxButton, Divider, Radio, RadioGroup,
  Pagination, Loading, Result
} from 'element-ui'

Vue.component(Button.name, Button);
Vue.component(Input.name, Input);
Vue.component(Form.name, Form);
Vue.component(FormItem.name, FormItem);
Vue.component(Row.name, Row);
Vue.component(Col.name, Col);
Vue.component(Menu.name, Menu);
Vue.component(Submenu.name, Submenu);
Vue.component(MenuItemGroup.name, MenuItemGroup);
Vue.component(MenuItem.name, MenuItem);
Vue.component(Container.name, Container);
Vue.component(Aside.name, Aside);
Vue.component(Main.name, Main);
Vue.component(Header.name, Header);
Vue.component(Table.name, Table);
Vue.component(TableColumn.name, TableColumn);
Vue.component(Popover.name, Popover);
Vue.component(Tag.name, Tag);
Vue.component(ButtonGroup.name, ButtonGroup);
Vue.component(Select.name, Select);
Vue.component(Option.name, Option);
Vue.component(Empty.name, Empty);
Vue.component(TimePicker.name, TimePicker);
Vue.component(Checkbox.name, Checkbox);
Vue.component(CheckboxGroup.name, CheckboxGroup);
Vue.component(Card.name, Card);
Vue.component(Descriptions.name, Descriptions);
Vue.component(DescriptionsItem.name, DescriptionsItem);
Vue.component(Transfer.name, Transfer);
Vue.component(Avatar.name, Avatar);
Vue.component(Collapse.name, Collapse);
Vue.component(CollapseItem.name, CollapseItem);
Vue.component(Dialog.name, Dialog);
Vue.component(CheckboxButton.name, CheckboxButton);
Vue.component(Divider.name, Divider);
Vue.component(Radio.name, Radio);
Vue.component(RadioGroup.name, RadioGroup);
Vue.component(Pagination.name, Pagination);
Vue.component(Result.name, Result);
Vue.use(Loading.directive);

new Vue({
  render: h => h(App),
  beforeCreate() {
    Vue.prototype.$bus = this; // 设置全局事件总线
  },
  router
}).$mount('#app')
