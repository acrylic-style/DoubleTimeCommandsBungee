TITLE="$(git show --pretty=format:%s -s HEAD)"
if [[ "$TITLE" == "//"* ]]; then
  exit 1
else
  exit 0
fi