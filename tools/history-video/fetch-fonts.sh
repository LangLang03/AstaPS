#!/bin/sh
# Download the OFL fonts used by the renderer into ./fonts
set -e
cd "$(dirname "$0")" && mkdir -p fonts
curl -sSfL -o fonts/brush.ttf https://fonts.gstatic.com/s/mashanzheng/v18/NaPecZTRCLxvwo41b4gvzkXaRMQ.ttf
curl -sSfL -o fonts/serif.ttf https://fonts.gstatic.com/s/cormorantgaramond/v21/co3umX5slCNuHLi8bLeY9MK7whWMhyjypVO7abI26QOD_hg9GnM.ttf
curl -sSfL -o fonts/serif-italic.ttf https://fonts.gstatic.com/s/cormorantgaramond/v21/co3smX5slCNuHLi8bLeY9MK7whWMhyjYrGFEsdtdc62E6zd5wDDOjw.ttf
curl -sSfL -o fonts/mono.ttf https://fonts.gstatic.com/s/jetbrainsmono/v24/tDbY2o-flEEny0FZhsfKu5WU4zr3E_BX0PnT8RD8yKxjPQ.ttf
