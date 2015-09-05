(ns cljstone.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljstone.board-test]
            [cljstone.card-test]))

(doo-tests 'cljstone.board-test
           'cljstone.card-test)
