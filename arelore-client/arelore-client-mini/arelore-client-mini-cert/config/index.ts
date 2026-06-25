import { defineConfig } from '@tarojs/cli';

/** 构建时写入；小程序运行时无 Node 的 process，需靠 DefinePlugin 整段替换为字符串字面量 */
const apiBaseFromEnv = process.env.TARO_APP_API_BASE_URL || '';

export default defineConfig<'webpack5'>({
  projectName: 'arelore-client-mini-cert',
  date: '2026-04-20',
  designWidth: 750,
  deviceRatio: {
    640: 2.34 / 2,
    750: 1,
    828: 1.81 / 2
  },
  sourceRoot: 'src',
  outputRoot: 'dist',
  framework: 'react',
  compiler: {
    type: 'webpack5',
    // 开发模式 prebundle 在微信开发者工具中易出现 react-dom 模块路径错误
    prebundle: { enable: false }
  },
  plugins: ['@tarojs/plugin-framework-react', '@tarojs/plugin-platform-weapp'],
  cache: {
    enable: false
  },
  defineConstants: {
    'process.env.TARO_APP_API_BASE_URL': JSON.stringify(apiBaseFromEnv)
  },
  mini: {
    postcss: {
      pxtransform: {
        enable: true
      },
      url: {
        enable: true,
        config: {
          limit: 1024
        }
      },
      cssModules: {
        enable: false
      }
    }
  },
  h5: {
    publicPath: '/',
    staticDirectory: 'static'
  }
});

