(ns cljstone.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljstone.card-test]))

(js/console.log "RUNNER")
(doo-tests 'cljstone.card-test)
(js/console.log "WHAT ABOUT HERE")
