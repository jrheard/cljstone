(ns cljstone.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljstone.board-test]
            [cljstone.card-test]
            [cljstone.combat-test]
            [cljstone.dealer-test]
            [cljstone.html-test]
            [cljstone.minion-test]
            [cljstone.spell-test]
            [cljstone.utils-test]))

(doo-tests 'cljstone.board-test
           'cljstone.card-test
           'cljstone.combat-test
           'cljstone.dealer-test
           'cljstone.html-test
           'cljstone.minion-test
           'cljstone.spell-test
           'cljstone.utils-test)
