(ns cljstone.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljstone.card-test]))

(doo-tests 'cljstest.card-test)
