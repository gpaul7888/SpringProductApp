#!/bin/bash
# Opens this project in an ISOLATED VS Code (own settings + extensions, stored in .vscode-isolated/).
# Your normal VS Code and other projects are not touched.
P="$(cd "$(dirname "$0")" && pwd)"
export JAVA_HOME="$(ls -d "$P"/.jdk/jdk-25* | head -1)"; export PATH="$JAVA_HOME/bin:$PATH"
exec code --user-data-dir "$P/.vscode-isolated/data" --extensions-dir "$P/.vscode-isolated/ext" "$P"
