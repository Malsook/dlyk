<template>
  <el-button type="primary" class="btn" @click="batchExportExcel">批量导出(Excel)</el-button>
  <el-button type="success" class="btn" @click="chooseExportExcel">选择导出(Excel)</el-button>

  <el-table
      :data="customerList"
      style="width: 100%"
      @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50"/>
    <el-table-column type="index" label="序号" width="65"/>
    <el-table-column property="ownerDO.name" label="负责人" width="120" />
    <el-table-column property="activityDO.name" label="所属活动"/>
    <el-table-column label="姓名">
      <template #default="scope">
        <a href="javascript:" @click="view(scope.row.id)">{{ scope.row.clueDO.fullName }}</a>
      </template>
    </el-table-column>
    <el-table-column property="appellationDO.typeValue" label="称呼"/>
    <el-table-column property="clueDO.phone" label="手机" width="120"/>
    <el-table-column property="clueDO.weixin" label="微信" width="120"/>
    <el-table-column property="needLoanDO.typeValue" label="是否贷款"/>
    <el-table-column property="intentionStateDO.typeValue" label="意向状态"/>
    <el-table-column property="stateDO.typeValue" label="线索状态"/>
    <el-table-column property="sourceDO.typeValue" label="线索来源"/>
    <el-table-column property="intentionProductDO.name" label="意向产品"/>
    <el-table-column property="nextContactTime" label="下次联系时间" width="165"/>
    <el-table-column label="操作" width="85">
      <template #default="scope">
        <el-button type="primary" @click="view(scope.row.id)">详情</el-button>
      </template>
    </el-table-column>
  </el-table>
  <p>
    <el-pagination
        background
        layout="prev, pager, next"
        :page-size="pageSize"
        :total="total"
        @prev-click="page"
        @next-click="page"
        @current-change="page"/>
  </p>
</template>

<script>
import {defineComponent} from 'vue'
import {doGet} from "../http/httpRequest.js";
import axios from "axios";
import {getToken, messageTip} from "../util/util.js";

export default defineComponent({
  name: "CustomerView",

  data() {
    return {
      //客户列表数据对象，初始值是空
      customerList : [{
        clueDO : {},
        ownerDO : {},
        activityDO : {},
        appellationDO : {},
        needLoanDO : {},
        intentionStateDO : {},
        stateDO : {},
        sourceDO : {},
        intentionProductDO : {},
      }],
      //分页时每页显示多少条数据
      pageSize : 0,
      //总共有多少条
      total : 0,
      //客户的id数组，初始值是空
      customerIdArray : []
    }
  },

  mounted() {
    this.getData(1);
  },

  methods : {
    //勾选或者取消勾选时，触发该函数(已经勾选的数据或传给我们这个函数)
    handleSelectionChange(selectionnDataArray) {
      this.customerIdArray = [];
      console.log(selectionnDataArray);
      selectionnDataArray.forEach(data => {
        let customerId = data.id;
        this.customerIdArray.push(customerId);
      })
    },

    //获取线索分页列表数据
    getData(current) {
      doGet("/api/customers", {
        current : current //当前页，前面是参数名，后面是参数值
      }).then(resp => {
        if (resp.data.code === 200) {
          this.customerList = resp.data.data.list; // resp.data  =  R 对象
          this.pageSize = resp.data.data.pageSize;
          this.total = resp.data.data.total;
        }
      })
    },

    //分页函数
    page(number) { //number值是element-plus回调时传给我们的，number这个值就是当前页
      this.getData(number);
    },

    //批量导出客户的Excel数据
    exportExcel(ids) {
      let token = getToken();
      //1、向后端发送一个请求 （我们来实现）
      let iframe = document.createElement("iframe")
      if (ids) {
        iframe.src = axios.defaults.baseURL + "/api/exportExcel?Authorization="+token + "&ids="+ ids;
      } else {
        iframe.src = axios.defaults.baseURL + "/api/exportExcel?Authorization="+token
      }
      iframe.style.display = "none"; //iframe隐藏，页面上不要显示出来
      document.body.appendChild(iframe);
      //2、后端查询数据库的数据，把数据写入Excel，然后把Excel以IO流的方式输出到前端浏览器（我们来实现）
      //3、前端浏览器弹出一个下载框进行文件下载（浏览器本身实现的，不需要我们去实现）
    },

    //批量导出客户的Excel数据
    batchExportExcel() {
      this.exportExcel(null);
    },

    //选择导出
    chooseExportExcel() {
      if (this.customerIdArray.length <= 0) {
        messageTip("请选择要导出的数据", "warning");
        return;
      }
      // ids = "1,3,6,11,15";
      let ids = this.customerIdArray.join(",");
      this.exportExcel(ids);
    }
  }
})
</script>

<style scoped>
.el-table {
  margin-top: 10px;
}
</style>