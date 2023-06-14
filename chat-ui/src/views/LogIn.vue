<template>
  <div class="login-wrap">
    <el-form class="login-container">
      <h1 class="title">用户登陆</h1>
      <el-form-item>
        <el-input
          type="text"
          v-model="userid"
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
      userid: null,
      password: "",
    };
  },
  methods: {
    doLogin: function () {
      let uid = this.userid;
      let password = this.password;
      let url = "/api/users/login";
      // let params = new URLSearchParams();
      // params.append("account", uid);
      // params.append("password", password);
      //TODO
      axios
        .get(url, {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          params: {
            account: uid,
            password: password,
          },
        })
        .then((resp) => {
          console.log("Post response: " + resp.data);
          if (resp.data.name != null) {
            ElMessageBox.alert("登陆成功", {
              confirmButtonText: "OK",
              type: "success",
            });
            sessionStorage.setItem("name", resp.data.name);
            router.push("/chat");
          } else {
            ElMessageBox.alert("登陆失败", {
              confirmButtonText: "OK",
              type: "error",
            });
            console.log("注册失败");
          }
        })
        .catch((err) => {
          console.log("ERROR: " + err);
        });
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
