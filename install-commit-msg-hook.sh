REPO_DIR=$(git rev-parse --show-toplevel)
TARGET_PATH="$REPO_DIR/.git/hooks/commit-msg"
CONTENT=$(cat <<'EOF'
#!/bin/bash
RED='[31m'
NC='[0m' 

# 获取根目录下的所有文件夹名
FOLDERS=$(ls -d */ | sed 's:/$::')

# 获取提交消息
COMMIT_MSG=$(cat "$1")

# 检查提交消息是否以文件夹名开头
for FOLDER in $FOLDERS; do
    if [[ $COMMIT_MSG == $FOLDER* ]]; then
        exit 0
    fi
done

if [[ $COMMIT_MSG == common* ]]; then
    exit 0
fi

echo -e "${RED}提交失败：提交消息必须以关键字开头${NC}"
echo -e "  ${RED}common 通用改动${NC}"
echo -e "  ${RED}各模块改动对应的关键字：${NC}"
for e in $FOLDERS; do
    echo -e "    ${RED}${e}${NC}"
done

exit 1
EOF
)
if [ -f "$TARGET_PATH" ]; then 
    echo "commit-msg 钩子文件已存在，不再安装。"
else
    echo "$CONTENT" > "$TARGET_PATH"
    chmod +x "$TARGET_PATH"
    echo "安装 commit-msg 成功"
fi

