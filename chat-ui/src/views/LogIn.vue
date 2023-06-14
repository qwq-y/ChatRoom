<template>
  <div class="login-wrap">
    <el-form class="login-container">
      <h1 class="title">用户登陆</h1>
      <el-form-item>
        <el-input
          type="text"
          v-model="username"
          placeholder="用户账号"
          autocomplete="off"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-input
          type="password"
          v-model="password"
          placeholder="用户密码"
          autocomplete="off"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button
          class="login-button"
          type="primary"
          @click="doLogin"
          style="width: 100%"
          >登陆
        </el-button>
      </el-form-item>
      <el-row style="text-align: center; margin-top: -10px">
        <el-link type="primary" @click="goRegister">注册账号</el-link>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import axios from "axios";
import { ElMessageBox } from "element-plus";
import router from "@/router";
export default {
  name: "LogIn",
  data: function () {
    return {
      username: "",
      password: "",
    };
  },
  methods: {
    doLogin: function () {
      let username = this.username;
      let password = this.password;
      let url = "api/login";
      //TODO
    },
    goRegister: function () {
      router.push("/register");
    },
  },
};
</script>

<style>
.login-wrap {
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
.login-container {
  border-radius: 10px;
  margin: auto;
  width: 350px;
  padding: 30px 35px 15px 35px;
  //backdrop-filter: saturate(70%) blur(8px);
  background-image: linear-gradient(to left, #5fc3e4 0%, #e55d87 100%);
}
.login-button {
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
