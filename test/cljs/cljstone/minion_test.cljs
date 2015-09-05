(ns cljstone.minion-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]])
  (:use [cljstone.minion :only [get-health all-minions make-minion]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(deftest calculating-health
  (testing "base health, no modifiers"
    (let [minion (-> all-minions :wisp (make-minion 123))]
      (is (= (get-health minion) 1))))

  (testing "health after a couple of attacks"
    (let [minion (-> all-minions
                     :boulderfist-ogre
                     (make-minion 123)
                     (update-in [:modifiers] conj {:type :attack :name nil :effect {:health -2}})
                     (update-in [:modifiers] conj {:type :attack :name nil :effect {:health -3}}))]
      (is (= (get-health minion) 2)))))
