(ns cljstone.card-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]])
  (:use [cljstone.card :only [make-random-deck]]))

(deftest test-numbers
    (is (= 1 1)))

(js/console.log "butt")
