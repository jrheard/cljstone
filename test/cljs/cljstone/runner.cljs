(ns cljstone.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljstone.board-test]
            [cljstone.card-test]
            [cljstone.combat-test]
            [cljstone.minion-test]))

(doo-tests 'cljstone.board-test
           'cljstone.card-test
           'cljstone.combat-test
           'cljstone.minion-test)
