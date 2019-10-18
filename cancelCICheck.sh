TITLE=$(git show --pretty=format:%s -s HEAD)
if [[ "$TITLE" == \/\/* ]]; then
  false
else
  true
fi