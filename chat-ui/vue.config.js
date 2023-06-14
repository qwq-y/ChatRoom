const { defineConfig } = require("@vue/cli-service");
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    host: "localhost",
    port: 8080,
    proxy: {
      "/api": {
        target: "http://0.0.0.0:8888",
        changeOrigin: true,
        pathRewrite: {
          "/api": "http://0.0.0.0:8888",
        },
      },
    },
  },
});
