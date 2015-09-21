(ns cljstone.dealer-test
  (:require [cljs.test :refer-macros [deftest is use-fixtures]])
  (:use [cljstone.dealer :only [make-random-deck NUM-CARDS-IN-DECK]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(deftest test-make-random-deck
  (let [deck (make-random-deck)]
    (is (= (count deck)
           NUM-CARDS-IN-DECK))))
