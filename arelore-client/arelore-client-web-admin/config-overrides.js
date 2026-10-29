/**
 * 开发态：绝不把编译/类型错误画到页面上（仅终端可见）。
 * 生产态：保留 contenthash 输出策略。
 */
function softenDevDiagnostics(config) {
  config.plugins = (config.plugins || []).filter((plugin) => {
    const name = plugin && plugin.constructor && plugin.constructor.name;
    return name !== 'ForkTsCheckerWebpackPlugin';
  });

  const eslintPlugin = (config.plugins || []).find(
    (plugin) => plugin && plugin.constructor && plugin.constructor.name === 'ESLintWebpackPlugin'
  );
  if (eslintPlugin && eslintPlugin.options) {
    eslintPlugin.options.failOnError = false;
    eslintPlugin.options.failOnWarning = false;
    eslintPlugin.options.quiet = true;
  }

  return config;
}

function applyProdHashes(config) {
  config.output.filename = 'static/js/[name].[contenthash:8].js';
  config.output.chunkFilename = 'static/js/[name].[contenthash:8].chunk.js';

  const miniCssExtractPlugin = config.plugins.find(
    (plugin) => plugin.constructor.name === 'MiniCssExtractPlugin'
  );
  if (miniCssExtractPlugin) {
    miniCssExtractPlugin.options.filename = 'static/css/[name].[contenthash:8].css';
    miniCssExtractPlugin.options.chunkFilename =
      'static/css/[name].[contenthash:8].chunk.css';
  }

  config.optimization.runtimeChunk = false;
  config.optimization.splitChunks = {
    chunks: 'all',
    cacheGroups: {
      default: false,
      vendors: false,
    },
  };
  return config;
}

module.exports = {
  webpack: function override(config, env) {
    if (env !== 'production') {
      return softenDevDiagnostics(config);
    }
    return applyProdHashes(config);
  },
  devServer: function (configFunction) {
    return function (proxy, allowedHost) {
      const config = configFunction(proxy, allowedHost);
      config.client = {
        ...(config.client || {}),
        overlay: false,
      };
      config.overlay = false;
      return config;
    };
  },
};
