<template>
  <div class="reserve-wrap">
    <el-form class="reserve-container">
      <h1 class="title">预定会议</h1>
      <el-form-item>
        <el-input
          type="text"
          v-model="roomid"
          placeholder="会议号"
          autocomplete="off"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-input
          type="password"
          v-model="password"
          placeholder="进入会议密码"
          autocomplete="off"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button
          class="reserve-button"
          type="primary"
          @click="doReserve"
          style="width: 100%"
          >预定
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import axios from "axios";
import { ElMessageBox } from "element-plus";
import router from "@/router";
export default {
  name: "ReserveMeeting",
  data: function () {
    return {
      roomid: null,
    };
  },
  methods: {
    doReserve: function () {
      let roomid = this.roomid;
      let password = this.password;
      let leader = sessionStorage.getItem("account");
      let url = "/api/rooms/reserve";
      //TODO
      axios
        .get(url, {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          params: {
            number: roomid,
            key: password,
            leaderAccount: leader,
          },
        })
        .then((resp) => {
          console.log("Post response: " + resp.data);
          if (resp.data.number != null) {
            ElMessageBox.alert("预定成功", {
              confirmButtonText: "OK",
              type: "success",
            });
            sessionStorage.setItem("roomNumber", resp.data.number);
            router.push("/chat");
          } else {
            ElMessageBox.alert("失败", {
              confirmButtonText: "OK",
              type: "error",
            });
          }
        })
        .catch((err) => {
          console.log("ERROR: " + err);
        });
    },
  },
};
</script>

<style>
.reserve-wrap {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  padding-top: 10%;
  background-image: linear-gradient(to right, #5fc3e4 0%, #e55d87 100%);
}
.reserve-container {
  border-radius: 10px;
  margin: auto;
  width: 350px;
  padding: 30px 35px 15px 35px;
  background-image: linear-gradient(to left, #5fc3e4 0%, #e55d87 100%);
}
.reserve-button {
  background-color: aqua;
  opacity: 0.6;
  backdrop-filter: saturate(80%) blur(9px);
}
.title {
  margin: 0px auto 40px auto;
  text-align: center;
  color: #505458;
}
</style>
