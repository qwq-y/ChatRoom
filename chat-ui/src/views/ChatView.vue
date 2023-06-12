<template>
  <div class="chat">
    <div class="list-pane">
      <div class="user-pane">
        <div class="user-count">
          <h2>当前在线人数：{{ userCount }}</h2>
        </div>
        <div class="user-list">
          <div class="user" v-for="user in userList" :key="user">
            <el-image
              class="user-img"
              :src="require('@/assets/images/user.jpg')"
            ></el-image>
            <p class="username">{{ user }}</p>
          </div>
        </div>
      </div>
    </div>
    <div class="chat-pane">
      <div class="chat-header">
        <h2>Easy-Chat - {{ nickname }}</h2>
      </div>
      <div class="chat-message" ref="chatHistory">
        <div class="user-message" v-for="message in messages" :key="message">
          <div class="img">
            <el-image
              class="user-img"
              :src="require('@/assets/images/user.jpg')"
            ></el-image>
          </div>
          <div class="message">
            <div class="username">
              {{ message.name }} -> {{ message.to }}
              <span class="time">{{ message.time }}</span>
            </div>
            <div class="text user-text" v-if="nickname === message.name">
              {{ message.msg }}
            </div>
            <div class="text" v-if="nickname !== message.name">
              {{ message.msg }}
            </div>
          </div>
        </div>
      </div>
      <div class="chat-textarea">
        <el-input
          v-model="text"
          class="user-textarea"
          type="textarea"
          resize="none"
          @keydown.enter="sendButton"
        ></el-input>
        <el-select
          v-model="value"
          placeholder="请选择user"
          @change="$forceUpdate()"
        >
          <el-option
            v-for="user in userList"
            :key="user.id"
            :label="user.id"
            :value="user.toString()"
          ></el-option>
          <el-option key="0" label="All" value="All@" />
        </el-select>
        <el-button type="primary" class="send-button" @click="sendButton"
          >发送</el-button
        >
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onActivated } from "vue";
import router from "@/router";

let nickname = ref();
let socket: WebSocket;

onActivated(() => {
  nickname.value = sessionStorage.getItem("name");
  // 查询是否设置了昵称
  if (nickname.value == null) {
    router.push("/");
    return;
  }

  // 查询浏览器是否支持 WebSocket
  if (typeof WebSocket == "undefined") {
    alert("您的浏览器不支持 WebSocket");
    router.push("/");
    return;
  }

  // 开启 WebSocket 服务，这里可以设置websocket链接地址
  let socketHost = "localhost";
  let socketPort = "8888";
  let socketUrl =
    "ws://" + socketHost + ":" + socketPort + "/socket/" + nickname.value;
  socket = new WebSocket(socketUrl);

  // 连接服务器
  socket.onopen = () => {
    hellopacket.name = nickname.value;
    hellopacket.rsapk = publicRSAKey.valueOf();
    hellopacket.dsapk = DSApublicKey.valueOf();
    // console.log(hellopacket);
    socket.send(JSON.stringify(hellopacket));
    console.log("已连接至服务器");
  };

  // 浏览器接收服务端发送的消息
  socket.onmessage = (msg) => {
    let data = JSON.parse(msg.data);
    if (data.userlist) {
      // 接收用户列表消息
      userList.value = data.userlist;
      userCount.value = data.userlist.length;
    } else if (data.rsamap) {
      //更新rsa表
      rsapktable.value = data.rsamap;
      // console.log("rsa\n");
      // console.log(rsapktable);
    } else if (data.dsamap) {
      //更新dsa表
      dsapktable.value = data.dsamap;
      // console.log("dsa\n");
      // console.log(dsapktable);
    } else if (data.upd) {
      //leader生成新会议密钥
      key = generatekey(32);
      // console.log(key);
      for (const s in rsapktable.value) {
        updatepacket.key = encryptMessage(key, rsapktable.value[s]);
        updatepacket.sendto = s;
        socket.send(JSON.stringify(updatepacket));
      }
    } else if (data.key) {
      //参会者更新会议密钥
      key = decryptMessage(data.key);
      // console.log(key);
    } else {
      // 接收消息
      let dsapk = dsapktable.value[data.name];
      dsapk = dsapk.slice(1, -1);
      // console.log(data.msg);
      data.msg = decryptSK(data.msg);
      // console.log(data.sign);
      const isValid = nacl.sign.detached.verify(
        Uint8Array.from(data.msg, (c) => c.charCodeAt(0)),
        new Uint8Array(
          data.sign.match(/.{1,2}/g).map((byte: string) => parseInt(byte, 16))
        ),
        strToUint8Array(dsapk)
      );
      // console.log(isValid);
      if (isValid) {
        messages.value.push(data);
      }

      // 获取节点
      let chatHistory = document.getElementsByClassName("chat-message")[0];
      if (chatHistory.scrollHeight >= chatHistory.clientHeight) {
        setTimeout(function () {
          //设置滚动条到最底部
          // chatHistory.scrollTop = chatHistory.scrollHeight;
        }, 0);
      }
    }
  };
  // 关闭服务
  socket.onclose = () => {
    console.log("WebSocket 服务已关闭");
  };
  // 错误事件
  socket.onerror = () => {
    console.log("WebSocket 服务发生错误");
  };
});
function strToUint8Array(s) {
  const obj = JSON.parse(`{${s}}`);
  const len = Object.keys(obj).length;
  const result = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    result[i] = obj[i];
  }
  return result;
}
// 日期转换
const formatTime = (date: Date) => {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hour = date.getHours();
  const minute = date.getMinutes();
  const second = date.getSeconds();

  return (
    [year, month, day].map(formatNumber).join("-") +
    " " +
    [hour, minute, second].map(formatNumber).join(":")
  );
};
const formatNumber = (n: number) => {
  const s = n.toString();
  return s[1] ? s : "0" + s;
};
import CryptoJS from "crypto-js";

// 用户数量
let userCount = ref(0);

// 用户列表
let userList = ref([]);

// 信息框
let text = ref("");

//选项
let value = ref("");

// 信息列表
let messages = ref([]);

//public key table
let rsapktable = ref({});

let dsapktable = ref({});

// 会议密钥
let key = "";

//生成会议密钥（仅leader）
function generatekey(num) {
  let library =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
  let key = "";
  for (var i = 0; i < num; i++) {
    let randomPoz = Math.floor(Math.random() * library.length);
    key += library.substring(randomPoz, randomPoz + 1);
  }
  return key;
}
import jsrsasign from "jsrsasign";
//RSA key用于加密会议密钥
const rsaKeypair = jsrsasign.KEYUTIL.generateKeypair("RSA", 1024);
const privateRSAKey = jsrsasign.KEYUTIL.getPEM(
  rsaKeypair.prvKeyObj,
  "PKCS8PRV"
);
const publicRSAKey = jsrsasign.KEYUTIL.getPEM(rsaKeypair.pubKeyObj);
import { JSEncrypt } from "jsencrypt";
// RSA加密信息
function encryptMessage(message, key) {
  var encrypt = new JSEncrypt();
  encrypt.setPublicKey(key);
  var encrypted = encrypt.encrypt(JSON.stringify(message));
  // console.log("加密前数据:%o", message);
  // console.log("加密后数据:%o", encrypted);
  return encrypted;
}
// RSA解密信息
function decryptMessage(encryptedMessage) {
  var decrypt = new JSEncrypt();
  decrypt.setPrivateKey(privateRSAKey);
  var uncrypted = decrypt.decrypt(encryptedMessage);
  // console.log(uncrypted);
  uncrypted = uncrypted.slice(1, -1);
  // console.log("解密后数据:%o", uncrypted);
  return uncrypted;
}
//用于签名的EdDSA
import nacl from "tweetnacl";
const DSAkeyPair = nacl.sign.keyPair();
const DSApublicKey = DSAkeyPair.publicKey;
const DSAprivateKey = DSAkeyPair.secretKey;
//用户加入，给服务器发公钥
let hellopacket = {
  type: "Hello",
  name: "",
  rsapk: "",
  dsapk: "",
};
//会议密钥用来加密解密信息（AES）
function encryptSK(message) {
  return CryptoJS.AES.encrypt(JSON.stringify(message), key).toString();
}
function decryptSK(message) {
  const bytes = CryptoJS.AES.decrypt(message, key);
  return JSON.parse(bytes.toString(CryptoJS.enc.Utf8));
}
//leader把新的会议密钥分发下去
let updatepacket = {
  key: "",
  sendto: "",
};
// 信息,to是后来加的，用来指定目标发私聊 sign是签名
let message = {
  sign: "",
  name: "",
  time: "",
  msg: "",
  to: "",
};
// 将字节数组转换为十六进制字符串
function toHexString(byteArray) {
  return Array.prototype.map
    .call(byteArray, function (byte) {
      return ("0" + (byte & 0xff).toString(16)).slice(-2);
    })
    .join("");
}
// 发送信息
const sendButton = (event: { preventDefault: () => void }) => {
  event.preventDefault();
  if (text.value != null && text.value !== "" && nickname.value != null) {
    message.sign = toHexString(
      nacl.sign.detached(
        Uint8Array.from(text.value, (c) => c.charCodeAt(0)),
        DSAprivateKey
      )
    );
    message.name = nickname.value;
    message.time = formatTime(new Date());
    message.to = value.value;
    message.msg = encryptSK(text.value);
    socket.send(JSON.stringify(message));
    message.msg = "";
    text.value = "";
  }
};
</script>

<style lang="scss">
@import "../assets/css/chat";
</style>
