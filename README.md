# patchwerk

A Quil sketch designed to generate patterns for quilts.

## Usage

LightTable - open `core.clj` and press `Ctrl+Shift+Enter` to evaluate the file.

Emacs - run cider, open `core.clj` and press `C-c C-k` to evaluate the file.

REPL - run `(require 'patchwerk.core)`.

## Plan: Toward 1.0.0

Need to port code from other repo and refactor into separate drawing
functions for increased modularity.  Also need to make sure it works
with the
[new functional middleware.](https://github.com/quil/quil/wiki/Functional-mode-(fun-mode))
May not be ideal for the way that the code is implemented right now.
But one approach would be to encode the pixel grid as the only state
of the sketch.

## License

Copyright © 2014 zerosalife

Distributed under the Eclipse Public License version 1.0.
