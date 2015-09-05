(ns cljstone.card-test
  (:require [cljs.test :refer-macros [deftest is testing use-fixtures]])
  (:use [cljstone.card :only [make-random-deck NUM-CARDS-IN-DECK]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(deftest test-numbers
  (let [deck (make-random-deck)]
    (is (= (count deck)
           NUM-CARDS-IN-DECK))))
