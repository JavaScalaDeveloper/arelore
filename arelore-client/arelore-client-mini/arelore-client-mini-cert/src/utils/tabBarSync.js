/** 自定义 TabBar 与各个 tab 页之间同步选中项（避免仅依赖 TabBar 内 state 与真实路由不一致） */

const listeners = new Set();

export function subscribeTabBarIndex(listener) {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

export function emitTabBarIndex(index) {
  listeners.forEach((fn) => {
    try {
      fn(index);
    } catch (_e) {
      /* ignore */
    }
  });
}
