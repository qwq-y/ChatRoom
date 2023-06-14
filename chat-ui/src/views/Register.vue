<template>
  <div class="register-wrap">
    <el-form class="register-container">
      <h1 class="title">用户注册</h1>
      <el-form-item>
        <el-input
          type="text"
          v-model="username"
          placeholder="用户名"
          autocomplete="off"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-input
          type="text"
          v-model="uid"
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
          class="register-button"
          type="primary"
          @click="doRegister"
          style="width: 100%"
          >注册
        </el-button>
      </el-form-item>
      <el-row style="text-align: center; margin-top: -10px">
        <el-link type="primary" @click="goLogin">前往登陆</el-link>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import axios from "axios";
import { ElMessageBox } from "element-plus";
import router from "@/router";

export default {
  // eslint-disable-next-line vue/multi-word-component-names
  name: "Register",
  data: function () {
    return {
      uid: null,
      username: "",
      password: "",
    };
  },
  methods: {
    doRegister: function () {
      let uid = this.uid;
      let username = this.username;
      let password = this.password;
      let url = "/api/users/register";
      //TODO
      let params = new URLSearchParams();
      params.append("account", uid);
      params.append("name", username);
      params.append("password", password);
      axios
        .post(url, params, {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        })
        .then((resp) => {
          console.log("Post response: " + resp.data);
          if (resp.data.name != null) {
            ElMessageBox.alert("注册成功", {
              confirmButtonText: "OK",
              type: "success",
            });
            router.push("/login");
          } else {
            ElMessageBox.alert("注册失败", {
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
    goLogin: function () {
      router.push("/login");
    },
  },
};
</script>

<style>
.register-wrap {
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
.register-container {
  border-radius: 10px;
  margin: auto;
  width: 350px;
  padding: 30px 35px 15px 35px;
  //backdrop-filter: saturate(50%) blur(4px);
  background-image: linear-gradient(to left, #5fc3e4 0%, #e55d87 100%);
}
.register-button {
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
