# Usage: cd into this project, then: source env.sh   (current terminal only)
# Safety: does nothing if sourced from outside this project (e.g. from ~/.bashrc).
# Uses project-local .jdk/ and .node/ if present; otherwise the system java/node.
_proj="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
case "$PWD/" in
  "$_proj"/*)
    _jdk="$(ls -d "$_proj"/.jdk/jdk-25* 2>/dev/null | head -1)"
    if [ -n "$_jdk" ]; then export JAVA_HOME="$_jdk"; export PATH="$JAVA_HOME/bin:$PATH"; fi
    if [ -d "$_proj/.node/bin" ]; then export PATH="$_proj/.node/bin:$PATH"; fi
    echo "Project env enabled for this terminal only." ;;
  *)
    echo "env.sh: skipped (only works inside $_proj). Other projects keep system Java." ;;
esac
unset _proj _jdk
