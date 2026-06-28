const { getDefaultConfig } = require('expo/metro-config');

/** @type {import('expo/metro-config').MetroConfig} */
const config = getDefaultConfig(__dirname);

// 시뮬레이터는 127.0.0.1 고정 (localhost/LAN IP 혼선 방지)
config.server = {
  ...config.server,
  host: '127.0.0.1',
  port: 8081,
};

module.exports = config;
