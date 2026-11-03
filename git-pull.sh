#!/bin/bash
# ==============================================================================
# 拉取当前仓库最新代码（在服务器 / 本机均可）
# 用法:
#   ./git-pull.sh              # 当前分支 git pull --ff-only
#   ./git-pull.sh dev/v1       # 切换到指定分支再 pull
#   ./git-pull.sh --force      # 允许非快进（慎用：git pull --rebase）
# ==============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

FORCE=0
BRANCH=""
for arg in "$@"; do
    case "$arg" in
        --force|-f) FORCE=1 ;;
        --help|-h)
            sed -n '2,10p' "$0"
            exit 0
            ;;
        *)
            if [ -z "$BRANCH" ]; then
                BRANCH="$arg"
            fi
            ;;
    esac
done

if [ ! -d .git ]; then
    echo "错误: 不是 git 仓库: $REPO_ROOT"
    exit 1
fi

echo "仓库: $REPO_ROOT"
echo "远程更新中..."
git fetch --all --prune

if [ -n "$BRANCH" ]; then
    current="$(git rev-parse --abbrev-ref HEAD)"
    if [ "$current" != "$BRANCH" ]; then
        echo "切换分支: $current -> $BRANCH"
        git checkout "$BRANCH"
    fi
fi

before="$(git rev-parse HEAD)"
branch_now="$(git rev-parse --abbrev-ref HEAD)"
echo "当前分支: $branch_now"
echo "拉取前: $before ($(git log -1 --oneline "$before"))"

if [ "$FORCE" = "1" ]; then
    echo "执行: git pull --rebase"
    git pull --rebase
else
    echo "执行: git pull --ff-only"
    if ! git pull --ff-only; then
        echo ""
        echo "快进失败（本地有提交或冲突）。可："
        echo "  1) 处理本地改动后再 pull"
        echo "  2) 确认后使用: $0 --force   # git pull --rebase"
        exit 1
    fi
fi

after="$(git rev-parse HEAD)"
echo "拉取后: $after ($(git log -1 --oneline "$after"))"

if [ "$before" = "$after" ]; then
    echo "已是最新，无新提交。"
else
    echo "新增提交:"
    git log --oneline "${before}..${after}"
fi

echo ""
git status -sb
echo "完成。"
