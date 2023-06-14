import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import NameView from "../views/NameView.vue";
import ChatView from "../views/ChatView.vue";
import LogIn from "@/views/LogIn.vue";
import Register from "@/views/Register.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "name",
    component: NameView,
    meta: {
      keepAlive: true,
    },
  },
  {
    path: "/chat",
    name: "chat",
    component: ChatView,
    meta: {
      keepAlive: true,
    },
  },
  {
    path: "/login",
    name: "LogIn",
    component: LogIn,
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
