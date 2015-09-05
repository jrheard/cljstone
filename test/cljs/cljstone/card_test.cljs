(ns cljstone.card-test
  (:require [cljs.test :refer-macros [deftest is testing]])
  ; xxx having trouble importing cljstone.card
  ;(:use [cljstone.card :only [make-random-deck]])

  )

(js/console.log "CARD TEST")

(deftest test-numbers
   (is (= 1 1)))
