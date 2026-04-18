module.exports = function override(config, env) {
  // 开发环境保留 CRA 默认配置，否则会破坏 HMR（出现 hot-update.json 循环请求）。
  if (env !== 'production') {
    return config;
  }

  // 输出文件名添加内容哈希，确保每次构建都是唯一的
  config.output.filename = 'static/js/[name].[contenthash:8].js';
  config.output.chunkFilename = 'static/js/[name].[contenthash:8].chunk.js';
  
  // CSS 文件也添加哈希
  const miniCssExtractPlugin = config.plugins.find(
    plugin => plugin.constructor.name === 'MiniCssExtractPlugin'
  );
  if (miniCssExtractPlugin) {
    miniCssExtractPlugin.options.filename = 'static/css/[name].[contenthash:8].css';
    miniCssExtractPlugin.options.chunkFilename = 'static/css/[name].[contenthash:8].chunk.css';
  }
  
  // 禁用缓存优化，确保每次都生成新文件
  config.optimization.runtimeChunk = false;
  config.optimization.splitChunks = {
    chunks: 'all',
    cacheGroups: {
      default: false,
      vendors: false
    }
  };
  
  return config;
};
