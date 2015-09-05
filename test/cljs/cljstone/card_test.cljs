(ns cljstone.card-test
  (:require [cljs.test :refer-macros [deftest is testing]])
  (:use [cljstone.card :only [make-random-deck NUM-CARDS-IN-DECK]]))

(deftest test-numbers
  (let [deck (make-random-deck)]
    (is (= (count deck)
           NUM-CARDS-IN-DECK))))
